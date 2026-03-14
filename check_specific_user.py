from database import engine
from sqlalchemy import text

def check_user_exists(email):
    with engine.connect() as conn:
        res = conn.execute(text(f"SELECT email FROM users WHERE email = '{email}'"))
        user = res.fetchone()
        if user:
            print(f"✅ User found: {user[0]}")
        else:
            print(f"❌ User NOT found: {email}")

if __name__ == "__main__":
    check_user_exists("abhiswamy2005@gmail.com")
