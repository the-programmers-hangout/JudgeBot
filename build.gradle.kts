import java.util.*

group = "me.ddivad"
version = "2.0.0-BETA-1"
description = "A bot for managing discord infractions in an intelligent and user-friendly way."

plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.23.4")
    implementation("org.litote.kmongo:kmongo-coroutine:4.7.0")
    implementation("org.slf4j:slf4j-simple:1.7.30")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"

        Properties().apply {
            setProperty("name", "Judgebot")
            setProperty("description", project.description)
            setProperty("version", version.toString())
            setProperty("url", "https://github.com/the-programmers-hangout/JudgeBot/")

            store(file("src/main/resources/bot.properties").outputStream(), null)
        }
    }

    shadowJar {
        archiveFileName.set("Judgebot.jar")
        manifest {
            attributes(
                "Main-Class" to "me.ddivad.judgebot.MainKt"
            )
        }
    }
}