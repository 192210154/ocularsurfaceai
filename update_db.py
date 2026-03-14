from database import engine
from sqlalchemy import text

def update_db():
    print("Updating database schema...")
    with engine.connect() as conn:
        try:
            conn.execute(text('ALTER TABLE users ADD COLUMN reset_otp VARCHAR(6) NULL'))
            print("Added reset_otp column.")
        except Exception as e:
            print(f"reset_otp check: {e}")
            
        try:
            conn.execute(text('ALTER TABLE users ADD COLUMN otp_expiry DATETIME NULL'))
            print("Added otp_expiry column.")
        except Exception as e:
            print(f"otp_expiry check: {e}")
            
        conn.commit()
    print("Database update complete.")

if __name__ == "__main__":
    update_db()
