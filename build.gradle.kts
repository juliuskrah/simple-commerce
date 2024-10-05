plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
}
val springModulithVersion by extra("1.2.1")
val graphQlJavaVersion by extra("22.0")
val springInstrument: Configuration by configurations.creating
val enablePreview = "--enable-preview"
val javaVersion = 21

group = "com.simplecommerce"
version = "1.0.0"

// extra["hibernate.version"] = "6.4.9.Final"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    implementation("com.graphql-java:graphql-java-extended-scalars:$graphQlJavaVersion")
    implementation("com.graphql-java:graphql-java-extended-validation:$graphQlJavaVersion")
    implementation("io.minio:minio:8.5.11")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    springInstrument("org.springframework:spring-instrument") {
        because("Required for Spring Load-Time Weaving")
    }
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.springframework.security:spring-security-aspects")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAndDevelopmentOnly("org.springframework.boot:spring-boot-docker-compose")
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:$springModulithVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(enablePreview)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add(enablePreview)
    options.compilerArgs.add("-Xlint:preview")
    options.release.set(javaVersion)
}

tasks.withType<JavaExec> {
    jvmArgs(enablePreview, "-javaagent:${configurations["springInstrument"].singleFile}")
}
