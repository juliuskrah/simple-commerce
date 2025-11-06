plugins {
    java
    id("io.freefair.aspectj")
    id("simple-commerce.java-conventions")
}

description = "aspects for spring security"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.security:spring-security-core:6.5.5")
    implementation("org.aspectj:aspectjrt:1.9.25")
    testImplementation(platform(libs.junitBom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
