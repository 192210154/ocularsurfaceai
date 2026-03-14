"""
Migrate the users table to add missing columns:
- gender
- qualification  
- reset_otp
- otp_expiry
"""
import sqlite3

def migrate():
    conn = sqlite3.connect('ocular.db')
    cursor = conn.cursor()
    
    # Get existing columns
    cursor.execute("PRAGMA table_info(users)")
    cols = {row[1] for row in cursor.fetchall()}
    print(f"Existing columns: {cols}")
    
    migrations = [
        ("gender", "VARCHAR(10)"),
        ("qualification", "VARCHAR(20)"),
        ("reset_otp", "VARCHAR(6)"),
        ("otp_expiry", "DATETIME"),
    ]
    
    for col_name, col_type in migrations:
        if col_name not in cols:
            cursor.execute(f"ALTER TABLE users ADD COLUMN {col_name} {col_type}")
            print(f"Added column: {col_name}")
        else:
            print(f"Column already exists: {col_name}")
    
    conn.commit()
    conn.close()
    print("Migration complete.")

if __name__ == "__main__":
    migrate()
