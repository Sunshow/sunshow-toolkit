plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":core-base"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.1")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}