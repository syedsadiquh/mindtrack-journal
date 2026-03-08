"""
MindTrack Journal — Sentiment Analyzer (Serverless ML Function)

A lightweight FastAPI service that performs sentiment analysis on journal entry text.
Designed to be deployed as a serverless cloud function (AWS Lambda, GCP Cloud Run, etc.)
or run as a standalone container.

Endpoint:
    POST /analyze
    Request:  { "entry_id": "uuid", "text": "journal content..." }
    Response: { "entry_id": "uuid", "sentiment": "POSITIVE|NEGATIVE|NEUTRAL", "score": 0.85 }
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from textblob import TextBlob
import logging
import time

# ============================================================
# Configuration
# ============================================================
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("sentiment-analyzer")

app = FastAPI(
    title="MindTrack Sentiment Analyzer",
    description="Self-hosted sentiment analysis — your text never leaves your infrastructure.",
    version="1.0.0",
)


# ============================================================
# DTOs
# ============================================================
class SentimentRequest(BaseModel):
    entry_id: str
    text: str


class SentimentResponse(BaseModel):
    entry_id: str
    sentiment: str  # POSITIVE, NEGATIVE, NEUTRAL
    score: float    # -1.0 to 1.0 (TextBlob polarity), normalized to 0.0–1.0 in response


# ============================================================
# Sentiment Analysis Logic
# ============================================================
def analyze_sentiment(text: str) -> tuple[str, float]:
    """
    Analyze sentiment using TextBlob.
    Returns (label, score) where score is the raw polarity (-1.0 to 1.0).

    For production, replace with a transformer model like:
        from transformers import pipeline
        classifier = pipeline("sentiment-analysis",
                              model="distilbert-base-uncased-finetuned-sst-2-english")
    """
    blob = TextBlob(text)
    polarity = blob.sentiment.polarity  # -1.0 to 1.0

    if polarity > 0.1:
        label = "POSITIVE"
    elif polarity < -0.1:
        label = "NEGATIVE"
    else:
        label = "NEUTRAL"

    return label, polarity


# ============================================================
# API Endpoints
# ============================================================
@app.post("/analyze", response_model=SentimentResponse)
async def analyze(request: SentimentRequest):
    """
    Analyze sentiment of journal entry text.
    This endpoint is called asynchronously by the Java backend via Feign.
    """
    if not request.text or not request.text.strip():
        raise HTTPException(status_code=400, detail="Text cannot be empty")

    start = time.time()
    label, raw_polarity = analyze_sentiment(request.text)

    # Normalize polarity from [-1.0, 1.0] to [0.0, 1.0]
    normalized_score = round((raw_polarity + 1.0) / 2.0, 4)

    elapsed_ms = round((time.time() - start) * 1000, 2)
    logger.info(
        f"Analyzed entry {request.entry_id}: {label} ({normalized_score}) in {elapsed_ms}ms"
    )

    return SentimentResponse(
        entry_id=request.entry_id,
        sentiment=label,
        score=normalized_score,
    )


@app.get("/health")
async def health():
    """Health check endpoint for container orchestration."""
    return {"status": "healthy", "service": "sentiment-analyzer"}

