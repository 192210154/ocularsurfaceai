import tensorflow as tf
import numpy as np
import json
import os
from PIL import Image

MODEL_PATH = "deep_learning_models/eye_model.h5"
LABELS_PATH = "deep_learning_models/labels.json"

def debug_h5():
    print(f"Loading Deep Learning model from {MODEL_PATH}...")
    try:
        model = tf.keras.models.load_model(MODEL_PATH)
        print("Model loaded successfully.")
    except Exception as e:
        print(f"FAILED to load model: {e}")
        return

    # Mock preprocess from main.py
    def preprocess(image: Image.Image):
        image = image.resize((224, 224))
        arr = np.array(image).astype(np.float32)
        arr = np.expand_dims(arr, axis=0)
        return arr

    # Create dummy image
    dummy_img = Image.fromarray(np.random.randint(0, 255, (224, 224, 3), dtype=np.uint8))
    img_tensor = preprocess(dummy_img)

    print(f"Input shape: {img_tensor.shape}, dtype: {img_tensor.dtype}")

    try:
        print("Running prediction...")
        output = model.predict(img_tensor, verbose=0)[0]
        print(f"Prediction success! Output: {output}")
        idx = int(np.argmax(output))
        print(f"Argmax: {idx}")
    except Exception as e:
        print(f"FAILED during prediction: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    debug_h5()
