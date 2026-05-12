FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY gdoc-common/pom.xml gdoc-common/pom.xml
COPY gdoc-model/pom.xml gdoc-model/pom.xml
COPY gdoc-security/pom.xml gdoc-security/pom.xml
COPY gdoc-user/pom.xml gdoc-user/pom.xml
COPY gdoc-document/pom.xml gdoc-document/pom.xml
COPY gdoc-collaboration/pom.xml gdoc-collaboration/pom.xml
COPY gdoc-history/pom.xml gdoc-history/pom.xml
COPY gdoc-social/pom.xml gdoc-social/pom.xml
COPY gdoc-server/pom.xml gdoc-server/pom.xml
RUN mvn dependency:go-offline -B

COPY . .
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/gdoc-server/target/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
