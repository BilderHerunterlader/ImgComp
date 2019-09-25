@echo off

set cl=
:params
if "%1"=="" goto :launch
set cl=%cl% %1
shift
goto :params


:launch
java -Xms64m -Xmx1024m -jar ImgComp.jar %cl%
