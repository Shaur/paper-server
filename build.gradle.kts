plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("org.jetbrains.kotlin.plugin.spring") version "2.2.20"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.2.20"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "10.17.1"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.2.20"

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

val mockitoAgent: Configuration = configurations.create("mockitoAgent")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")

    implementation("org.flywaydb:flyway-core:10.17.1")
    implementation("org.flywaydb:flyway-database-postgresql:10.17.1")
    implementation("org.postgresql:postgresql")

    implementation("com.github.junrar:junrar:7.5.5")
    implementation("org.apache.commons:commons-text:1.12.0")

    implementation("com.squareup.okhttp3:okhttp:5.2.1")

    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.github.oshai:kotlin-logging:7.0.3")

    //Image scaler
    implementation("org.imgscalr:imgscalr-lib:4.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent("org.mockito:mockito-core:5.15.2") { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}
