plugins {
    id("simple-commerce.java-conventions")
    id("org.springframework.boot") apply false
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.logback)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.testContainers)
    testImplementation(platform(libs.junitBom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.assertJ)
    testImplementation(libs.springGraphql)
    testImplementation(libs.springWebflux)
    testImplementation(libs.jacksonDatabind)
    testImplementation("com.microsoft.playwright:playwright:1.54.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
