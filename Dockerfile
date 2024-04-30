FROM openjdk:21-jdk
LABEL org.opencontainers.image.source=https://github.com/github-insights/github-metrics
VOLUME /tmp
ARG JAR_FILE
COPY app/config/application.yml application.yml
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar", "-Dspring.config.location=/application.yml", "/app.jar"]