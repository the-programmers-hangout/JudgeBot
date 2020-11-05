
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.ddivad"
version = Versions.BOT
description = "judgebot"

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:${Versions.DISCORDKT}")
    implementation("org.litote.kmongo:kmongo-coroutine:4.1.3")
    implementation("joda-time:joda-time:2.10.6")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
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

object Versions {
    const val BOT = "1.0.0"
    const val DISCORDKT = "0.21.3"
}