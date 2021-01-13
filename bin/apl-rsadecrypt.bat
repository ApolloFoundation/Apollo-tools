#!/bin/bash
@REM Decrypt url
@echo ***********************************************************************
@echo * This batch script will decrypt data encrypted by RSA private key    *
@echo * Double decryption supported. Program will launch in interactive mode*
@echo * by default. You can pass parameters to the executable class to      *
@echo * disable interactive mode. Use case - decryption of updater urls.    *
@echo * Parameters order is important:                                      *
@echo * a certificate path absolute                                         *
@echo * b hexadecimal string of encrypted message bytes                     *
@echo * c boolean flag that indicates that you want to convert decrypted    *
@echo * bytes real to real UTF-8 string                                     *
@echo ***********************************************************************
@echo off
set DIRP=%~dp0
call %DIRP%\apl-common.bat
@REM start Apollo update URL decryptor
%JAVA_CMD% -jar %APL_TOOLS_JAR% updaterurl --decrypt %*
