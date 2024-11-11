@echo off
REM ******************************************************************************
REM
REM Pentaho
REM
REM Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
REM
REM Use of this software is governed by the Business Source License included
REM in the LICENSE.TXT file.
REM
REM Change Date: 2028-08-13
REM ******************************************************************************

setlocal

set OPT=%OPT% "-Drepos.url=%REPOS_URL%" "-Drepos.user=%REPOS_USER%" "-Drepos.password=%REPOS_PASSOWORD%"

SET initialDir=%cd%
pushd %~dp0
SET STARTTITLE="Scale"
SET SPOON_CONSOLE=1
call Spoon.bat -main org.pentaho.di.scale.Scale -initialDir "%initialDir%"\ %*
popd
