SHELL := /bin/bash
.SILENT:
.PHONY: git-has-pushed git-is-clean assets
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
	mvn -q -DincludeScope=runtime dependency:copy-dependencies package assembly:single
#	yarn run build-all

show-deps:
	mvn dependency:tree

#git-has-pushed:
#	! git diff --stat HEAD origin/master | grep . >/dev/null && [ 0 == $${PIPESTATUS[0]} ]

git-is-clean:
	git diff-index --quiet HEAD --

git-is-master:
	[ master = "$$(git rev-parse --abbrev-ref HEAD)" ]

publish: git-is-clean git-is-master
	. ~/.distelli.config && mvn -Dsurefire.useFile=false -DgenerateBackupPoms=false -DuseReleaseProfile=false -DscmCommentPrefix='[skip ci][release:prepare]' release:prepare release:perform

up-deps:
	mvn versions:use-latest-releases -Dincludes='com.distelli*' -DgenerateBackupPoms=false
