@rem KEmulator nnx64 starter with console window, only for JRE 8!

@echo off
set dir=%~dp0
for /f "delims=" %%G in ('dir /b "%~dp0..\..\sdk\jdk*"') do set jdkbin=%~dp0..\..\sdk\%%G\bin
set f=%1
if defined f (
"%jdkbin%\java.exe" -Djava.library.path=%dir% -Xmx512M -Dfile.encoding=UTF-8 -jar "%dir%KEmulator.jar" -jar "%1"
) else (
"%jdkbin%\java.exe" -Djava.library.path=%dir% -Xmx512M -Dfile.encoding=UTF-8 -jar "%dir%KEmulator.jar"
)
pause
