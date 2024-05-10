val wiremockVersion: String by project

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.sonarqube") version "5.0.0.4638"
    id("jacoco-report-aggregation")
}

version = "0.0.1-SNAPSHOT"

allprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    repositories {
        mavenCentral()
    }
    tasks.withType(JavaCompile::class) {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        options.compilerArgs.add("-Werror")
    }

    sonar {
        properties {
            property("sonar.sources", "src/main")
            property("sonar.tests", "src/test")
        }
    }
    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.2")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()

        maxHeapSize = "1G"

        testLogging {
            events("passed")
        }
    }

    tasks.withType<Test> {
        finalizedBy(tasks.withType<JacocoReport>())
    }
    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }
}

dependencies {
    "runtimeOnly"(project(":domain"))
    "runtimeOnly"(project(":github-adapter"))
    "runtimeOnly"(project(":prometheus-exporter"))

    "implementation"("org.springframework.boot:spring-boot-autoconfigure")
    "implementation"("org.springframework.boot:spring-boot")


    "testImplementation"(project(":domain"))
    "testImplementation"(project(":prometheus-exporter"))
    "testImplementation"(project(":github-adapter"))
    "testImplementation"("org.springframework.boot:spring-boot-starter-web")
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.wiremock:wiremock-jetty12:$wiremockVersion")
}

subprojects {

    group = "be.xplore.githubmetrics"


}

sonar {
    properties {
        property("sonar.projectKey", "github-insights_github-metrics")
        property("sonar.organization", "github-insights")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/testCodeCoverageReport/testCodeCoverageReport.xml")
        //property("sonar.qualitygate.wait", "true")
    }
}
