FROM bellsoft/liberica-openjdk-alpine-musl:22-37-aarch64 AS builder
FROM jenkins/inbound-agent:latest-alpine-jdk21

USER root

RUN apk update
RUN apk add docker helm

COPY --from=builder /usr/lib/jvm/jdk-22-bellsoft-aarch64 /usr/lib/jvm/jdk-22-bellsoft-aarch64
ENV JAVA_HOME=/usr/lib/jvm/jdk-22-bellsoft-aarch64

USER jenkins

ENTRYPOINT ["/usr/local/bin/jenkins-agent"]