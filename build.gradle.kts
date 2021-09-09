plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version("7.0.0")
    java
    application
}

application.mainClassName = "net.dranoel.memebot.Main"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("io.ktor:ktor-client-core:1.6.3")
    implementation("io.ktor:ktor-client-apache:1.6.3")
    implementation("io.ktor:ktor-client-gson:1.6.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.21")
    implementation("net.dv8tion:JDA:4.3.0_277")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.dranoel.memebot.Main"
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}