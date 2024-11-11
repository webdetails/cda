# ******************************************************************************
#
# Pentaho
#
# Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
#
# Use of this software is governed by the Business Source License included
# in the LICENSE.TXT file.
#
# Change Date: 2028-08-13
# ******************************************************************************


###
# setting JVM mem for current shell and all processes started from current shell
# TODO parameterize xms/xmx values
###
export PENTAHO_DI_JAVA_OPTIONS="-Xms512m -Xmx1024m"
echo "[PDI-CONTENT-EXECUTOR-JOB] PENTAHO_DI_JAVA_OPTIONS: $PENTAHO_DI_JAVA_OPTIONS"

echo "[CDA] REPOS_URL: $REPOS_URL"
echo "[CDA] REPOS_USER: $REPOS_USER"
echo "[CDA] REPOS_PASSWORD: $REPOS_PASSWORD"

export OPT="$OPT -Drepos.url=$REPOS_URL -Drepos.user=$REPOS_USER -Drepos.password=$REPOS_PASSWORD"

INITIALDIR="`pwd`"
BASEDIR="`dirname $0`"
cd "$BASEDIR"
DIR="`pwd`"
cd - > /dev/null

if [ "$1" = "-x" ]; then
  set LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASEDIR/lib
  export LD_LIBRARY_PATH
  export OPT="-Xruntracer $OPT"
  shift
fi

"$DIR/spoon.sh" -main org.pentaho.di.scale.Scale "$@"
