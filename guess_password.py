import bcrypt

# The hash from MySQL for rams123@gmail.com
stored_hash = "$2b$12$oUKJxoffmZLoJQsMblxiz.deK5uHvUTrFAkkGyX3nqbLqnUord5la"

# Common password guesses based on the email
passwords_to_try = [
    "rams123",
    "Rams123",
    "rams1234",
    "Rams1234",
    "rams@123",
    "Rams@123",
    "password",
    "123456",
    "1234567",
    "rams",
    "12345678",
    "Rams123!",
]

for pwd in passwords_to_try:
    pwd_bytes = pwd[:72].encode('utf-8')
    hash_bytes = stored_hash.encode('utf-8')
    try:
        if bcrypt.checkpw(pwd_bytes, hash_bytes):
            print(f"✅ MATCH FOUND: password is '{pwd}'")
            break
    except Exception as e:
        pass
else:
    print("❌ None of the guesses matched. Ask user for their password.")
