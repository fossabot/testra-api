FROM openjdk:8-jre-alpine
LABEL Testra <testra2018@gmail.com>

ENV MONGO_PORT=27017 \
    MONGO_HOST=mongo \
    MONGO_DATABASE=testra \
    MONGO_USER=testra \
    MONGO_PASSWORD=password

RUN apk update && apk add bash

ADD *.jar spring-boot-app.jar
RUN sh -c 'touch /spring-boot-app.jar'
ENTRYPOINT java -Dspring.data.mongodb.host=$MONGO_HOST -Dspring.data.mongodb.port=$MONGO_PORT -Dspring.data.mongodb.database=$MONGO_DATABASE -Dspring.data.mongodb.username=$MONGO_USER -Dspring.data.mongodb.password=$MONGO_PASSWORD -Djava.security.egd=file:/dev/./urandom -jar /spring-boot-app.jar