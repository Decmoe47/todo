plugins {
    val kotlinVersion = "2.3.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.devtools.ksp") version "2.3.0"
}

group = "com.decmoe47"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // DB
    val komapperVersion = "5.7.0"
    implementation("org.komapper:komapper-spring-boot-starter-jdbc:$komapperVersion")
    implementation("org.komapper:komapper-dialect-mysql-jdbc:$komapperVersion")
    ksp("org.komapper:komapper-processor:$komapperVersion")
    compileOnly("com.mysql:mysql-connector-j")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // tool
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.7.1")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.3")

    // dev tool
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test:7.0.2")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    val kotestVersion = "6.0.7"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.mockk:mockk:1.14.2")
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}

// for SpEl in @PreAuthorize
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
