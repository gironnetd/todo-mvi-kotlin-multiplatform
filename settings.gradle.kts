pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "todo-mvi-kotlin-multiplatform"
include(":android")
include(":shared")