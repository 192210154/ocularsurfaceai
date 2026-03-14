import requests
import os
from auth_utils import create_token

URL = "http://127.0.0.1:8000/predict"
TEST_IMAGE = "uploads/ff0f4e85d976401ea883108d25926041.jpg"

def test_predict():
    if not os.path.exists(TEST_IMAGE):
        print(f"File {TEST_IMAGE} not found.")
        return
        
    print("Generating JWT directly...")
    # Use existing user ID 1
    token = create_token(1, "student1@test.com")
    
    print("Sending image...")
    headers = {"Authorization": f"Bearer {token}"}
    with open(TEST_IMAGE, "rb") as f:
        files = {"file": ("image.jpg", f, "image/jpeg")}
        res = requests.post(URL, files=files, headers=headers)
    
    print(f"Status Code: {res.status_code}")
    try:
        print("Response:", res.json())
    except:
        print("Raw Response:", res.text)

if __name__ == "__main__":
    test_predict()
