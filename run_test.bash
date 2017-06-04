#!/usr/bin/env bash

set -eo pipefail

JAVA=java
JAVAC=javac

JDBC_URL="jdbc:postgresql://localhost/test?user=test&password=password"
if [ -z $1 ]; then
  echo "JDBC URL not provideded, using the default of \"$JDBC_URL\""
else
  JDBC_URL=$1
fi

MYDIR=`dirname $0`

echo "******************* JDBC Batch insert performance tester *******************"
echo

mkdir -p ${MYDIR}/classes
COMPILE_CMD="$JAVAC ${MYDIR}/src/Main.java -d ${MYDIR}/classes"
echo "Compiling with $COMPILE_CMD"
$COMPILE_CMD

function runTest() {
  POSTGRES_DRIVER_JAR=$1
  echo "--------"
  echo "Running test with $POSTGRES_DRIVER_JAR :"
  time $JAVA -cp ${POSTGRES_DRIVER_JAR}:classes Main $JDBC_URL
}

for DRIVER in ${MYDIR}/lib/postgres*.jar; do
  runTest $DRIVER
done

