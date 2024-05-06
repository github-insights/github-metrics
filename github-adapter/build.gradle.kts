import org.springframework.boot.gradle.tasks.bundling.BootJar

val wiremockVersion: String by project

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
