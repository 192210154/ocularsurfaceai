from sqlalchemy import Column, Integer, String, DateTime
from database import Base


class History(Base):
    __tablename__ = "history"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer)
    image_path = Column(String(255))
    disease = Column(String(50))
    confidence = Column(Integer)
    severity = Column(String(20))
    title = Column(String(150))
    result = Column(String(255))
    original_path = Column(String(255))
    enhanced_path = Column(String(255))
    created_at = Column(DateTime)
