from fastapi import FastAPI, UploadFile, File, Depends, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlalchemy.orm import Session
from jose import jwt, JWTError

import os
import uuid
import json
import io

import tensorflow as tf
import numpy as np
import random
from datetime import datetime, timedelta, timezone
from PIL import Image, UnidentifiedImageError, ImageOps

from database import engine
from models import Base, User, History
from deps import get_db
from auth_utils import (
    verify_password,
    hash_password,
    create_token,
    JWT_SECRET,
    JWT_ALG,
    create_reset_token,
    create_otp_token,
)
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# ---------------- SMTP CONFIG ----------------
# USER: Update these with your real SMTP settings
SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT = 465
SMTP_USER = "abhiswamy2005@gmail.com"
SMTP_PASSWORD = "bypd xffe aiww ogve"
FRONTEND_URL = "http://localhost:5173"  # Adjust based on your dev server

app = FastAPI(title="Ocular Backend", version="1.0")

Base.metadata.create_all(bind=engine)

# ---------------- FOLDERS ----------------
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

# Serve uploaded images
app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")

# ---------------- CORS ----------------
@app.middleware("http")
async def log_requests(request, call_next):
    start_time = datetime.now()
    response = await call_next(request)
    duration = datetime.now() - start_time
    print(f"DEBUG: {request.method} {request.url.path} - {response.status_code} ({duration.total_seconds():.3f}s)")
    return response

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://10.51.101.27:5173",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------- DEEP LEARNING MODEL LOAD ----------------
MODEL_PATH = "deep_learning_models/eye_model.h5"
LABELS_PATH = "deep_learning_models/labels.json"

try:
    print(f"Loading Deep Learning model from {MODEL_PATH}...")
    model_keras = tf.keras.models.load_model(MODEL_PATH)
    print("Deep Learning model loaded successfully.")

except Exception as e:
    raise RuntimeError(f"Failed to load Deep Learning model: {e}")

try:
    with open(LABELS_PATH, "r", encoding="utf-8") as f:
        labels = json.load(f)
    print("LOADED LABELS:", labels)
except Exception as e:
    raise RuntimeError(f"Failed to load labels file: {e}")


# ---------------- HELPERS ----------------
def preprocess(image: Image.Image):
    image = image.resize((224, 224))
    # The model expects images in the [0, 255] range for optimal accuracy
    arr = np.array(image).astype(np.float32)

    if arr.ndim != 3 or arr.shape[2] != 3:
        raise HTTPException(status_code=400, detail="Image must be RGB")

    arr = np.expand_dims(arr, axis=0)  # (1, 224, 224, 3)
    return arr


def severity_from_conf(conf: float, disease: str = None) -> str:
    if disease and disease.lower() == "normal":
        return "Low"
    if conf >= 0.90:
        return "High"
    if conf >= 0.75:
        return "Medium"
    return "Low"


async def send_email(subject: str, recipient: str, body: str, otp_for_log: str = None):
    """Sends a plain HTML email using SMTP. Logs OTP to console on failure."""
    try:
        msg = MIMEMultipart()
        msg["From"] = SMTP_USER
        msg["To"] = recipient
        msg["Subject"] = subject
        msg.attach(MIMEText(body, "html"))

        # Try SSL first if port 465, else try STARTTLS on 587
        if SMTP_PORT == 465:
            server = smtplib.SMTP_SSL(SMTP_SERVER, SMTP_PORT, timeout=15)
        else:
            server = smtplib.SMTP(SMTP_SERVER, SMTP_PORT, timeout=15)
            server.starttls()
            
        server.login(SMTP_USER, SMTP_PASSWORD)
        server.send_message(msg)
        server.quit()
        return True
    except Exception as e:
        print(f"\n❌ EMAIL SEND ERROR ({SMTP_SERVER}:{SMTP_PORT}): {e}")
        if otp_for_log:
            print(f"👉 FALLBACK OTP FOR {recipient}: {otp_for_log}")
            print("Troubleshooting: Ensure port 587 or 465 is open and your App Password is correct.\n")
        return False


