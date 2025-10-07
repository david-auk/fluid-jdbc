import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("org.jetbrains.dokka") version "1.9.20"
    signing
}

sourceSets {
    named("main") {
        java.srcDirs("src/main/java", "src/main/kotlin")
        resources.srcDirs("src/main/resources")
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
tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

mavenPublishing {
    // Set coordinates for the artifact
    coordinates(group.toString(), "fluid-jdbc", version.toString())
    configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaHtml")))

    // Publish to Sonatype Central Portal
    publishToMavenCentral()

    // Sign all publications when SIGNING_KEY / SIGNING_PASSWORD are present (handled by plugin)
    signAllPublications()

    // POM metadata
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

// Configure signing keys from environment variables (for CI)
val envSigningKey: String? = System.getenv("SIGNING_KEY")
val envSigningPass: String? = System.getenv("SIGNING_PASSWORD")
if (!envSigningKey.isNullOrBlank()) {
    // The Vanniktech plugin applies the Signing plugin; we just feed it the key
    signing {
        useInMemoryPgpKeys(envSigningKey, envSigningPass)
    }
} else {
    logger.lifecycle("PGP signing not configured from env; if this is CI, set SIGNING_KEY and SIGNING_PASSWORD secrets.")
}

// Avoid publishing two javadoc JARs: we let Vanniktech produce the single javadocJar from Dokka HTML
tasks.matching { it.name == "dokkaJavadocJar" }.configureEach { enabled = false }