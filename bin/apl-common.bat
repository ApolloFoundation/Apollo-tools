@echo off
@REM common functions for Apollo scripts
@echo *********** APOLLO common **********
set MIN_JAVA=110
set apl_bin=%~dp0
call :getTop "%apl_bin%"
goto cont1
:getTop
for %%i in ("%~dp0..") do set "APL_TOP=%%~fi"
:cont1

if exist "%APL_TOP%\jre" (
	set JAVA_HOME="%APL_TOP%\jre"
	set JAVA_CMD="%APL_TOP%\jre\bin\java"
	set JAR_CMD="%APL_TOP%\jre\bin\jar"
) else (
	set JAVA_CMD=java
	set JAR_CMD=jar
)

@REM determine Java version
PATH %JAVA_HOME%\bin\;%PATH%
for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set "java_ver=%%j%%k"

if  %java_ver% LSS %MIN_JAVA% (
   @echo WARNING! Java version is less then 11. Programs will not work!
) else (
   @echo Java version is OK.
)



@REM are we in dev env or in production
if exist "%APL_TOP%\apl-tools-*.jar" (
	set APL_LIB="%APL_TOP%\lib"
	set IN_DEV=false
) else (
	set APL_LIB="%APL_TOP%\target\lib"
	set IN_DEV=true
)

@echo APL_LIB = %APL_LIB%
if exist "%APL_TOP%\VERSION" (
	echo Version file exist
	set /p APL_VER=<"%APL_TOP%\VERSION"
	echo %APL_VER%
) else (
    @REM calculate version by parsing path
    exit /B 5
)

if %IN_DEV%==true (
	ECHO "in dev True"
        set APL_TOOLS_JAR="%APL_TOP%\target\apl-tools-%APL_VER%.jar"
) else (
	set APL_TOOLS_JAR="%APL_TOP%\apl-tools-%APL_VER%.jar"
)
@echo Apollo Tools Version:
@echo %APL_VER%
