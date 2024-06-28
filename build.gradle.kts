plugins {
	java
	id("org.springframework.boot") version "3.3.1"
	id("io.spring.dependency-management") version "1.1.5"
}
val springModulithVersion by extra("1.2.1")

group = "com.simplecommerce"
version = "1.0.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("com.graphql-java:graphql-java-extended-scalars:22.0")
	implementation("org.springframework.modulith:spring-modulith-starter-core")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.modulith:spring-modulith-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.graphql:spring-graphql-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
dependencyManagement {
	imports {
		mavenBom("org.springframework.modulith:spring-modulith-bom:$springModulithVersion")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
