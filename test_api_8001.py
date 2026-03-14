import requests
import os

def test_api():
    url = 'http://127.0.0.1:8001/predict'
    upload_dir = 'uploads'
    # Get the latest file
    files = [os.path.join(upload_dir, f) for f in os.listdir(upload_dir) if f.endswith('.jpg')]
    if not files:
        print("No files to test.")
        return
    latest_file = max(files, key=os.path.getmtime)
    print(f"Testing with: {latest_file}")
    
    with open(latest_file, 'rb') as f:
        r = requests.post(url, files={'file': ('test.jpg', f, 'image/jpeg')})
        print(f"Status Code: {r.status_code}")
        print(f"Response: {r.text}")

if __name__ == "__main__":
    test_api()
