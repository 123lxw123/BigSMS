pluginManagement {
    repositories {
        mavenLocal()
        // 配置Maven仓库地址，以提高依赖下载速度
        // 添加中央仓库的阿里云镜像
        maven("https://maven.aliyun.com/repository/central")
        // 添加公共仓库的阿里云镜像，这个仓库包含了大量开源项目的发布版本
        maven("https://maven.aliyun.com/repository/public")
        // 添加Gradle插件仓库的阿里云镜像，这个仓库主要用于Gradle插件的下载
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        // 添加Apache Snapshots仓库的阿里云镜像，这个仓库用于下载Apache项目的快照版本
        maven("https://maven.aliyun.com/repository/apache-snapshots")
        // 华为开发者仓库
        maven("https://developer.huawei.com/repo/")
        // 腾讯镜像仓库
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        // Jitpack仓库
        maven("https://jitpack.io")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        // 配置Maven仓库地址，以提高依赖下载速度
        // 添加中央仓库的阿里云镜像
        maven("https://maven.aliyun.com/repository/central")
        // 添加公共仓库的阿里云镜像，这个仓库包含了大量开源项目的发布版本
        maven("https://maven.aliyun.com/repository/public")
        // 添加Gradle插件仓库的阿里云镜像，这个仓库主要用于Gradle插件的下载
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        // 添加Apache Snapshots仓库的阿里云镜像，这个仓库用于下载Apache项目的快照版本
        maven("https://maven.aliyun.com/repository/apache-snapshots")
        // 华为开发者仓库
        maven("https://developer.huawei.com/repo/")
        // 腾讯镜像仓库
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
        // Jitpack仓库
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "SMSReader"
include(":app")
 