def get_current_user_id(authorization: str | None = Header(default=None)) -> int:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing token")

    token = authorization.split(" ", 1)[1].strip()
    if not token:
        raise HTTPException(status_code=401, detail="Missing token")

    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])
        sub = payload.get("sub")
        if sub is None:
            raise HTTPException(status_code=401, detail="Invalid token")
        return int(sub)
    except (JWTError, ValueError):
        raise HTTPException(status_code=401, detail="Invalid token")


# ---------------- ROOT ----------------
@app.get("/")
def root():
    return {"ok": True, "message": "Ocular backend running"}


# ---------------- AUTH ROUTES ----------------
@app.post("/auth/register")
def register(data: dict, db: Session = Depends(get_db)):
    try:
        name = (data.get("name") or "").strip()
        email = (data.get("email") or "").strip().lower()
        password = data.get("password") or ""
        gender = (data.get("gender") or "").strip() or None
        qualification = (data.get("qualification") or "").strip() or None

        if not name or not email or not password:
            raise HTTPException(status_code=400, detail="name/email/password required")

        exists = db.query(User).filter(User.email == email).first()
        if exists:
            raise HTTPException(status_code=409, detail="Email already registered")

        user = User(
            name=name,
            email=email,
            password_hash=hash_password(password),
            gender=gender,
            qualification=qualification,
        )

        db.add(user)
        db.commit()
        db.refresh(user)

        token = create_token(user.id, user.email)

        return {
            "token": token,
            "user": {
                "id": user.id,
                "name": user.name,
                "email": user.email,
                "gender": user.gender,
                "qualification": user.qualification,
            },
        }

    except HTTPException:
        raise
    except Exception as e:
        print("REGISTER ERROR:", str(e))
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/auth/login")
def login(data: dict, db: Session = Depends(get_db)):
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""

    print(f"\n[LOGIN] Attempt for email: {email}")

    if not email or not password:
        print("[LOGIN] Missing email or password")
        raise HTTPException(status_code=400, detail="email/password required")

    user = db.query(User).filter(User.email == email).first()

    if not user:
        print(f"[LOGIN] User not found: {email}")
        raise HTTPException(status_code=401, detail="Invalid email or password")

    is_verified = verify_password(password, user.password_hash)
    print(f"[LOGIN] Password verified: {is_verified}")

    if not is_verified:
        print(f"[LOGIN] Password mismatch for: {email}")
        raise HTTPException(status_code=401, detail="Invalid email or password")

    token = create_token(user.id, user.email)
    print(f"[LOGIN] Success for: {email}")

    return {
        "token": token,
        "user": {
            "id": user.id,
            "name": user.name,
            "email": user.email,
            "gender": user.gender,
            "qualification": user.qualification,
        },
    }


@app.get("/auth/me")
def me(user_id: int = Depends(get_current_user_id), db: Session = Depends(get_db)):
    user = db.query(User).filter(User.id == user_id).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    return {
        "id": user.id,
        "name": user.name,
        "email": user.email,
        "gender": user.gender,
        "qualification": user.qualification,
    }

from fastapi import Form

@app.post("/auth/change_password")
def change_password(
    email: str = Form(...),
    current_password: str = Form(...),
    new_password: str = Form(...),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.email == email).first()
    if not user or not verify_password(current_password, user.password_hash):
        return {"error": True, "message": "Invalid current password"}

    user.password_hash = hash_password(new_password)
    db.commit()
    return {"error": False, "message": "Password changed successfully"}


@app.post("/auth/delete_account")
def delete_account(
    email: str = Form(...),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.email == email).first()
    if not user:
        return {"error": True, "message": "User not found"}

    # Optional: Delete history items associated with user
    db.query(History).filter(History.user_id == user.id).delete()
    db.delete(user)
    db.commit()
    return {"error": False, "message": "Account deleted successfully"}


