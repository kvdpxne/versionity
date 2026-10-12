plugins {
  idea
  java
  `java-library`
  signing
  `maven-publish`
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()
description = providers.gradleProperty("description").get()

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

dependencies {
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks {

  test {
    useJUnitPlatform()
  }

  compileJava {
    options.encoding = "UTF-8"
    options.release.set(25)
  }

  val sourcesJar = register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
  }

  val javadocJar = register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(javadoc)
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifact(tasks.named("sourcesJar"))
      artifact(tasks.named("javadocJar"))

      pom {
        name.set(rootProject.name)
        description.set(project.description)
        url.set(providers.gradleProperty("url").get())

        licenses {
          license {
            name.set(providers.gradleProperty("license.name").get())
            url.set(providers.gradleProperty("license.url").get())
          }
        }
        developers {
          developer {
            id.set(providers.gradleProperty("developer.id").get())
            name.set(providers.gradleProperty("developer.name").get())
          }
        }
        scm {
          connection.set(providers.gradleProperty("scm.connection").get())
          developerConnection.set(providers.gradleProperty("scm.developerConnection").get())
          url.set(providers.gradleProperty("scm.url").get())
        }
      }
    }
  }
}

signing {
  // Sign only when credentials are available to avoid failing local builds.
  val signingKey = providers.gradleProperty("signing.secretKey").orNull
  val signingPassword = providers.gradleProperty("signing.password").orNull

  if (signingKey != null && signingPassword != null) {
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
  }
}