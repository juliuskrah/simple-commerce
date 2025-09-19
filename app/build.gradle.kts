import com.google.protobuf.gradle.id

plugins {
    id("simple-commerce.java-conventions")
    id("org.springframework.boot")
    id("com.google.protobuf")
    antlr
    application
}

val antlrVersion by extra("4.13.2")
val graphQlJavaVersion by extra("22.0")
val jSpecifyVersion by extra("1.0.0")

val springInstrument: Configuration by configurations.creating
val mockitoAgent: Configuration by configurations.creating

val enablePreview = "--enable-preview"
val javaVersion: Provider<Int> = providers
    .gradleProperty("javaVersion")
    .map(String::toInt)

extra["springModulithVersion"] = "1.4.1"
extra["springStatemachineVersion"] = "4.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.statemachine:spring-statemachine-starter")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    implementation("com.graphql-java:graphql-java-extended-scalars:$graphQlJavaVersion")
    implementation("com.graphql-java:graphql-java-extended-validation:$graphQlJavaVersion")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jspecify:jspecify:$jSpecifyVersion")
    implementation(project(":minio-docker-compose"))
    implementation(libs.picocli)
    implementation(libs.minio)
    implementation(libs.moneta)
    implementation("org.antlr:antlr4-runtime:$antlrVersion")
    antlr("org.antlr:antlr4:$antlrVersion")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    springInstrument("org.springframework:spring-instrument") {
        because("Required for Spring Load-Time Weaving")
    }
    mockitoAgent(libs.mockito) {
        because("Required for inline mocking")
        isTransitive = false
    }
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.springframework.security:spring-security-aspects")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.grpc:spring-grpc-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.google.protobuf:protobuf-java-util")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAndDevelopmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
        mavenBom("org.springframework.statemachine:spring-statemachine-bom:${property("springStatemachineVersion")}")
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(enablePreview, "-javaagent:${mockitoAgent.asPath}")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add(enablePreview)
    options.compilerArgs.add("-Xlint:preview")
    options.release.set(javaVersion)
}

tasks.withType<JavaExec> {
    jvmArgs(enablePreview, "-javaagent:${configurations["springInstrument"].singleFile}")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-listener")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}
application {
    mainClass.set("com.simplecommerce.SimpleCommerceApplication")
    applicationDefaultJvmArgs = listOf(
        enablePreview,
        "-javaagent:${configurations["springInstrument"].singleFile}"
    )
}
