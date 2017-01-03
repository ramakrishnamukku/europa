#!/bin/bash -e

if [ -z "$EUROPA_CONFIG" ]; then
    echo "You must set the EUROPA_CONFIG environment variable to point to the configuration file." 1>&2
    exit 1
fi

DEPS_CLASSPATH=`cat target/.classpath`
CLASSPATH=target/classes/:$DEPS_CLASSPATH
JVM_ARGS="-Duser.timezone=UTC -Xmx2000M -Xms2000M"
$JAVA_HOME/bin/java $JVM_ARGS -cp $CLASSPATH -Duser.timezone=UTC com.distelli.europa.Europa --stage "$STAGE" --port 5050 --log-to-console --config "$EUROPA_CONFIG" $@
