@echo off
REM ZH-Learn - Modular Java Application Launcher (Windows)
REM This script demonstrates true modular execution using --module-path

setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
set CLI_TARGET=%SCRIPT_DIR%zh-learn-cli\target

REM Check if application is built
if not exist "%CLI_TARGET%\lib" (
    echo Application not built. Please run: mvn clean package
    exit /b 1
)

REM Build module path - this is the key for true modular execution
set MODULE_PATH=%CLI_TARGET%\lib\*;%CLI_TARGET%\zh-learn-cli-1.0.0-SNAPSHOT.jar

REM Execute using true Java modules
java --module-path "%MODULE_PATH%" --enable-native-access=org.fusesource.jansi,ALL-UNNAMED --module com.zhlearn.cli/com.zhlearn.cli.ZhLearnApplication %*
