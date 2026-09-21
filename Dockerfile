FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src
COPY web ./web
COPY data ./data

RUN mkdir -p out && javac -d out src/PlacementHubServer.java

EXPOSE 8080

CMD ["java", "-cp", "out", "PlacementHubServer"]
