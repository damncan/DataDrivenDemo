FROM openjdk:18-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} flink.jar
ENV JAVA_OPTS="--add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens java.base/jdk.internal.module=ALL-UNNAMED"
ENTRYPOINT java $JAVA_OPTS -jar /flink.jar