import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

val wiremockVersion: String by project
val jsonwebtokenVersion: String by project
val commonsCodecVersion: String by project
val bouncyCastleVersion: String by project
val nimbusVersion: String by project

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")

    implementation("org.bouncycastle:bcpkix-jdk18on:$bouncyCastleVersion")

    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    testImplementation("org.mockito:mockito-core")

    testImplementation("org.awaitility:awaitility:4.2.1")
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
