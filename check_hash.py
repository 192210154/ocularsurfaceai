import pymysql

conn = pymysql.connect(host='127.0.0.1', user='root', password='', database='ocular_app')
cur = conn.cursor()
cur.execute("SELECT email, password_hash FROM users WHERE email='rams123@gmail.com'")
row = cur.fetchone()
print(f"Email: {row[0]}")
print(f"Hash: {row[1]}")
conn.close()
