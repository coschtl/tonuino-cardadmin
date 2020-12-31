rem @echo off
if not defined java_location (
  set java_location=D:\development\apps\jdk1.8.0_201
)

rem ########################################################

%java_location%\bin\java.exe at.dcosta.tonuino.cardadmin.Main %1 %2 %3 %4