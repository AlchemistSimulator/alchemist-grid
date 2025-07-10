plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
}

group = "it.unibo.alchemist"

repositories {
    mavenCentral()
}

multiJvm {
    jvmVersionForCompilation = 17
}

dependencies {
    compileOnly(libs.spotbugs.annotations)

    api(libs.ignite.core)

    implementation(libs.alchemist)
    implementation(libs.apache.commons.io)
    implementation(libs.guava)
    implementation(libs.ignite.spring)
    implementation(libs.ignite.indexing)
    implementation(libs.kotlin.stdlib)
    implementation(libs.resourceloader)
    implementation(libs.slf4j)

    testImplementation(libs.bundles.kotlin.testing)
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(
            *org.gradle.api.tasks.testing.logging.TestLogEvent
                .values(),
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

signing {
    if (System.getenv("CI") == "true") {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

publishOnCentral {
    repoOwner = "AlchemistSimulator"
    projectLongName.set("Template Kotlin JVM Project")
    projectDescription.set("A template repository for Kotlin JVM projects")
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    developers {
                        developer {
                            name.set("Danilo Pianini")
                            email.set("danilo.pianini@gmail.com")
                            url.set("http://www.danilopianini.org/")
                        }
                    }
                }
            }
        }
    }
}

publishing.publications {
    withType<MavenPublication> {
        pom {
            developers {
                developer {
                    name.set("Matteo Magnani")
                    email.set("matteo.magnani18@studio.unibo.it")
                }
                developer {
                    name.set("Danilo Pianini")
                    email.set("danilo.pianini@unibo.it")
                }
            }
        }
    }
}
