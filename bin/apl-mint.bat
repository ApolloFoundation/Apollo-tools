@REM Start mint worker
@echo ***********************************************************************
@echo * This batch file will start Apollo minting worker for                *
@echo * mintable currencies   						                        *
@echo * Take a look at conf/apl-mint.properties section for detailed config *
@echo ***********************************************************************
@echo off
set DIRP=%~dp0
call %DIRP%\apl-common.bat
@REM start Apollo
%JAVA_CMD% -jar %APL_TOOLS_JAR% --mint %*
