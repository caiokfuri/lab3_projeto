# Imagem da API para a Render (que não tem runtime Java nativo, só Docker).

# Etapa 1: compila o jar com Maven + JDK 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa 2: imagem final só com o JRE e o jar
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 app && chown app /app
COPY --from=build /app/target/moeda-estudantil-*.jar app.jar
USER app

# Instância gratuita da Render: 512 MB e pouca CPU. SerialGC e só o compilador C1 reduzem memória e
# aceleram a inicialização.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
