#!/bin/bash -e

if [ -z "$EUROPA_CONFIG" ]; then
    EUROPA_CONFIG=EuropaConfig.json
fi

DEPS_CLASSPATH=`cat target/.classpath`
CLASSPATH=target/classes/:$DEPS_CLASSPATH
JVM_ARGS="-Djava.util.logging.config.file=logging.properties -Duser.timezone=UTC -Xmx2000M -Xms2000M"
$JAVA_HOME/bin/java $JVM_ARGS -cp $CLASSPATH -Duser.timezone=UTC com.distelli.europa.Europa --stage "$STAGE" --port 5050 --log-to-console --config "$EUROPA_CONFIG" $@
