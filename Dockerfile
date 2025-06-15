FROM maven:3.9.9-amazoncorretto-21 as builder

WORKDIR /app

COPY ./pom.xml /app

RUN mvn dependency:go-offline -B

COPY ./src /app/src

RUN mvn -e clean package -DskipTests -B

FROM tomcat:10.1.30-jre21

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
