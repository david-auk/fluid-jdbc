plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.9.20"
}

sourceSets {
    named("main") {
        java.srcDirs("src/main/java", "src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
    named("test") {
        java.srcDirs("src/test/java", "src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}

group = "io.github.david-auk"
version = "0.1.0"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
    withJavadocJar()
}
kotlin {
    jvmToolchain(21)
}

tasks.javadoc { enabled = false }     // optional if you only want Dokka HTML

repositories {
    mavenCentral()
}

dependencies {
    api(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")

    // Jackson Databind (ObjectMapper lives here)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    // (Optional) Jackson Annotations — sometimes required for features like @JsonProperty
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
}

tasks.test { useJUnitPlatform() }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("fluid-jdbc")
                description.set("Map classes to tables via annotations, generate generic DAOs, and query in pure Java/Kotlin.")
                url.set("https://github.com/david-auk/fluid-jdbc")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("david-auk")
                        name.set("David Aukes")
                        url.set("https://github.com/david-auk")
                    }
                }
                scm {
                    url.set("https://github.com/david-auk/fluid-jdbc")
                    connection.set("scm:git:https://github.com/david-auk/fluid-jdbc.git")
                    developerConnection.set("scm:git:ssh://git@github.com/david-auk/fluid-jdbc.git")
                }
            }
        }
    }
}

// Sign all publications using in-memory PGP (good for CI)
signing {
    val signingKey = System.getenv("SIGNING_KEY")      // ASCII-armored private key
    val signingPass = System.getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPass)
    sign(publishing.publications)
}