import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# These should match main.py
SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT = 587
SMTP_USER = "abhiswamy2005@gmail.com"
SMTP_PASSWORD = "bypd xffe aiww ogve"

def test_smtp():
    print(f"Testing SMTP for {SMTP_USER}...")
    try:
        msg = MIMEMultipart()
        msg["From"] = SMTP_USER
        msg["To"] = SMTP_USER # Send to self
        msg["Subject"] = "Ocular SMTP Diagnostic Test"
        msg.attach(MIMEText("If you see this, your SMTP settings are working!", "plain"))

        print("Connecting to server...")
        server = smtplib.SMTP(SMTP_SERVER, SMTP_PORT, timeout=10)
        print("Starting TLS...")
        server.starttls()
        print("Logging in...")
        server.login(SMTP_USER, SMTP_PASSWORD)
        print("Sending message...")
        server.send_message(msg)
        print("Quitting...")
        server.quit()
        print("✅ SUCCESS: Email sent successfully!")
    except Exception as e:
        print(f"❌ FAILED: {type(e).__name__}: {e}")

if __name__ == "__main__":
    test_smtp()
