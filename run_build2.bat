@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "C:\APPS\st244\ST244_Call The Cat Oia Oia Clicker"
gradlew.bat assembleDebug > build_log.txt 2>&1
echo BUILD_DONE exit=%ERRORLEVEL% >> build_log.txt
