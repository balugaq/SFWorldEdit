plugins {
    java
    alias(libs.plugins.shadow)
}

group = "com.balugaq.sfworldedit"
// version 由 gradle.properties 的 version= 提供，release.yml 也从那里读取。

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    // MetaLib / DisplayModelLib 的原仓库已整站下线，改由 lib/ 下的 JAR 提供。
    // maven("https://repo.metamechanists.org/releases/")
}

dependencies {
    // Core
    compileOnly(libs.paper.api)
    compileOnly(libs.slimefun4)

    // Tools
    compileOnly(libs.guizhan.lib.plugin)
    implementation(libs.bstats)
    compileOnly(libs.findbugs.annotations) {
        exclude("net.jcip", "jcip-annotations")
        exclude("com.google.code.findbugs", "jsr305")
    }
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // 原 Maven 的 compile 依赖，仓库 404 后由本地 JAR 提供，仍需随 shadow 打包
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        // 本机可能跑 JDK 25/26、CI 跑 21，release 统一锁定 Java 21 字节码与 API。
        options.release = 21
        options.compilerArgs.add("-Xlint:-removal")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version.toString())
        }
    }

    shadowJar {
        archiveBaseName.set("SFWorldEdit")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        minimize()

        relocate("org.bstats", "com.balugaq.sfworldedit.bstats")
        exclude("META-INF/*")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }
}

defaultTasks("clean", "build")
