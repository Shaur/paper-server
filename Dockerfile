FROM bellsoft/liberica-openjre-alpine:22-37-aarch64
COPY build/libs/paper-server-0.0.1-SNAPSHOT.jar paper.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","paper.jar"]