@echo off
REM 校园美食点评平台 - 微服务批量启动脚本 (Windows)
REM 使用方法：start-services.bat [all|core|gateway]

set PROJECT_ROOT=%~dp0
set LOG_DIR=%PROJECT_ROOT%logs

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

setlocal enabledelayedexpansion

set SERVICES=user-service restaurant-service review-service interaction-service ranking-service notification-service risk-control-service admin-service
set GATEWAYS=campus-review-user-gateway campus-review-admin-gateway

:parseArgs
set MODE=%1
if "%MODE%"=="" set MODE=all
if "%MODE%"=="core" goto :startCore
if "%MODE%"=="gateway" goto :startGateways
if "%MODE%"=="all" goto :startAll

echo Usage: %0 [all^|core^|gateway]
echo   all     - Start all services and gateways
echo   core    - Start only core services
echo   gateway - Start only gateways
exit /b 1

:startCore
echo === Starting Core Services ===
for %%s in (%SERVICES%) do (
    call :startService %%s campus-review-service
    timeout /t 5 /nobreak >nul
)
goto :end

:startGateways
echo === Starting Gateways ===
for %%g in (%GATEWAYS%) do (
    call :startService %%g campus-review-gateway
    timeout /t 3 /nobreak >nul
)
goto :end

:startAll
call :startCore
echo.
call :startGateways
goto :end

:startService
set SERVICE_NAME=%1
set MODULE_PARENT=%2
echo Starting %SERVICE_NAME%...
start /B cmd /c "cd /d %PROJECT_ROOT% && nohup mvn -pl %MODULE_PARENT%/%SERVICE_NAME% -am spring-boot:run > %LOG_DIR%\%SERVICE_NAME%.log 2>&1"
echo PID: !errorlevel!
goto :eof

:end
echo.
echo === Startup Complete ===
echo Logs are in: %LOG_DIR%
endlocal
