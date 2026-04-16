FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
	PYTHONUNBUFFERED=1 \
	PIP_NO_CACHE_DIR=1 \
	MODELS_DIR=/app/models \
	TRANSFORMERS_OFFLINE=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && \
	rm -rf /var/lib/apt/lists/* && \
	addgroup --system app && adduser --system --ingroup app app

COPY requirements.txt ./
# Install CPU-only PyTorch (much smaller than full torch with CUDA)
RUN pip install torch --index-url https://download.pytorch.org/whl/cpu && \
	pip install -r requirements.txt

COPY main.py ./

# Model weights are NOT baked into the image — mount them at runtime.
# Run 'python download_models.py' once to populate ml/sentiment-analyzer/models/,
# then docker compose will bind-mount that directory here.
RUN mkdir -p /app/models && chown app:app /app/models

USER app

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
