import sqlite3
import datetime

# Hash from users.txt for rams123@gmail.com:
# $2y$10$XzLXDCqh4E5u0mzfHGPKzO7S0thqMZoYGzdhEq6s9iylGzdBkBJyG

def restore_user():
    conn = sqlite3.connect('ocular.db')
    cursor = conn.cursor()
    
    email = 'rams123@gmail.com'
    name = 'Rams'
    password_hash = '$2y$10$XzLXDCqh4E5u0mzfHGPKzO7S0thqMZoYGzdhEq6s9iylGzdBkBJyG'
    
    # Check if user already exists
    cursor.execute("SELECT id FROM users WHERE email=?", (email,))
    if cursor.fetchone():
        print(f"User {email} already exists in DB.")
    else:
        cursor.execute("""
            INSERT INTO users (name, email, password_hash, created_at) 
            VALUES (?, ?, ?, ?)
        """, (name, email, password_hash, datetime.datetime.now()))
        conn.commit()
        print(f"User {email} restored to DB.")
        
    conn.close()

if __name__ == "__main__":
    restore_user()
