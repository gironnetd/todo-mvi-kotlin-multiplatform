plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.app.compat)
    implementation(libs.androidx.card.view)
    implementation(libs.google.material)
    implementation(libs.androidx.recycler.view)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.test.espresso)
    implementation(libs.reactivex.rxjava2.rxjava)
    implementation(libs.reactivex.rxjava2.rxandroid)
    implementation(libs.jakewharton.rxbinding2.rxbinding.support.v4)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.reactivex.rxjava2.rxkotlin)
    implementation(libs.badoo.reaktive.rxjava2.interop)
    implementation(libs.badoo.reaktive.reaktive)
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "com.todo.mvi.kotlin.multiplatform.android"
        minSdk = 21
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}