plugins {
    id("org.sonarqube") version "5.0.0.4638"
    jacoco
}


version = "0.0.1-SNAPSHOT"


allprojects {
    repositories {
        mavenCentral()
    }
}



subprojects {
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    apply(plugin = "java")
    apply(plugin = "jacoco")

    group = "be.xplore.githubmetrics"

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

sonar {
    properties {
        property("sonar.projectKey", "github-insights_github-metrics")
        property("sonar.organization", "github-insights")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        //property("sonar.qualitygate.wait", "true")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn(":app:compileJava")
    dependsOn(":app:compileTestJava")
    dependsOn(":app:test")
    dependsOn(":app:processTestResources")
    dependsOn(":domain:compileTestJava")
    dependsOn(":domain:test")
    dependsOn(":github-adapter:compileTestJava")
    dependsOn(":github-adapter:test")
    dependsOn(":prometheus-exporter:compileTestJava")
    dependsOn(":prometheus-exporter:test")
    group = "Reporting"
    description = "Generate Jacoco coverage reports"

    reports {
        csv.required = false
        html.required = true
        xml.required = true
    }

    sourceDirectories.setFrom(
            fileTree("${project.projectDir}/app/src/main"),
            fileTree("${project.projectDir}/domain/src/main"),
            fileTree("${project.projectDir}/prometheus-exporter/src/main"),
            fileTree("${project.projectDir}/github-adapter/src/main"),
    )

    classDirectories.setFrom(
            fileTree("${project.projectDir}/app/build/classes"),
            fileTree("${project.projectDir}/domain/build/classes"),
            fileTree("${project.projectDir}/prometheus-exporter/build/classes"),
            fileTree("${project.projectDir}/github-adapter/build/classes")
    )
    executionData.setFrom(
            fileTree(project.projectDir) {
                setIncludes(setOf("**/**/*.exec", "**/**/*.ec"))
            }
    )
}