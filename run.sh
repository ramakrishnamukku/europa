#!/bin/bash -e

DEPS_CLASSPATH=`cat target/.classpath`
CLASSPATH=target/classes/:$DEPS_CLASSPATH

$JAVA_HOME/bin/java $JVM_ARGS -cp $CLASSPATH -Duser.timezone=UTC com.distelli.europa.Europa --port 5050 --log-to-console --config EuropaConfig.json $@
