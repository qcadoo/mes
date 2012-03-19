#!/bin/bash

echo "Running: mvn jetty:run $@"
MAVEN_OPTS="$MAVEN_OPTS -javaagent:./driver/aspectjweaver-1.6.12.jar" mvn jetty:run $@

