import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.ddivad"
version = "0.0.1"
description = "judgebot"

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.ben-manes.versions") version "0.33.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("me.jakejmattson:DiscordKt:0.20.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.1.2")
    implementation("joda-time:joda-time:2.10.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}