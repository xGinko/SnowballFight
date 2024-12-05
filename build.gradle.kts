plugins {
    java
    id("com.gradleup.shadow") version ("8.3.2")
    id("io.papermc.paperweight.userdev") version ("1.7.4")
    id("xyz.wagyourtail.jvmdowngrader") version ("1.2.0")
}

group = "me.xginko"
version = "1.3.2"
description = "Make snowball fights fun and engaging."

repositories {
    mavenCentral()

    maven("https://ci.pluginwiki.us/plugin/repository/everything/") {
        name = "configmaster-repo"
    }

    maven("https://repo.codemc.io/repository/maven-releases/") {
        name = "codemc-repo"
    }

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }

    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }

    maven("https://mvn-repo.arim.space/lesser-gpl3/") {
        name = "arim-mvn-lgpl3"
    }
}

dependencies {
    paperweight.foliaDevBundle("1.20.6-R0.1-SNAPSHOT");
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")

    implementation("com.github.thatsmusic99:ConfigurationMaster-API:v2.0.0-rc.1")
    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")
    implementation("org.reflections:reflections:0.10.2")

    implementation("space.arim.morepaperlib:morepaperlib:0.4.3")
    implementation("com.github.cryptomorin:XSeries:11.3.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-serializer-ansi:4.17.0")
    implementation("net.kyori:adventure-text-logger-slf4j:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    build.configure {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveFileName.set("SnowballFight-${version}.jar")
        relocate("io.github.thatsmusic99.configurationmaster", "me.xginko.snowballfight.libs.configmaster")
        relocate("com.github.cryptomorin.xseries", "me.xginko.snowballfight.libs.xseries")
        exclude(
            "com/cryptomorin/xseries/XBiome*",
            "com/cryptomorin/xseries/NMSExtras*",
            "com/cryptomorin/xseries/NoteBlockMusic*",
            "com/cryptomorin/xseries/SkullCacheListener*"
        )
        relocate("com.github.benmanes.caffeine", "me.xginko.snowballfight.libs.caffeine")
        relocate("space.arim.morepaperlib", "me.xginko.snowballfight.libs.morepaperlib")
        relocate("org.reflections", "me.xginko.snowballfight.libs.reflections")
        relocate("org.bstats", "me.xginko.snowballfight.libs.bstats")
        relocate("net.kyori", "me.xginko.snowballfight.libs.kyori")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(
                    mapOf(
                            "name" to project.name,
                            "version" to project.version,
                            "description" to project.description!!.replace('"'.toString(), "\\\""),
                            "url" to "https://github.com/xGinko/SnowballFight"
                    )
            )
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(shadowJar.get())
        from(zipTree(shadowJar.get().archiveFile))
        finalizedBy("shadeDowngradedApi")
    }

    shadeDowngradedApi {
        archiveFileName = shadowJar.get().archiveFileName
        destinationDirectory = projectDir.resolve("build/libs")

        downgradeTo = JavaVersion.VERSION_1_8
        shadePath = { _ -> "me/xginko/shadow/jvmdowngrader" }
    }
}