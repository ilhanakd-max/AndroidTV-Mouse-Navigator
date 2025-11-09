#!/usr/bin/env sh

APP_HOME=$(cd "$(dirname "$0")"; pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "Gradle wrapper JAR eksik. Lütfen 'gradle wrapper' komutunu çalıştırarak oluşturun veya projeyi gradle ile çalıştırın." >&2
  exit 1
fi

JAVA_CMD="java"
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

exec "$JAVA_CMD" $JAVA_OPTS $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
