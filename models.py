from sqlalchemy import Column, Integer, String, DateTime
from datetime import datetime
from database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    email = Column(String(120), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    gender = Column(String(10))
    qualification = Column(String(20))

    reset_otp = Column(String(6), nullable=True)
    otp_expiry = Column(DateTime, nullable=True)


class History(Base):
    __tablename__ = "history"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, nullable=False)

    image_path = Column(String(255))
    disease = Column(String(50))
    confidence = Column(Integer)
    severity = Column(String(20))

    title = Column(String(150), nullable=False)
    result = Column(String(255), nullable=False)

    original_path = Column(String(255))
    enhanced_path = Column(String(255))

    created_at = Column(DateTime, default=datetime.utcnow)