@app.post("/auth/forgot_password")
async def forgot_password(
    email: str = Form(...),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.email == email).first()
    if not user:
         return {"error": True, "message": "Email not found"}

    # Special case for testing if email is blocked
    if email == "test@gmail.com":
        otp_code = "123456"
    else:
        otp_code = f"{random.randint(100000, 999999)}"
        
    user.reset_otp = otp_code
    # Use naive UTC datetime for safer DB compatibility
    user.otp_expiry = datetime.now(timezone.utc).replace(tzinfo=None) + timedelta(minutes=10)
    db.commit()

    # Log to console ALWAYS so user isn't stuck if email fails
    print(f"\n[AUTH] OTP generated for {email}: {otp_code}")

    email_body = f"""
    <html>
        <body style="font-family: Arial, sans-serif; background-color: #f4f7f6; padding: 20px;">
            <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1);">
                <h2 style="color: #2563eb; text-align: center;">Ocular Account Recovery</h2>
                <p style="font-size: 16px; color: #333;">Hello {user.name},</p>
                <p style="font-size: 16px; color: #333;">Use the following code to reset your password. This code is valid for 10 minutes:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <span style="font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #2563eb; background-color: #f0f7ff; padding: 10px 20px; border-radius: 5px; border: 1px dashed #2563eb;">{otp_code}</span>
                </div>
                <p style="font-size: 14px; color: #666; text-align: center;">If you didn't request this, please ignore this email.</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="font-size: 12px; color: #999; text-align: center;">Ocular AI System - Artificial Intelligence Eye Screening</p>
            </div>
        </body>
    </html>
    """
    
    success = await send_email("Your Password Reset Code - Ocular", email, email_body, otp_for_log=otp_code)
    
    if not success:
        return {"error": False, "message": "Proceeding with mock: Reset code generated. Check server logs."}

    return {"error": False, "message": "A 6-digit verification code has been sent to your email."}


@app.post("/auth/verify_otp")
def verify_otp(
    email: str = Form(...),
    otp: str = Form(...),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.email == email).first()
    if not user:
        return {"error": True, "message": "Email not found"}
    
    # Check if OTP matches and is not expired
    if not user.reset_otp or user.reset_otp != otp:
        return {"error": True, "message": "Invalid verification code"}
    
    # Check expiry
    # Use naive UTC comparison
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    if user.otp_expiry and user.otp_expiry < now:
         return {"error": True, "message": "Verification code has expired"}
    
    # Optional: clear OTP after success or keep it until reset
    # We generate a temporary JWT token to authorize the password change
    token = create_otp_token(user.id, user.email)
    
    return {"error": False, "message": "Code verified", "reset_token": token}


@app.post("/auth/reset_password")
def reset_password(
    token: str = Form(...),
    new_password: str = Form(...),
    db: Session = Depends(get_db)
):
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])
        user_id = payload.get("sub")
        token_type = payload.get("type")
        
        if not user_id or token_type != "otp_verified":
            return {"error": True, "message": "Invalid or expired session"}
        
        user = db.query(User).filter(User.id == int(user_id)).first()
        if not user:
            return {"error": True, "message": "User not found"}

        user.password_hash = hash_password(new_password)
        # Clear the OTP fields
        user.reset_otp = None
        user.otp_expiry = None
        db.commit()
        
        return {"error": False, "message": "Password updated successfully. You can now login."}
    except JWTError:
        return {"error": True, "message": "Invalid or expired session"}
    except Exception as e:
        return {"error": True, "message": str(e)}


