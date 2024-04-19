plugins {
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4"
}

version = "0.0.1-SNAPSHOT"

dependencies {
    "implementation"(project(":domain"))
    "implementation"("org.springframework:spring-web:6.1.6")
}