"""
Reset the password for rams123@gmail.com in ocular.db to a known value.
New password: Rams@1234
"""
import sqlite3
import bcrypt

def reset_password():
    conn = sqlite3.connect('ocular.db')
    cursor = conn.cursor()
    
    new_password = "Rams@1234"
    password_bytes = new_password[:72].encode('utf-8')
    new_hash = bcrypt.hashpw(password_bytes, bcrypt.gensalt()).decode('utf-8')
    
    cursor.execute("UPDATE users SET password_hash=? WHERE email=?", 
                   (new_hash, 'rams123@gmail.com'))
    
    if cursor.rowcount == 0:
        print("User rams123@gmail.com not found in DB!")
    else:
        conn.commit()
        print(f"Password reset for rams123@gmail.com")
        print(f"New password: {new_password}")
        print(f"New hash: {new_hash}")
    
    conn.close()

if __name__ == "__main__":
    reset_password()
