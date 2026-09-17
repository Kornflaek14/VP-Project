@echo off
setlocal
pushd "%~dp0"
call gradlew.bat :lwjgl3:packageWindowsZip --warning-mode all
set "BUILD_EXIT_CODE=%ERRORLEVEL%"
if "%BUILD_EXIT_CODE%"=="0" (
    echo.
    echo Game: "%~dp0lwjgl3\build\windows\Locura\Locura.exe"
    echo Share: "%~dp0lwjgl3\build\distributions\Locura-windows.zip"
) else (
    echo.
    echo Build failed. Use a full JDK 17 or newer with JAVA_HOME configured.
)
popd
pause
exit /b %BUILD_EXIT_CODE%
