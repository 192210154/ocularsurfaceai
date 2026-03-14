import tensorflow as tf
import numpy as np
import json
import os
from PIL import Image
import io

MODEL_PATH = "deep_learning_models/eye_model.h5"
LABELS_PATH = "deep_learning_models/labels.json"
TEST_IMAGE = "uploads/ff0f4e85d976401ea883108d25926041.jpg"

def preprocess(image: Image.Image):
    image = image.resize((224, 224))
    arr = np.array(image).astype(np.float32)
    if arr.ndim != 3 or arr.shape[2] != 3:
        raise ValueError("Image must be RGB")
    arr = np.expand_dims(arr, axis=0)
    return arr

def test_inference():
    print(f"Loading Keras model from {MODEL_PATH}")
    try:
        model = tf.keras.models.load_model(MODEL_PATH)
        print("Model loaded successfully.")
    except Exception as e:
        print(f"ERROR: Failed to load model: {e}")
        return

    try:
        with open(LABELS_PATH, "r", encoding="utf-8") as f:
            labels = json.load(f)
        print("LOADED LABELS:", labels)
    except Exception as e:
        print(f"ERROR: Failed to load labels: {e}")
        return

    if not os.path.exists(TEST_IMAGE):
        print(f"ERROR: Test image {TEST_IMAGE} not found.")
        return

    try:
        image = Image.open(TEST_IMAGE).convert("RGB")
        img_arr = preprocess(image)
        print(f"Image preprocessed. Shape: {img_arr.shape}")
        
        output = model.predict(img_arr, verbose=0)[0]
        print("RAW OUTPUT:", output)
        
        idx = int(np.argmax(output))
        conf = float(output[idx])
        disease = labels[idx] if idx < len(labels) else str(idx)
        
        print(f"PREDICTION: {disease} ({conf:.4f})")
    except Exception as e:
        print(f"ERROR during inference: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_inference()
