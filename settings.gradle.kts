pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.springframework.boot") version providers.gradleProperty("springBootPluginVersion").get()
        id("io.spring.dependency-management") version providers.gradleProperty("dependencyManagementPluginVersion")
        id("com.google.protobuf") version providers.gradleProperty("protobufPluginVersion")
    }
}
rootProject.name = "simple-commerce"
include("app", "minio-docker-compose", "integration-test")
