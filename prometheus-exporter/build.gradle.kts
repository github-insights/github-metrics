import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

val wiremockVersion: String by project
val togglzVersion: String by project

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":domain"))
    implementation("org.springframework:spring-context")

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-aspects")
    implementation("org.togglz:togglz-spring-boot-starter:$togglzVersion")
    implementation("org.togglz:togglz-spring-security:$togglzVersion")


    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    testImplementation("org.mockito:mockito-core")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

tasks.getByName<BootBuildImage>("bootBuildImage") {
    enabled = false
}