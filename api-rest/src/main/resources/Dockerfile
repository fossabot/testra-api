FROM openjdk:8-jre-alpine

ENV MONOGO_URL mongodb://mongo:27017/testra

RUN apk update && apk add bash

ADD *.jar spring-boot-app.jar
RUN sh -c 'touch /spring-boot-app.jar'
ENTRYPOINT java -Dspring.data.mongodb.uri=$MONOGO_URL -Djava.security.egd=file:/dev/./urandom -jar /spring-boot-app.jar