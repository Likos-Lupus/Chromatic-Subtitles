plugins {
    id("dev.kikugie.loom-back-compat")
}

val modId = providers.gradleProperty("archives_base_name").get()
val modVersion = providers.gradleProperty("mod_version").get()
val mcVersion = sc.current.version
val javaVersion = libs.versions.java.get()

group = providers.gradleProperty("maven_group").get()
version = "$modVersion+$mcVersion"
base.archivesName.set("$modId-fabric")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    loomx.applyMojangMappings()

    modImplementation(libs.fabric.loader)

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    include(libs.night.config.core)
    include(libs.night.config.toml)

    compileOnly(libs.jspecify)
}

loom {
    fabricModJsonPath = rootProject.file("src/fabric/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/chromaticsubtitles.accesswidener")

    runConfigs.configureEach {
        runDir(rootProject.file("run").path)
    }
}

sourceSets["main"].apply {
    java.srcDir(rootProject.file("src/fabric/java"))
    resources.srcDir(rootProject.file("src/fabric/resources"))
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion.toInt())
    options.encoding = "UTF-8"
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft_version" to mcVersion,
        "loader_version" to libs.versions.fabric.loader.get(),
    )

    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
    filesMatching("chromaticsubtitles.mixin.json") {
        expand("java" to "JAVA_$javaVersion")
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_$modId" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the Fabric jar and copies it into the root build/libs directory."
    dependsOn(tasks.named("build"))
    from(loomx.modJar, loomx.modSourcesJar)
    into(rootProject.layout.buildDirectory.dir("libs/${sc.current.project}"))
}
