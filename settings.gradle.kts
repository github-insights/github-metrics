plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "github-metrics"

include(
        "app",
        "domain",
        "prometheus-exporter",
        "github-adapter"
)
