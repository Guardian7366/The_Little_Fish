@echo off
setlocal
cd /d "%~dp0"
set "FISH_JAVA=java"
for /f "delims=" %%J in ('where jar.exe 2^>nul') do if exist "%%~dpJjava.exe" set "FISH_JAVA=%%~dpJjava.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "FISH_JAVA=%JAVA_HOME%\bin\java.exe"
set "FISH_JAR=TheLittleFish.jar"
if not exist "%FISH_JAR%" set "FISH_JAR=build\TheLittleFish.jar"
if not exist "%FISH_JAR%" (
    echo No se encontro el juego. Extrae el ZIP completo o ejecuta Compilar.cmd.
    pause
    exit /b 1
)
"%FISH_JAVA%" -jar "%FISH_JAR%"
if errorlevel 1 (
    echo.
    echo No se pudo iniciar el juego. Necesitas Java 17 o posterior.
    echo Si aparece otro error, conserva el texto de esta ventana para revisarlo.
    pause
)
endlocal
