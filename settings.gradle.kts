pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
        gradlePluginPortal()
    }
    resolutionStrategy {
        // 修正点：使用 eachPlugin 闭包
        eachPlugin {
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" ->
                    useVersion("1.9.25")
                "org.jetbrains.kotlin.plugin.spring" ->
                    useModule("org.jetbrains.kotlin:kotlin-allopen:1.9.25")
                "org.springframework.boot" ->
                    useVersion("3.2.4")
                "io.spring.dependency-management" ->
                    useVersion("1.1.3")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "QrServerCloud"
// include("submodule1", "submodule2") // 按需添加子模块
include("gateway-service")
include("auth-service")
