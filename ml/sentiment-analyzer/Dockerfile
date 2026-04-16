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

# Copy local model directories (must contain config.json, tokenizer files, and weights)
COPY models/ ./models/
COPY main.py ./

USER app

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
