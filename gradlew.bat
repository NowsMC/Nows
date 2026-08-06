@echo off
setlocal
set ROOT=%~dp0
if exist "%ROOT%gradle\wrapper\gradle-wrapper.jar" (
  java -classpath "%ROOT%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
  exit /b %ERRORLEVEL%
)
set VERSION=9.1.0
set EXPECTED_SHA256=a17ddd85a26b6a7f5ddb71ff8b05fc5104c0202c6e64782429790c933686c806
set BOOT=%ROOT%.gradle-bootstrap
set DIST=%BOOT%\gradle-%VERSION%
set ZIP=%BOOT%\gradle-%VERSION%-bin.zip
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%BOOT%" mkdir "%BOOT%"
  echo Nows bootstrap: downloading Gradle %VERSION%... 1>&2
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b %ERRORLEVEL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$actual=(Get-FileHash -Algorithm SHA256 '%ZIP%').Hash.ToLower(); if ($actual -ne '%EXPECTED_SHA256%') { Write-Error 'Gradle distribution SHA-256 mismatch'; exit 1 }"
  if errorlevel 1 exit /b %ERRORLEVEL%
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%BOOT%'"
  if errorlevel 1 exit /b %ERRORLEVEL%
)
call "%DIST%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
