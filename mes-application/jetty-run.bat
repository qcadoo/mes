echo Running: mvn jetty:run %*

set OLD_MAVEN_OPTS=%MAVEN_OPTS%
set MAVEN_OPTS=%MAVEN_OPTS% -javaagent:./driver/aspectjweaver-1.8.2.jar

mvn jetty:run %*

set MAVEN_OPTS=%OLD_MAVEN_OPTS%
set OLD_MAVEN_OPTS=
