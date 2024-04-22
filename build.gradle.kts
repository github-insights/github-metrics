plugins {
    id("org.sonarqube") version "5.0.0.4638"
}


version = "0.0.1-SNAPSHOT"

allprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

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
        //property("sonar.qualitygate.wait", "true")
    }
}

/* FIXME: original from groovy creates aggregate report, dont know how to convert to kotlin.
    Not sure if this is even necessary? Sonar already combines the reports into one.

tasks.register('jacocoRootReport', JacocoReport) {
    description = 'Generates an aggregate report from all subprojects'
    dependsOn(subprojects.test)

    additionalSourceDirs.from = files(subprojects.sourceSets.main.allSource.srcDirs)
    sourceDirectories.from = files(subprojects.sourceSets.main.allSource.srcDirs)
    classDirectories.from = files(subprojects.sourceSets.main.output)
    executionData.from = files(subprojects.jacocoTestReport.executionData)

    reports {
        xml.required = true
    }
}

*/