#!/bin/bash
@REM Decrypt url
@echo ***********************************************************************
@echo * This shell script will encrypt data using RSA private key           *
@echo * Double encryption supported. Program will launch in interactive mode*
@echo * by default. You can pass parameters to the executable class to      *
@echo * disable interactive mode. Use case - encryption of updater urls.    *
@echo * Parameters order is important:                                      *
@echo * a isHexadecimal - boolean flag that indicates that you want to pass *
@echo *    to encryption not the ordinary string, but hexadecimal           *
@echo * b) hexadecimal string of message bytes or just message depending    *
@echo *    on option isHexadecimal                                          *
@echo ***********************************************************************
@echo off
set DIRP=%~dp0
call %DIRP%\apl-common.bat
@REM start Apollo update URL decryptor
%JAVA_CMD% -jar %APL_TOOLS_JAR% updaterurl --encrypt %*