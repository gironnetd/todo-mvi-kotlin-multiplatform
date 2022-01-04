buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Dependencies.JetBrains.Kotlin.gradlePlugin)
        classpath(Dependencies.Android.Tools.Build.gradlePlugin)
        classpath(Dependencies.Com.Squareup.SQLDelight.gradlePlugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}