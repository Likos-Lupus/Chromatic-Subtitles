plugins {
    // Applies the correct Fabric Loom variant based on the Minecraft version.
    // On 26.1+ (unobfuscated) this is `net.fabricmc.fabric-loom`, on older versions `fabric-loom-remap`.
    id("dev.kikugie.loom-back-compat")
    `maven-publish`
}

val modId = sc.properties["mod.id"] as String
val modVersion = sc.properties["mod.version"] as String
val fabricLoader = sc.properties.get<String>("deps.fabric_loader")

// Keep the published group stable for Maven coordinates across every node.
group = sc.properties["mod.group"] as String
version = "$modVersion+${sc.current.version}"
base.archivesName.set(modId)

val requiredJava = JavaVersion.VERSION_25

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:$fabricLoader")

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    include(libs.night.config.core)
    include(libs.night.config.toml)

    api(libs.jspecify)
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/chromaticsubtitles.accesswidener")

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }

    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(requiredJava.majorVersion.toInt())
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf(
        "id" to modId,
        "name" to sc.properties["mod.name"] as String,
        "version" to project.version.toString(),
        "loader" to Regex("\\d+\\.\\d+").find(fabricLoader)!!.value,
        "minecraft" to sc.properties["mod.mc_compat"] as String
    )

    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(props)
    }

    filesMatching("chromaticsubtitles.mixin.json") {
        expand("java" to "JAVA_${requiredJava.majorVersion}")
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the mod jars and copies them into the root build/libs directory."
    dependsOn(tasks.named("build"))
    from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs"))
}
