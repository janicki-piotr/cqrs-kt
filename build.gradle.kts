import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco
    kotlin("jvm") version "1.6.10"
    id("org.sonarqube") version "3.3"
    id("maven-publish")
}

group = "pl.redny"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
    }
}

tasks.sonarqube {
    dependsOn(tasks.named("jacocoTestReport"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

sonarqube {
    properties {
        property("sonar.projectKey", "janicki-piotr_cqrs-kt")
        property("sonar.organization", "pcpiotr-github")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.core.codeCoveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/janicki-piotr/cqrs-kt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}