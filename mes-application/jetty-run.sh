#!/bin/bash

echo "Running: mvn jetty:run $@"
MAVEN_OPTS="$MAVEN_OPTS -javaagent:./driver/aspectjweaver-1.8.2.jar" mvn jetty:run $@

