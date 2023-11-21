FROM    openjdk:17

LABEL   author="Roza Chatzigeorgiou"

VOLUME  /tmp

ENV PORT=8080

EXPOSE  $PORT

EXPOSE 5005

ARG JAR_FILE=target/money-transfer-0.0.1-SNAPSHOT.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT  ["java","-jar","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005","/app.jar"]