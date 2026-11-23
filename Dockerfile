FROM eclipse-temurin:22-jre
RUN useradd -u 10001 -r -s /usr/sbin/nologin paper
COPY build/libs/paper-server-0.0.1-SNAPSHOT.jar paper.jar
EXPOSE 8080
USER 10001
ENTRYPOINT ["java","-jar","paper.jar"]