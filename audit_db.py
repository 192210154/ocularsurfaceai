import sqlite3

def audit_db():
    conn = sqlite3.connect('ocular.db')
    cursor = conn.cursor()
    
    # Check tables
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = cursor.fetchall()
    print(f"Tables: {tables}")
    
    for table in tables:
        tn = table[0]
        cursor.execute(f"SELECT count(*) FROM {tn}")
        count = cursor.fetchone()[0]
        print(f"Table {tn}: {count} rows")
        
        if tn == 'users':
            cursor.execute("SELECT id, name, email FROM users")
            rows = cursor.fetchall()
            for r in rows:
                print(f"User: {r}")
                
    conn.close()

if __name__ == "__main__":
    audit_db()