@app.post("/auth/update_profile")
def update_profile(
    user_id: int = Form(...),
    name: str = Form(...),
    email: str = Form(...),
    password: str = Form(default=""),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        return {"error": True, "message": "User not found"}

    user.name = name
    user.email = email
    if password:
        user.password_hash = hash_password(password)

    db.commit()
    return {"error": False, "message": "Profile updated successfully"}


# ---------------- PREDICT ROUTE ----------------
@app.post("/predict")
def predict(
    file: UploadFile = File(...),
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    if not file:
        raise HTTPException(status_code=400, detail="file is required")

    allowed_types = {"image/jpeg", "image/png", "image/jpg"}
    if file.content_type not in allowed_types:
        raise HTTPException(status_code=400, detail="Only JPG and PNG images are allowed")

    contents = file.file.read()
    if not contents:
        raise HTTPException(status_code=400, detail="Empty file")

    try:
        image = Image.open(io.BytesIO(contents)).convert("RGB")
        image = ImageOps.exif_transpose(image)  # Fix orientation for mobile photos
    except UnidentifiedImageError:
        raise HTTPException(status_code=400, detail="Uploaded file is not a valid image")

    # Save uploaded image
    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in [".jpg", ".jpeg", ".png"]:
        ext = ".jpg"

    unique_name = f"{uuid.uuid4().hex}{ext}"
    saved_path = os.path.join(UPLOAD_DIR, unique_name)

    with open(saved_path, "wb") as f:
        f.write(contents)

    # Path to store in DB / return to frontend
    db_image_path = f"/uploads/{unique_name}"

    img = preprocess(image)

    try:
        # Deep Learning model inference
        output = model_keras.predict(img, verbose=0)[0]

        print("RAW OUTPUT:", output)
        print("ARGMAX INDEX:", int(np.argmax(output)))
        print("LABELS:", labels)

    except Exception as e:
        print("\n❌ INFERENCE ERROR:")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")

    idx = int(np.argmax(output))
    conf = float(output[idx])

    if isinstance(labels, list) and 0 <= idx < len(labels):
        disease = labels[idx]
    else:
        disease = str(idx)

    severity = severity_from_conf(conf, disease)
    conf_percent = int(round(conf * 100))

    # Strict safeguards for non-eye images
    is_invalid = False
    
    # if disease == "no_eye":
    #     is_invalid = True
    # elif disease == "normal" and conf < 0.80:
    #     # Require higher confidence for "Normal" to avoid false negatives on non-eye images
    #     is_invalid = True
    # elif conf < 0.50:
    #     # General low-confidence rejection for medical safety
    #     is_invalid = True

    if is_invalid:
        raise HTTPException(
            status_code=400,
            detail="Could not detect a clear eye in the image. Please upload a high-quality, focused photo of the eye."
        )

    try:
        history_item = History(
            user_id=user_id,
            image_path=db_image_path,
            disease=disease,
            confidence=conf_percent,
            severity=severity,
            title="AI Scan",
            result=f"{disease} - {conf_percent}%",
            original_path=db_image_path,
            enhanced_path=None,
            created_at=datetime.now() # Manually adding date for robustness
        )

        db.add(history_item)
        db.commit()
        db.refresh(history_item)
    except Exception as e:
        print("\n❌ DATABASE ERROR:")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

    return {
        "disease": disease,
        "confidence": conf,
        "confidence_percent": conf_percent,
        "severity": severity,
        "history_id": history_item.id,
        "image_path": db_image_path,
    }


# ---------------- HISTORY ROUTES ----------------
@app.get("/history")
def get_history(
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db),
):
    rows = (
        db.query(History)
        .filter(History.user_id == user_id)
        .order_by(History.created_at.desc())
        .limit(50)
        .all()
    )

    return [
        {
            "id": str(row.id),
            "user_id": row.user_id,
            "title": row.title,
            "disease": row.disease,
            "confidence": row.confidence,
            "severity": row.severity,
            "result": row.result,
            "image_path": row.image_path,
            "image_url": f"http://10.51.101.27:8000{row.image_path}",
            "original_path": row.original_path,
            "enhanced_path": row.enhanced_path,
            "created_at": row.created_at.isoformat() if row.created_at else None,
            "date": row.created_at.strftime("%Y-%m-%d") if row.created_at else "",
            "time": row.created_at.strftime("%H:%M:%S") if row.created_at else "",
        }
        for row in rows
    ]