FROM openjdk:17
EXPOSE 7777
ADD build/libs/ShortURL-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]