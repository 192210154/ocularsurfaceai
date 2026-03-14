import os
import json
import numpy as np
import tensorflow as tf
from PIL import Image

MODEL_PATH = "ml_models/new_model/eye_model.tflite"
LABELS_PATH = "ml_models/new_model/labels.json"
VAL_DIR = r"C:\Users\Admin\Desktop\eye_dataset\val"

def verify():
    if not os.path.exists(MODEL_PATH):
        print(f"Error: Model not found at {MODEL_PATH}")
        return

    # Load labels
    with open(LABELS_PATH, "r") as f:
        labels = json.load(f)
    print(f"Labels: {labels}")

    # Load TFLite model
    interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # Get sample images from each directory in val
    for class_name in os.listdir(VAL_DIR):
        class_dir = os.path.join(VAL_DIR, class_name)
        if not os.path.isdir(class_dir):
            continue
        
        print(f"\n--- Testing Class: {class_name} ---")
        images = [f for f in os.listdir(class_dir) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
        # Test up to 3 images per class
        for img_name in images[:3]:
            img_path = os.path.join(class_dir, img_name)
            
            # Preprocess
            img = Image.open(img_path).convert("RGB").resize((224, 224))
            img_array = np.array(img).astype(np.float32)
            # EfficientNet usually expects [0, 255] then internal preprocessing
            # But we used tf.keras.applications.efficientnet.preprocess_input in training
            # For B0, it scales to [0, 255] and then does nothing or rescales. 
            # In TFLite, we must be careful.
            img_array = np.expand_dims(img_array, axis=0)

            # Invoke interpreter
            interpreter.set_tensor(input_details[0]['index'], img_array)
            interpreter.invoke()
            output = interpreter.get_tensor(output_details[0]['index'])[0]

            idx = np.argmax(output)
            pred_label = labels[idx]
            conf = output[idx]

            print(f"File: {img_name} -> Predicted: {pred_label} ({conf:.2f})")

if __name__ == "__main__":
    verify()
