import smtplib
import socket
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# These should match main.py
SMTP_SERVER = "smtp.gmail.com"
SMTP_PORT_SSL = 465
SMTP_USER = "abhiswamy2005@gmail.com"
SMTP_PASSWORD = "bypd xffe aiww ogve"

def test_smtp_ssl():
    print(f"Testing SMTP (SSL) for {SMTP_USER} on port {SMTP_PORT_SSL}...")
    try:
        msg = MIMEMultipart()
        msg["From"] = SMTP_USER
        msg["To"] = SMTP_USER # Send to self
        msg["Subject"] = "Ocular SMTP SSL Diagnostic Test"
        msg.attach(MIMEText("If you see this, SSL SMTP is working!", "plain"))

        print("Connecting to server via SSL...")
        # Use SMTP_SSL for port 465
        server = smtplib.SMTP_SSL(SMTP_SERVER, SMTP_PORT_SSL, timeout=15)
        print("Logging in...")
        server.login(SMTP_USER, SMTP_PASSWORD)
        print("Sending message...")
        server.send_message(msg)
        print("Quitting...")
        server.quit()
        print("✅ SUCCESS (SSL): Email sent successfully!")
    except socket.timeout:
        print("❌ FAILED: Connection timed out on port 465. This usually means a firewall or ISP is blocking the port.")
    except Exception as e:
        print(f"❌ FAILED: {type(e).__name__}: {e}")

if __name__ == "__main__":
    test_smtp_ssl()
