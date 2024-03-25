import dependencies.Dependencies
import dependencies.TestAndroidDependencies
import dependencies.TestDependencies

plugins {
    id(BuildPlugins.ANDROID_APPLICATION)
    id(BuildPlugins.KOTLIN_ANDROID)
    id(BuildPlugins.KOTLIN_KAPT)
}

android {
    compileSdk = BuildAndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = BuildAndroidConfig.APPLICATION_ID
        minSdk = BuildAndroidConfig.MIN_SDK_VERSION
        targetSdk = BuildAndroidConfig.TARGET_SDK_VERSION
        versionCode = BuildAndroidConfig.VERSION_CODE
        versionName = BuildAndroidConfig.VERSION_NAME

        testInstrumentationRunner = BuildAndroidConfig.TEST_INSTRUMENTATION_RUNNER
        vectorDrawables.useSupportLibrary = true

        multiDexEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    dataBinding {
        isEnabled = true
    }

    signingConfigs {
        getByName(BuildType.DEBUG) {
            keyAlias = "debug"
            keyPassword = "12345678"
            storeFile = file("../buildSystem/debug")
            storePassword = "12345678"
        }

        create(BuildType.RELEASE) {
            keyAlias = "debug"
            keyPassword = "12345678"
            storeFile = file("../buildSystem/debug")
            storePassword = "12345678"
        }
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
            signingConfig = signingConfigs.getByName(name)

            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
        }

        getByName(BuildType.DEBUG) {
            signingConfig = signingConfigs.getByName(name)

            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
        }
    }
}

dependencies {
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.jar")
            )
        )
    )

//    implementation(files("libs/ekyc-release.aar"))
    implementation(project(":ekyc"))
//
    // Dependency Injection
    implementation(Dependencies.DAGGER)
    implementation(Dependencies.DAGGER_ANDROID)



    api(Dependencies.KOTLIN)
    api(Dependencies.CORE_KTX)

    api(Dependencies.APPCOMPAT)
    api(Dependencies.MATERIAL_COMPONENTS)
    api(Dependencies.CONSTRAIN_LAYOUT)
    api(Dependencies.NAVIGATION)
    api(Dependencies.NAVIGATION_UI)
    implementation(Dependencies.FRAGMENT)

    testImplementation(TestDependencies.JUNIT)

    androidTestImplementation(TestAndroidDependencies.JUNIT)
    androidTestImplementation(TestAndroidDependencies.ESPRESSO)

    implementation(Dependencies.KOTLIN)
    implementation(Dependencies.CORE_KTX)

    // Networking
    implementation(Dependencies.RETROFIT)
    implementation(Dependencies.RETROFIT_CONVERTER)
    implementation(Dependencies.OKHTTP)
    implementation(Dependencies.OKHTTP_LOGGING)
    implementation(Dependencies.GSON)

    // Dependency Injection
    implementation(Dependencies.DAGGER)
    implementation(Dependencies.DAGGER_ANDROID)
    implementation(Dependencies.CORE_KTX)
    kapt(AnnotationProcessorsDependencies.DAGGER_COMPILER)
    kapt(AnnotationProcessorsDependencies.DAGGER_ANDROID_PROCESSOR)

    //  Coroutine
    implementation(Dependencies.LIFECYCLE_LIVEDATA)
    implementation(Dependencies.COROUTINES)
    implementation(Dependencies.COROUTINES_ANDROID)

    // UI and Lifecycle
    implementation(Dependencies.FRAGMENT)
    implementation(Dependencies.RECYCLER_VIEW)
    implementation(Dependencies.LIFECYCLE_VIEWMODEL)
    implementation(Dependencies.LIFECYCLE_LIVEDATA)
    implementation(Dependencies.CONSTRAIN_LAYOUT)
    implementation(Dependencies.MATERIAL_COMPONENTS)

    // Navigation
    implementation(Dependencies.NAVIGATION)
    implementation(Dependencies.NAVIGATION_UI)

    // Multidex
    implementation(Dependencies.MULTIDEX)

    implementation("org.jmrtd:jmrtd:0.7.18")
    implementation("net.sf.scuba:scuba-sc-android:0.0.20")
}