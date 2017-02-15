#!/bin/bash

POM_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2> /dev/null | grep -Ev '(^\[|Download\w+:)')
echo "Building Europa Version: ${POM_VERSION}/${DISTELLI_BUILDNUM}"

printf "package com.distelli.europa;\n\npublic class EuropaVersion\n{\n    public static final String VERSION = \"${POM_VERSION}/${DISTELLI_BUILDNUM}\";\n}" > src/main/java/com/distelli/europa/EuropaVersion.java
