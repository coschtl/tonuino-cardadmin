@echo off
set cardAdminConfigFile=./configuration.properties

if not defined java_location (
	if not defined java_home (
		set java_location=D:\development\apps\jdk1.8.0_201		
	) else (
		set java_location=%java_home%
	)
)

rem ########################################################

%java_location%\bin\java -jar card-admin.jar