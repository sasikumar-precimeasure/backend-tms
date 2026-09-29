@echo off
REM Double-click convenience wrapper for start-dev.ps1 (see that file for
REM what it does). Bypasses PowerShell's execution-policy prompt for just
REM this one script run, without changing the machine's global policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-dev.ps1"
