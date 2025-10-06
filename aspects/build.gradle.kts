plugins {
    java
    id("io.freefair.aspectj")
}

description = "aspects for spring security"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.security:spring-security-core:6.5.5")
    implementation("org.aspectj:aspectjrt:1.9.24")
    testImplementation(platform(libs.junitBom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
