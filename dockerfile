FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS="-Dlog4j2.debug"
ENTRYPOINT ["java","-jar","/app.jar"]
