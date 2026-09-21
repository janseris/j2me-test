@rem KEmulator nnx64 starter with console window, only for JRE 8!

@echo off
set "jarabs=%~f1"
cd /d "%~dp0"
set dir=%~dp0
for /f "delims=" %%G in ('dir /b "%~dp0..\..\sdk\jdk*"') do set jdkbin=%~dp0..\..\sdk\%%G\bin
set f=%1
if defined f (
"%jdkbin%\java.exe" -Djava.library.path=%dir% -Xmx512M -Dfile.encoding=UTF-8 -jar "%dir%KEmulator.jar" -jar "%jarabs%"
) else (
"%jdkbin%\java.exe" -Djava.library.path=%dir% -Xmx512M -Dfile.encoding=UTF-8 -jar "%dir%KEmulator.jar"
)
pause
