plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3-fabric" /* [SC] DO NOT EDIT */

tasks.register("chiseledBuildAndCollect") {
    group = "build"
    description = "Builds and collects every registered version."
    dependsOn(stonecutter.tasks.names("buildAndCollect"))
}

tasks.register("chiseledClean") {
    group = "build"
    description = "Cleans every registered version."
    dependsOn(stonecutter.tasks.names("clean"))
}

tasks.register("buildActive") {
    group = "build"
    description = "Builds and collects the active version."
    dependsOn(
        ":${
            stonecutter.active?.project
                ?: error("No active Stonecutter version")
        }:buildAndCollect"
    )
}
