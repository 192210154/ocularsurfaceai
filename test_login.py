def test_bcrypt():
    from auth_utils import hash_password, verify_password
    try:
        pw = "my_new_password"
        h = hash_password(pw)
        print("Hash:", h)
        print("Verify new:", verify_password(pw, h))
        
        # Test legacy $2y$ (we manually create one from our db sample)
        h_old = "$2y$10$OodXMzBmSLoXJJqG8p64Ouoipq1EPMaycwbhFhWB1gTEXeLsG5evm"
        # We don't know the password for student1@test.com but we can verify it doesn't crash!
        print("Verify old wrong pass:", verify_password("wrong", h_old))
    except Exception as e:
        print("Error:", repr(e))

if __name__ == "__main__":
    test_bcrypt()
