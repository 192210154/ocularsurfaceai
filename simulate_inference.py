import tensorflow as tf
import numpy as np
import json
import os
from PIL import Image
import io

MODEL_PATH = "deep_learning_models/eye_model.h5"
LABELS_PATH = "deep_learning_models/labels.json"

def simulate_request():
    print("Loading model...")
    model = tf.keras.models.load_model(MODEL_PATH)
    
    with open(LABELS_PATH, "r") as f:
        labels = json.load(f)

    # Use the most recent upload if possible
    upload_dir = "uploads"
    files = [os.path.join(upload_dir, f) for f in os.listdir(upload_dir) if f.endswith(('.jpg', '.jpeg', '.png'))]
    if not files:
        print("No uploads found to test.")
        return
    
    test_file = max(files, key=os.path.getmtime)
    print(f"Testing with file: {test_file}")

    with open(test_file, "rb") as f:
        contents = f.read()

    # Mimic main.py logic
    image = Image.open(io.BytesIO(contents)).convert("RGB")
    
    def preprocess(image: Image.Image):
        image = image.resize((224, 224))
        arr = np.array(image).astype(np.float32)
        if arr.ndim != 3 or arr.shape[2] != 3:
            raise Exception("Image must be RGB")
        arr = np.expand_dims(arr, axis=0)
        return arr

    img_input = preprocess(image)
    
    try:
        print("Running model.predict...")
        # Try both ways to see which one fails or works
        output = model.predict(img_input, verbose=0)[0]
        print(f"Predict SUCCESS! Output: {output}")
        
        print("Running model(img)...")
        output2 = model(img_input, training=False).numpy()[0]
        print(f"Model call SUCCESS! Output: {output2}")
        
    except Exception as e:
        print(f"FAILED: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    simulate_request()
