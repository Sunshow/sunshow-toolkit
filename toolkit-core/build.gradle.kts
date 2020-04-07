plugins {
    kotlin("jvm") version "1.3.71"
}

group = "net.sunshow.toolkit"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
allprojects {
    repositories {
        mavenCentral()
    }
}