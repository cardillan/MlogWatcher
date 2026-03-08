@echo off
set JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.6.10-hotspot"
call gradlew.bat jar
copy build\libs\MlogWatcher7Desktop.jar "%APPDATA%\Mindustry\mods\MlogWatcher7Desktop.jar" /y