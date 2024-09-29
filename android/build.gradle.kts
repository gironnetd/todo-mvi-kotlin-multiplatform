plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":shared"))
    implementation(Dependencies.AndroidX.constraintLayout)
    implementation (Dependencies.AndroidX.appCompat)
    implementation (Dependencies.AndroidX.cardView)
    implementation (Dependencies.Com.Google.Android.material)
    implementation (Dependencies.AndroidX.recyclerView)
    implementation (Dependencies.AndroidX.legacy)
    implementation(Dependencies.AndroidX.Test.Espresso.idlingResource)
    implementation(Dependencies.Io.ReactiveX.RxJava2.rxJava)
    implementation(Dependencies.Io.ReactiveX.RxJava2.rxAndroid)
    implementation(Dependencies.Com.JakeWharton.RxBinding2.rxBinding)
    implementation(Dependencies.AndroidX.LifeCycle.runtime)
    implementation(Dependencies.AndroidX.LifeCycle.extensions)
    implementation(Dependencies.Io.ReactiveX.RxJava2.rxKotlin)
    implementation(Dependencies.Com.Badoo.Reaktive.rxjava2_interop)
    implementation(Dependencies.Com.Badoo.Reaktive.reaktive)
    kapt(Dependencies.AndroidX.LifeCycle.compiler)
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.todo.mvi.kotlin.multiplatform.android"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    namespace = "com.todo.mvi.kotlin.multiplatform.android"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}