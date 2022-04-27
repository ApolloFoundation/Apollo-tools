#!/bin/bash
@REM Encrypt url
@echo ***********************************************************************
@echo * This shell script will encrypt data using RSA private key           *
@echo * Double encryption supported. Must be used for update url encryption *
@echo * Parameters:                                                         *
@echo * 1) --hex or -h : - boolean flag that indicates that you want to pass*
@echo *    to encryption not the ordinary string, but hexadecimal (optional)*
@echo * 2) --in or -i:  input file with hexadecimal string of message bytes *
@echo * or just UTF-8 message depending on option --hex defined or not      *
@echo * 3) --key or -k: private key path (absolute). Encrypted keys are not supported    *
@echo ***********************************************************************
@echo off
set DIRP=%~dp0
call %DIRP%\apl-common.bat
@REM start Apollo update URL encryptor
%JAVA_CMD% -jar %APL_TOOLS_JAR% updaterurl %*