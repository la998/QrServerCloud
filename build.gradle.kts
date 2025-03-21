plugins {
    kotlin("jvm") version "1.9.25" apply false
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    group = "com.qr"
    version = "1.0.0"

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(19))
        }
    }

    repositories {
        // 国内镜像优先
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/spring")
        mavenCentral()
    }

    dependencies {
        // Kotlin 反射支持
        "implementation"(kotlin("reflect"))
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "19"
        }
    }

    // 依赖管理配置
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            // Spring Boot BOM
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.4")
            // Spring Cloud Alibaba BOM
            mavenBom("com.alibaba.cloud:spring-cloud-alibaba-dependencies:2023.0.1.0")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.1")
        }
    }
}