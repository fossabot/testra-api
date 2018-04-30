FROM openjdk:8-jre-alpine
ADD *.jar spring-boot-app.jar
RUN sh -c 'touch /spring-boot-app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/spring-boot-app.jar"]