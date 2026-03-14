from datetime import datetime, timedelta, timezone
from jose import jwt
from jose import jwt

# JWT settings
JWT_SECRET = "ocular_ai_backend_secret_2026_super_secure_key"
JWT_ALG = "HS256"
JWT_EXPIRE_MINUTES = 60 * 24  # 24 hours

import bcrypt

def hash_password(password: str) -> str:
    """
    Hash password safely.
    Bcrypt supports only first 72 bytes, so we truncate.
    """
    password = password[:72]
    pwd_bytes = password.encode('utf-8')
    salt = bcrypt.gensalt()
    return bcrypt.hashpw(pwd_bytes, salt).decode('utf-8')


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Verify password safely with same 72 byte rule.
    Handles legacy $2y$ hashes.
    """
    plain_password = plain_password[:72]
    pwd_bytes = plain_password.encode('utf-8')
    
    # Handle legacy $2y$ hashes (often from PHP) which Python bcrypt doesn't support directly
    if hashed_password.startswith("$2y$"):
        hashed_password = "$2b$" + hashed_password[4:]
        
    hash_bytes = hashed_password.encode('utf-8')
    try:
        return bcrypt.checkpw(pwd_bytes, hash_bytes)
    except Exception as e:
        print(f"bcrypt error: {e}")
        return False


def create_token(user_id: int, email: str) -> str:
    expire = datetime.now(timezone.utc) + timedelta(minutes=JWT_EXPIRE_MINUTES)

    payload = {
        "sub": str(user_id),
        "email": email,
        "exp": expire,
    }

    token = jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)
    return token


def create_reset_token(user_id: int, email: str) -> str:
    """Creates a short-lived token for password reset (15 minutes)."""
    expire = datetime.now(timezone.utc) + timedelta(minutes=15)

    payload = {
        "sub": str(user_id),
        "email": email,
        "exp": expire,
        "type": "reset"
    }

    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)


def create_otp_token(user_id: int, email: str) -> str:
    """Creates a temporary token after OTP verification, valid for 5 minutes."""
    expire = datetime.now(timezone.utc) + timedelta(minutes=5)

    payload = {
        "sub": str(user_id),
        "email": email,
        "exp": expire,
        "type": "otp_verified"
    }

    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)