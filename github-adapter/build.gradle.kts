import org.springframework.boot.gradle.tasks.bundling.BootJar

val wiremockVersion: String by project
val jsonwebtokenVersion: String by project
val commonsCodecVersion: String by project
val bouncyCastleVersion: String by project

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":domain"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.nimbusds:nimbus-jose-jwt:9.38-rc4")

    implementation("org.bouncycastle:bcpkix-jdk18on:$bouncyCastleVersion")

    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")
    testImplementation("org.mockito:mockito-core")

}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
