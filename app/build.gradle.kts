val wiremockVersion: String by project

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":domain"))
    implementation(project(":github-adapter"))
    implementation(project(":prometheus-exporter"))

    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot")


    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.wiremock:wiremock-jetty12:$wiremockVersion")

}