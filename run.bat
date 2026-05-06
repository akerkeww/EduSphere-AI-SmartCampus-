@echo off
cd /d %~dp0
mvn clean spring-boot:run
pause
