class Dependencies {

    object JetBrains {
        object Kotlin {
            val gradlePlugin get() = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.JetBrains.Kotlin.VERSION}"
        }
    }

    object Android {
        object Tools {
            object Build {
                const val gradlePlugin = "com.android.tools.build:gradle:${Versions.Android.Tools.Build.VERSION}"
            }
        }
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayout}"
        const val cardView = "androidx.cardview:cardview:${Versions.AndroidX.cardView}"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerView}"
        const val legacy = "androidx.legacy:legacy-support-v4:${Versions.AndroidX.legacy}"

        object Test {
            object Espresso {
                const val idlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.AndroidX.Test.Espresso.idlingResource}"
            }
        }

        object LifeCycle {
            const val runtime = "androidx.lifecycle:lifecycle-runtime:${Versions.AndroidX.LifeCycle.runtime}"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.AndroidX.LifeCycle.extensions}"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:${Versions.AndroidX.LifeCycle.compiler}"
        }
    }

    object Io {
        object ReactiveX {
            object RxJava2 {
               const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.Io.ReactiveX.RxJava2.rxJava}"
               const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.Io.ReactiveX.RxJava2.rxAndroid}"
                const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.Io.ReactiveX.RxJava2.rxKotlin}"
            }
        }
    }

    object Com {
        object JakeWharton {
            object RxBinding2 {
                const val rxBinding = "com.jakewharton.rxbinding2:rxbinding-support-v4:${Versions.Com.JakeWharton.RxBinding2.rxBinding}"
            }
        }

        object Google {
            object Android {
                const val material = "com.google.android.material:material:${Versions.Com.Google.Android.material}"
            }
        }

        object Badoo {
            object Reaktive {
                const val reaktive = "com.badoo.reaktive:reaktive:${Versions.Com.Badoo.Reaktive.VERSION}"
                const val rxjava2_interop = "com.badoo.reaktive:rxjava2-interop:${Versions.Com.Badoo.Reaktive.VERSION}"
                const val iosSim = "com.badoo.reaktive:reaktive-iossim:${Versions.Com.Badoo.Reaktive.VERSION}"
            }
        }

        object Squareup {
            object SQLDelight {
                const val gradlePlugin = "com.squareup.sqldelight:gradle-plugin:${Versions.Com.Squareup.SQLDelight.VERSION}"
                const val androidDriver = "com.squareup.sqldelight:android-driver:${Versions.Com.Squareup.SQLDelight.VERSION}"
                const val sqliteDriver = "com.squareup.sqldelight:sqlite-driver:${Versions.Com.Squareup.SQLDelight.VERSION}"
                const val nativeDriver = "com.squareup.sqldelight:native-driver:${Versions.Com.Squareup.SQLDelight.VERSION}"
            }
        }
    }

    object Co {
        object TouchLab {
            object Stately {
                const val common = "co.touchlab:stately-common:${Versions.Co.TouchLab.Stately.VERSION}"
                const val concurrency = "co.touchlab:stately-concurrency:${Versions.Co.TouchLab.Stately.VERSION}"
            }
        }
    }
}