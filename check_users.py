from database import engine
from sqlalchemy import text

def check_users():
    with engine.connect() as conn:
        res = conn.execute(text('SELECT id, name, email FROM users'))
        users = res.fetchall()
        print(f"Total users: {len(users)}")
        for u in users:
            print(f"ID: {u[0]}, Name: {u[1]}, Email: {u[2]}")

if __name__ == "__main__":
    check_users()
