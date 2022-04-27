#!/bin/bash
@REM Decrypt url
@echo ***********************************************************************
@echo * This shell script will decrypt data encrypted by RSA private key    *
@echo * Double decryption supported.                                        *
@echo * Must be used for correctness of Update Url encryption               *
@echo * Parameters:                                                         *
@echo * 1) --key or -k: path to certificate to use for decryption (absolute)*
@echo * 2) --in or -i: path to input file with encrypted hexadecimal string *
@echo * 3) --utf or -u: boolean flag that indicates that you want to convert*
@echo * decrypted bytes to UTF-8 string                                     *
@echo ***********************************************************************
@echo off
set DIRP=%~dp0
call %DIRP%\apl-common.bat
@REM start Apollo update URL decryptor
%JAVA_CMD% -jar %APL_TOOLS_JAR% updaterurl --decrypt %*
