from sqlalchemy.orm import Session
from database import SessionLocal, engine
import models
from datetime import datetime

def test_db_no_date():
    print("Testing DB History insert (NO created_at)...")
    db = SessionLocal()
    try:
        new_item = models.History(
            user_id=1,
            image_path="/uploads/test_no_date.jpg",
            disease="normal",
            confidence=95,
            severity="Low",
            title="AI Scan Test No Date",
            result="normal - 95%",
            original_path="/uploads/test_no_date.jpg",
            enhanced_path=None
            # NOT passing created_at
        )
        db.add(new_item)
        db.commit()
        db.refresh(new_item)
        print(f"SUCCESS! Created history item with ID: {new_item.id}")
        
    except Exception as e:
        print(f"FAILED: {e}")
        import traceback
        traceback.print_exc()
    finally:
        db.close()

if __name__ == "__main__":
    test_db_no_date()
