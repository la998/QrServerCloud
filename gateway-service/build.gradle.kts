plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
}
group = "com.qr"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery:2023.0.1.0")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-sentinel:2023.0.1.0")
    // 响应式 Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Sentinel网关适配器（关键）
    implementation("com.alibaba.csp:sentinel-spring-cloud-gateway-adapter:1.8.6")
    // Sentinel响应式支持
    implementation("com.alibaba.csp:sentinel-reactor-adapter:1.8.6")
    // Sentinel传输层（用于连接Dashboard）
    implementation("com.alibaba.csp:sentinel-transport-simple-http:1.8.6")

    // Kotlin 相关
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}