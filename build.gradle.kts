plugins {
    java
    id("org.springframework.boot") version "3.3.2"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.0.20"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.flywaydb.flyway") version "10.17.1"
    kotlin("jvm")
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:10.17.1")
    }
}

group = "org.home"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.flywaydb:flyway-core:10.17.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.17.1")
    implementation("org.postgresql:postgresql")

    implementation("com.github.junrar:junrar:7.5.5")

    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql:1.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
