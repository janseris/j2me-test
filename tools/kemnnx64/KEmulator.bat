@rem KEmulator nnx64 starter, only for JRE 8!

@echo off
set "jarabs=%~f1"
cd /d "%~dp0"
set dir=%~dp0
for /f "delims=" %%G in ('dir /b "%~dp0..\..\sdk\jdk*"') do set jdkbin=%~dp0..\..\sdk\%%G\bin
set f=%1
if defined f (
start "" "%jdkbin%\javaw.exe" -Djava.library.path=%dir% -Xmx512M -Dfile.encoding=UTF-8 -jar "%dir%KEmulator.jar" -jar "%jarabs%"
) else (
start "" "%jdkbin%\javaw.exe" -Djava.library.path=%dir% -Xmx512M -Dfile.encoding=UTF-8 -jar "%dir%KEmulator.jar"
)
