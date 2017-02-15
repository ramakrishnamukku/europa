SHELL := /bin/bash
.SILENT:
.PHONY: git-has-pushed git-is-clean assets
POM_VERSION=$(shell mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2> /dev/null | grep -Ev '(^\[|Download\w+:)')

all:
	mvn -q -U dependency:build-classpath compile -DincludeScope=runtime -Dmdep.outputFile=target/.classpath -Dmaven.compiler.debug=false

assets:
	yarn run build-all

install:
	mvn -q install

test:
	mvn -q -Dsurefire.useFile=false test

clean:
	mvn -q clean

package:
	echo "Building Europa Version: ${POM_VERSION}/${DISTELLI_BUILDNUM}"
	printf "package com.distelli.europa;\n\npublic class EuropaVersion\n{\n    public static final String VERSION = \"${POM_VERSION}/${DISTELLI_BUILDNUM}\";\n}" > src/main/java/com/distelli/europa/EuropaVersion.java
	mvn -q -DincludeScope=runtime dependency:copy-dependencies package assembly:single

show-deps:
	mvn dependency:tree

#git-has-pushed:
#	! git diff --stat HEAD origin/master | grep . >/dev/null && [ 0 == $${PIPESTATUS[0]} ]

git-is-clean:
	git diff-index --quiet HEAD --

git-is-master:
	[ master = "$$(git rev-parse --abbrev-ref HEAD)" ]

publish: git-is-clean git-is-master
	mvn -Dsurefire.useFile=false -DgenerateBackupPoms=false -DuseReleaseProfile=false -DscmCommentPrefix='[skip ci][release:prepare]' release:prepare release:perform && \
	git push --follow-tags

up-deps:
	mvn versions:use-latest-releases -Dincludes='com.distelli*' -DgenerateBackupPoms=false
