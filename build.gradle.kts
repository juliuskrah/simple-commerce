plugins {
    id("io.spring.dependency-management")
}

extra["springModulithVersion"] = "1.3.1"
extra["springStatemachineVersion"] = "4.0.0"

allprojects {
    group = "com.simplecommerce"
    version = "1.0.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
}



repositories {
    mavenCentral()
}
