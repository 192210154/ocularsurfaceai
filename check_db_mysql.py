import pymysql
import json

try:
    connection = pymysql.connect(
        host='127.0.0.1',
        user='root',
        password='',
        database='ocular_app',
        port=3306
    )
    cursor = connection.cursor(pymysql.cursors.DictCursor)
    cursor.execute("SELECT id, user_id, image_path, disease, created_at FROM history ORDER BY created_at DESC LIMIT 5")
    rows = cursor.fetchall()
    
    # Format datetime for JSON
    for row in rows:
        if row['created_at']:
            row['created_at'] = row['created_at'].isoformat()
            
    print(json.dumps(rows, indent=2))
    connection.close()
except Exception as e:
    print(f"Error: {e}")
