@REM Maven Wrapper startup script for Windows - Simplified Version
@echo off
@setlocal

set MAVEN_CMD_LINE_ARGS=%*
set MAVEN_HOME=%~dp0.mvn\apache-maven-3.9.6

@REM Check if Maven is already downloaded
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

@REM Download and extract Maven
echo.
echo Maven not found. Downloading Apache Maven 3.9.6...
echo This will take a few minutes on first run...
echo.

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\apache-maven-3.9.6-bin.zip'}"
if errorlevel 1 (
    echo ERROR: Failed to download Maven >&2
    exit /b 1
)

echo Extracting Maven...
powershell -Command "& {Expand-Archive -Path '%TEMP%\apache-maven-3.9.6-bin.zip' -DestinationPath '%~dp0.mvn' -Force}"
if errorlevel 1 (
    echo ERROR: Failed to extract Maven >&2
    exit /b 1
)

del "%TEMP%\apache-maven-3.9.6-bin.zip"
echo Maven installed successfully!
echo.

:runMaven
call "%MAVEN_HOME%\bin\mvn.cmd" %MAVEN_CMD_LINE_ARGS%
exit /b %ERRORLEVEL%
