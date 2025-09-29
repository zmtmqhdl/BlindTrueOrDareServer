plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.5")
    implementation("io.ktor:ktor-server-netty:2.3.5")
    implementation("io.ktor:ktor-server-websockets:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Exposed + Postgres 최신 버전만 사용
//    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
//    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
//    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
//    implementation("org.jetbrains.exposed:exposed-java-time:0.50.1")
//    implementation("org.postgresql:postgresql:42.7.3")
//    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.slf4j:slf4j-simple:2.0.12")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("org.example.MainKt")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    shadowJar {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        archiveVersion.set("")

        manifest {
            attributes["Main-Class"] = "org.example.MainKt"
        }
    }
}