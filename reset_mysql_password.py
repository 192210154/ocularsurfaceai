import bcrypt
import pymysql

# Reset password for rams123@gmail.com in MySQL
new_password = "rams123"
password_bytes = new_password[:72].encode('utf-8')
new_hash = bcrypt.hashpw(password_bytes, bcrypt.gensalt()).decode('utf-8')

conn = pymysql.connect(host='127.0.0.1', user='root', password='', database='ocular_app')
cur = conn.cursor()
cur.execute("UPDATE users SET password_hash=%s WHERE email=%s", (new_hash, 'rams123@gmail.com'))
conn.commit()
print(f"Password reset for rams123@gmail.com to: {new_password}")
conn.close()
