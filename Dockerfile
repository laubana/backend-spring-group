FROM maven:3.9.9-amazoncorretto-21 as build

WORKDIR /app

COPY ./pom.xml /app
COPY ./src /app/src

RUN mvn dependency:go-offline

RUN mvn clean package -DskipTests

FROM tomcat:10.1.30-jre21

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/backend-spring-event-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD [ "catalina.sh", "run" ]
