FROM    openjdk:17

LABEL   author="Roza Chatzigeorgiou"

VOLUME  /tmp

ENV PORT=8080

EXPOSE  $PORT

ARG JAR_FILE=target/money-transfer-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT  ["java","-jar","/app.jar"]