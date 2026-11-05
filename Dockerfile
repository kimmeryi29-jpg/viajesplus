# Etapa 1: compilación con Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: solo el JRE y el JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S viajes && adduser -S viajes -G viajes
COPY --from=build /app/target/app.jar app.jar
USER viajes
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=70 -XX:TieredStopAtLevel=1 -Xss256k"
EXPOSE 8080
# En Render la base llega por partes (DB_HOST, DB_PORT, DB_NAME); aquí se arma la URL JDBC.
ENTRYPOINT ["sh", "-c", "if [ -n \"$DB_HOST\" ]; then export SPRING_DATASOURCE_URL=jdbc:postgresql://$DB_HOST:${DB_PORT:-5432}/$DB_NAME; fi; exec java $JAVA_OPTS -jar app.jar"]
