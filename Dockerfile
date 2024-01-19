FROM openjdk:17-jdk
# JAR_FILE 변수 정의 -> 기본적으로 jar file이 2개이기 때문에 이름을 특정한다.

ARG JAR_FILE=./build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]