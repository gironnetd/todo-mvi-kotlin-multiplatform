import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("app.cash.sqldelight")
    //id("com.squareup.sqldelight")
}

version = "1.0"

sqldelight {
    databases {
        create("TasksDatabase") {
            packageName.set("com.todo.mvi.kotlin.multiplatform")
        }
    }
//    database("TasksDatabase") {
//        packageName = "com.todo.mvi.kotlin.multiplatform"
//    }
}

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {}

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "shared"
            export(Dependencies.Com.Badoo.Reaktive.iosSim)
            export(Dependencies.Com.Badoo.Reaktive.reaktive)
            binaryOption("bundleId", "com.todo.mvi.kotlin.multiplatform.ios")
        }
        podfile = project.file("../ios/Podfile")
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(Dependencies.Com.Badoo.Reaktive.reaktive)
                implementation(Dependencies.Co.TouchLab.Stately.common)
                implementation(Dependencies.Co.TouchLab.Stately.concurrency)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(Dependencies.Com.Squareup.SQLDelight.androidDriver)
                implementation(Dependencies.Com.Squareup.SQLDelight.sqliteDriver)
            }
        }
        //val androidAndroidTestRelease by getting
//        val androidTest by getting {
//            //dependsOn(androidAndroidTestRelease)
//            dependencies {
//                implementation(kotlin("test-junit"))
//                implementation("junit:junit:4.13.2")
//            }
//        }
        val iosMain by getting {
            dependencies {
                implementation(Dependencies.Com.Squareup.SQLDelight.nativeDriver)
            }
        }
        val iosTest by getting
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }
    namespace = "com.todo.mvi.kotlin.multiplatform"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}