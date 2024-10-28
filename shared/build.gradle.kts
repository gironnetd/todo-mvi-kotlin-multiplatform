plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("TasksDatabase") {
            packageName.set("com.todo.mvi.kotlin.multiplatform")
        }
    }
    linkSqlite = true
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.AZUL)
    }

    sourceSets.commonMain.dependencies {
        api(libs.badoo.reaktive.reaktive)
        implementation(libs.touchlab.stately.common)
        implementation(libs.touchlab.stately.concurrency)
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    androidTarget()

    sourceSets.androidMain.dependencies {
        implementation(libs.sqldelight.android.driver)
    }

    sourceSets.androidUnitTest.dependencies {
        implementation(libs.kotlin.test)
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
            export(libs.badoo.reaktive.iossim)
            export(libs.badoo.reaktive.reaktive)
            binaryOption("bundleId", "com.todo.mvi.kotlin.multiplatform.ios")
        }
    }

    sourceSets.iosMain.dependencies {
        implementation(libs.sqldelight.native.driver)
    }

    sourceSets.iosTest.dependencies {
        implementation(libs.kotlin.test)
    }

    jvm()

    sourceSets.jvmMain.dependencies {
        implementation(libs.sqldelight.sqlite.driver)
    }

    sourceSets.jvmTest.dependencies {
        implementation(libs.kotlin.test)
    }
}

//android {
//    namespace = "com.todo.mvi.kotlin.multiplatform"
//    compileSdk = 34
//    defaultConfig {
//        minSdk = 21
//    }
//}

//import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
//
//plugins {
//    kotlin("multiplatform")
//    kotlin("native.cocoapods")
//    id("com.android.library")
//    id("app.cash.sqldelight")
//}
//
//version = "1.0"
//
//sqldelight {
//    databases {
//        create("TasksDatabase") {
//            packageName.set("com.todo.mvi.kotlin.multiplatform")
//        }
//    }
//    linkSqlite = true
//}
//
//kotlin {
//    applyDefaultHierarchyTemplate()
//
//    jvmToolchain {
//        languageVersion.set(JavaLanguageVersion.of(21))
//        vendor.set(JvmVendorSpec.AZUL)
//    }
//
//    androidTarget()
//
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//
//    cocoapods {
//        summary = "Some description for the Shared Module"
//        homepage = "Link to the Shared Module homepage"
//        version = "1.0"
//        ios.deploymentTarget = "14.4"
//        podfile = project.file("../ios/Podfile")
//        framework {
//            baseName = "shared"
//            isStatic = false
//            export(Dependencies.Com.Badoo.Reaktive.iosSim)
//            export(Dependencies.Com.Badoo.Reaktive.reaktive)
//            binaryOption("bundleId", "com.todo.mvi.kotlin.multiplatform.ios")
//        }
//    }
//
//    jvm()
//
//    sourceSets.jvmMain.dependencies {
//        implementation(Dependencies.Com.Squareup.SQLDelight.sqliteDriver)
//    }
//
//    sourceSets.jvmTest.dependencies {
//        //implementation(libs.kotlin.test)
//    }
//
//    sourceSets {
//        val commonMain by getting {
//            dependencies {
//                api(Dependencies.Com.Badoo.Reaktive.reaktive)
//                implementation(Dependencies.Co.TouchLab.Stately.common)
//                implementation(Dependencies.Co.TouchLab.Stately.concurrency)
//            }
//        }
//        val commonTest by getting {
//            dependencies {
//                implementation(kotlin("test-common"))
//                implementation(kotlin("test-annotations-common"))
//            }
//        }
//        val androidMain by getting {
//            dependencies {
//                implementation(Dependencies.Com.Squareup.SQLDelight.androidDriver)
//                //implementation(Dependencies.Com.Squareup.SQLDelight.sqliteDriver)
//            }
//        }
//        //val androidAndroidTestRelease by getting
////        val androidTest by getting {
////            //dependsOn(androidAndroidTestRelease)
////            dependencies {
////                implementation(kotlin("test-junit"))
////                implementation("junit:junit:4.13.2")
////            }
////        }
//        val iosMain by getting {
//            dependencies {
//                implementation(Dependencies.Com.Squareup.SQLDelight.nativeDriver)
//            }
//        }
//        val iosTest by getting
//    }
//}
//
android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        lint.targetSdk = 35
    }
    namespace = "com.todo.mvi.kotlin.multiplatform"

//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_21
//        targetCompatibility = JavaVersion.VERSION_21
//    }
}