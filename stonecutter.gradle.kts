plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

tasks.register("chiseledBuildAndCollect") {
    group = "build"
    description = "Builds and collects every registered version."
    dependsOn(stonecutter.tasks.named("buildAndCollect").map { it.values })
}

tasks.register("chiseledClean") {
    group = "build"
    description = "Cleans every registered version."
    dependsOn(stonecutter.tasks.named("clean").map { it.values })
}
