import tensorflow as tf
import numpy as np
import json
import os
from PIL import Image

MODEL_PATH = "ml_models/new_model/eye_model.tflite"
LABELS_PATH = "ml_models/new_model/labels.json"

def test_inference():
    print(f"Loading model from {MODEL_PATH}")
    try:
        interpreter = tf.lite.Interpreter(model_path=MODEL_PATH)
        interpreter.allocate_tensors()
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
        print("Model loaded successfully.")
    except Exception as e:
        print(f"ERROR: Failed to load model: {e}")
        return

    # Create a dummy image
    img = np.random.randint(0, 255, (1, 224, 224, 3)).astype(np.float32)
    
    print(f"Input details: {input_details}")
    print(f"Output details: {output_details}")
    
    try:
        interpreter.set_tensor(input_details[0]["index"], img)
        print("Input tensor set.")
        interpreter.invoke()
        print("Inference invoked.")
        output = interpreter.get_tensor(output_details[0]["index"])[0]
        print(f"Success! Output: {output}")
    except Exception as e:
        print(f"ERROR during inference: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    test_inference()
