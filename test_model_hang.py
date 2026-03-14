import sys
import os
import tensorflow as tf
from PIL import Image
import numpy as np

# Suppress TF logs
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3' 

MODEL_PATH = "deep_learning_models/eye_model.h5"

def test_predict():
    print("Loading model...")
    model = tf.keras.models.load_model(MODEL_PATH)
    print("Model loaded.")
    
    print("Creating dummy image...")
    img = Image.new('RGB', (224, 224), color = 'red')
    arr = np.array(img).astype(np.float32)
    arr = np.expand_dims(arr, axis=0)
    
    print("Predicting...")
    output = model.predict(arr, verbose=0)[0]
    print("Prediction done:", output)

if __name__ == "__main__":
    test_predict()
