"""
MindTrack Journal — Sentiment & Emotion Analyzer

A FastAPI service that performs both sentiment analysis and emotion detection
on journal page text using locally stored transformer models:
  - Sentiment: finiteautomata/bertweet-base-sentiment-analysis (POS/NEG/NEU)
  - Emotion:   bhadresh-savani/bert-base-uncased-emotion (6 emotions)

Analysis is triggered via Kafka (journal.page.analysis.request) and results
are published back to Kafka (journal.page.analysis.result).
The REST endpoint POST /analyze is kept for manual/ad-hoc analysis.

Models are loaded exclusively from local directories. No HuggingFace calls are made.
"""

import asyncio
import json
import os
from concurrent.futures import ThreadPoolExecutor
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import pipeline
from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
import logging
import time

# Configuration
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("sentiment-analyzer")

MODELS_DIR = os.environ.get("MODELS_DIR", "./models")
SENTIMENT_MODEL_PATH = os.path.join(MODELS_DIR, "sentiment")
EMOTION_MODEL_PATH = os.path.join(MODELS_DIR, "emotion")
MODEL_VERSION = "v2-bertweet-emotion"

KAFKA_BOOTSTRAP = os.environ.get("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
KAFKA_REQUEST_TOPIC = os.environ.get("KAFKA_TOPIC_SENTIMENT_REQUEST", "journal.page.analysis.request")
KAFKA_RESULT_TOPIC = os.environ.get("KAFKA_TOPIC_SENTIMENT_RESULT", "journal.page.analysis.result")
KAFKA_GROUP_ID = os.environ.get("KAFKA_GROUP_ID", "sentiment-analyzer")

# BERTweet label mapping to consistent labels
SENTIMENT_LABEL_MAP = {
    "POS": "POSITIVE",
    "NEG": "NEGATIVE",
    "NEU": "NEUTRAL",
}

sentiment_pipeline = None
emotion_pipeline = None
kafka_consumer_task = None
inference_executor: ThreadPoolExecutor | None = None

_WEIGHT_EXTENSIONS = (".safetensors", ".bin")


def _validate_model_dir(path: str, name: str):
    """Fail fast if a model directory is missing required files."""
    if not os.path.isdir(path):
        raise FileNotFoundError(
            f"{name} model directory not found: {path}. "
            f"Run 'python download_models.py' to download models."
        )
    if not os.path.exists(os.path.join(path, "config.json")):
        raise FileNotFoundError(
            f"{name} model is missing config.json in {path}. "
            f"Ensure config.json and tokenizer files are present alongside the model weights."
        )
    has_weights = any(
        f.endswith(_WEIGHT_EXTENSIONS)
        for f in os.listdir(path)
    )
    if not has_weights:
        raise FileNotFoundError(
            f"{name} model is missing weight files (*.safetensors or *.bin) in {path}. "
            f"Run 'python download_models.py' to download models."
        )


def _load_models():
    """Load both ML models from local directories."""
    global sentiment_pipeline, emotion_pipeline

    _validate_model_dir(SENTIMENT_MODEL_PATH, "Sentiment")
    _validate_model_dir(EMOTION_MODEL_PATH, "Emotion")

    logger.info("Loading sentiment model from: %s", SENTIMENT_MODEL_PATH)
    sentiment_pipeline = pipeline(
        "text-classification",
        model=SENTIMENT_MODEL_PATH,
        truncation=True,
        max_length=128,
    )
    logger.info("Sentiment model loaded.")

    logger.info("Loading emotion model from: %s", EMOTION_MODEL_PATH)
    emotion_pipeline = pipeline(
        "text-classification",
        model=EMOTION_MODEL_PATH,
        truncation=True,
        max_length=512,
    )
    logger.info("Emotion model loaded.")


# Kafka Consumer Loop
async def kafka_consumer_loop():
    """Consume analysis requests from Kafka, run models, publish results."""
    producer = AIOKafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8") if k else None,
    )
    consumer = AIOKafkaConsumer(
        KAFKA_REQUEST_TOPIC,
        bootstrap_servers=KAFKA_BOOTSTRAP,
        group_id=KAFKA_GROUP_ID,
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        auto_offset_reset="earliest",
    )

    await producer.start()
    await consumer.start()
    logger.info("Kafka consumer started — listening on '%s'", KAFKA_REQUEST_TOPIC)

    try:
        async for msg in consumer:
            try:
                data = msg.value
                page_id = data["page_id"]
                text = data["text"]

                if not text or not text.strip():
                    logger.warning("Empty text for page %s, skipping.", page_id)
                    continue

                start = time.time()
                loop = asyncio.get_running_loop()
                result = await loop.run_in_executor(inference_executor, run_analysis, page_id, text)
                elapsed_ms = round((time.time() - start) * 1000, 2)

                await producer.send_and_wait(
                    KAFKA_RESULT_TOPIC,
                    key=page_id,
                    value=result,
                )
                logger.info(
                    "Kafka: analyzed page %s — sentiment=%s, emotion=%s in %sms",
                    page_id, result["sentiment_label"], result["dominant_emotion"], elapsed_ms,
                )

            except asyncio.CancelledError:
                raise
            except Exception as e:
                logger.error("Error processing Kafka message: %s", e, exc_info=True)

    except asyncio.CancelledError:
        logger.info("Kafka consumer loop cancelled.")
    finally:
        async def _stop(coro, name):
            try:
                await asyncio.wait_for(coro, timeout=5.0)
            except Exception as e:
                logger.warning("Error stopping %s: %s", name, e)

        await _stop(consumer.stop(), "consumer")
        await _stop(producer.stop(), "producer")
        logger.info("Kafka consumer stopped.")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load ML models and start Kafka consumer on startup."""
    global kafka_consumer_task, inference_executor

    _load_models()

    inference_executor = ThreadPoolExecutor(max_workers=2)
    kafka_consumer_task = asyncio.create_task(kafka_consumer_loop())
    logger.info("Kafka consumer task started.")

    yield

    if kafka_consumer_task:
        kafka_consumer_task.cancel()
        try:
            await kafka_consumer_task
        except asyncio.CancelledError:
            pass
    inference_executor.shutdown(wait=True)
    logger.info("Shutdown complete.")


app = FastAPI(
    title="MindTrack Sentiment & Emotion Analyzer",
    description="Self-hosted sentiment and emotion analysis — your text never leaves your infrastructure.",
    version="2.0.0",
    lifespan=lifespan,
)


# DTOs (for REST endpoint)
class AnalysisRequest(BaseModel):
    page_id: str
    text: str


class AnalysisResponse(BaseModel):
    page_id: str
    sentiment_label: str
    sentiment_scores: dict[str, float]
    dominant_emotion: str
    emotion_vector: dict[str, float]
    analyzer_version: str


# Analysis Logic (shared by Kafka consumer and REST endpoint)
def _unwrap_pipeline_output(raw: list) -> list:
    """Normalise pipeline output — handle both [[{...}]] and [{...}] shapes."""
    first = raw[0]
    return first if isinstance(first, list) else raw


def analyze_sentiment(text: str) -> tuple[str, dict[str, float]]:
    """Run sentiment analysis. Returns (label, scores_dict)."""
    results = _unwrap_pipeline_output(sentiment_pipeline(text, top_k=None))
    scores = {}
    for item in results:
        mapped_label = SENTIMENT_LABEL_MAP.get(item["label"], item["label"].upper())
        scores[mapped_label] = round(item["score"], 4)

    dominant_label = max(scores, key=scores.get)
    return dominant_label, scores


def analyze_emotion(text: str) -> tuple[str, dict[str, float]]:
    """Run emotion detection. Returns (dominant_emotion, emotion_vector)."""
    results = _unwrap_pipeline_output(emotion_pipeline(text, top_k=None))
    vector = {}
    for item in results:
        vector[item["label"]] = round(item["score"], 4)

    dominant = max(vector, key=vector.get)
    return dominant, vector


def run_analysis(page_id: str, text: str) -> dict:
    """Run both models and return a result dict."""
    sentiment_label, sentiment_scores = analyze_sentiment(text)
    dominant_emotion, emotion_vector = analyze_emotion(text)

    return {
        "page_id": page_id,
        "sentiment_label": sentiment_label,
        "sentiment_scores": sentiment_scores,
        "dominant_emotion": dominant_emotion,
        "emotion_vector": emotion_vector,
        "analyzer_version": MODEL_VERSION,
    }


# REST Endpoints (for manual/ad-hoc analysis)
@app.post("/analyze", response_model=AnalysisResponse)
async def analyze(request: AnalysisRequest):
    """
    Manually analyze sentiment and emotion of text.
    For automated analysis, use the Kafka pipeline instead.
    """
    if not request.text or not request.text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")

    start = time.time()
    loop = asyncio.get_running_loop()
    result = await loop.run_in_executor(inference_executor, run_analysis, request.page_id, request.text)
    elapsed_ms = round((time.time() - start) * 1000, 2)

    logger.info(
        "REST: analyzed page %s — sentiment=%s, emotion=%s in %sms",
        request.page_id, result["sentiment_label"], result["dominant_emotion"], elapsed_ms,
    )

    return AnalysisResponse(**result)


@app.get("/health")
async def health():
    """Health check endpoint for container orchestration."""
    models_ready = sentiment_pipeline is not None and emotion_pipeline is not None
    kafka_alive = (
        kafka_consumer_task is not None
        and not kafka_consumer_task.done()
    )
    healthy = models_ready and kafka_alive
    return {
        "status": "healthy" if healthy else "degraded",
        "service": "sentiment-analyzer",
        "models_loaded": models_ready,
        "kafka_consumer": "running" if kafka_alive else "stopped",
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
