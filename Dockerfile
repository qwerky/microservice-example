FROM azul/zulu-openjdk:17

COPY target/microservice-example-0.0.1-SNAPSHOT.jar microservice-example.jar

CMD ["java", "-jar", "microservice-example.jar"]