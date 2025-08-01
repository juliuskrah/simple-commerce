val springBootVersion: String by settings
pluginManagement {
    plugins {
        id("org.springframework.boot") version "3.5.4"
        id("io.spring.dependency-management") version "1.1.7"
    }
}
rootProject.name = "simple-commerce"
include("app", "minio-docker-compose")
