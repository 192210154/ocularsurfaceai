@echo off
start "Ocular Backend" cmd /k "%USERPROFILE%\Desktop\start_backend.bat"
timeout /t 2 >nul
start "Ocular Frontend" cmd /k "%USERPROFILE%\Desktop\start_frontend.bat"

echo Open: http://localhost:5173
echo Backend: http://127.0.0.1:8000/docs