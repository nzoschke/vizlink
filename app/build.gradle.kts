application { mainClass = "vizlink.AppKt" }

dependencies {
  implementation("com.google.guava:guava:33.3.1-jre")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
  implementation("org.deepsymmetry:beat-link:8.0.0-SNAPSHOT")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0-RC")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
  implementation("org.slf4j:slf4j-api:2.0.11")
  implementation("org.slf4j:slf4j-simple:2.0.11")
  implementation("net.jthink:jaudiotagger:3.0.1")

  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

graalvmNative {
  binaries.all {
    buildArgs.add("--strict-image-heap")
    buildArgs.add("-Djava.awt.headless=true")
    imageName.set("vizlink")
  }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

plugins {
  application
  id("org.jetbrains.kotlin.jvm") version "2.0.21"
  id("org.graalvm.buildtools.native") version "0.10.4"
  kotlin("plugin.serialization") version "2.0.21"
}

repositories {
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  mavenCentral()
}

tasks.named<JavaExec>("run") { standardInput = System.`in` }

tasks.named<Test>("test") { useJUnitPlatform() }
