@echo off
set cardAdminConfigFile=./configuration.properties
if not defined java_location (
  set java_location=D:\development\apps\jdk1.8.0_201
)

rem ########################################################

%java_location%\bin\java -jar card-admin.jar