"""
One-time setup script to download complete model files from HuggingFace.

Usage:
    pip install transformers torch --index-url https://download.pytorch.org/whl/cpu
    python download_models.py

This downloads config.json, tokenizer files, and model weights into:
  ./models/sentiment/   (bertweet-base-sentiment-analysis)
  ./models/emotion/     (bert-base-uncased-emotion)

After downloading, these models run fully offline with TRANSFORMERS_OFFLINE=1.
If you already have weight files (*.safetensors or *.bin) in these directories,
the download will overwrite them with fresh copies from HuggingFace.
"""

import os
from transformers import AutoModelForSequenceClassification, AutoTokenizer

MODELS = {
    "./models/sentiment": "finiteautomata/bertweet-base-sentiment-analysis",
    "./models/emotion": "bhadresh-savani/bert-base-uncased-emotion",
}

for path, model_name in MODELS.items():
    os.makedirs(path, exist_ok=True)
    print(f"Downloading {model_name} → {path}")
    AutoModelForSequenceClassification.from_pretrained(model_name).save_pretrained(path)
    AutoTokenizer.from_pretrained(model_name).save_pretrained(path)
    print(f"  Done: {path}")

print("\nAll models downloaded successfully.")
print("You can now run: TRANSFORMERS_OFFLINE=1 uvicorn main:app --port 8000")
