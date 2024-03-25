import dependencies.Dependencies
plugins {
    id(BuildPlugins.ANDROID_LIBRARY)
    id(BuildPlugins.KOTLIN_ANDROID)
    id(BuildPlugins.KOTLIN_KAPT)
    id(BuildPlugins.KOTLIN_ANDROID_EXTENSIONS)
    id(BuildPlugins.NAVIGATION_SAFE_ARGS)
    `maven-publish`
}

android {
    resourcePrefix("kyc_")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.fast.vtcc"
            artifactId = "ekyc"
            version = "2.1.8"

            afterEvaluate {
                from(components["release"])
            }
        }
    }

    repositories {
        maven {
            name = "myrepo"
            url = uri("${project.buildDir}/repo")
        }
    }
}

tasks.register<Zip>("generateRepo") {
    val publishTask = tasks.named(
        "publishReleasePublicationToMyrepoRepository",
        PublishToMavenRepository::class.java
    )
    from(publishTask.map { it.repository.url })
    into("mylibrary")
    archiveFileName.set("mylibrary.zip")
}

android {
    compileSdk = BuildAndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = BuildAndroidConfig.MIN_SDK_VERSION
        targetSdk = BuildAndroidConfig.TARGET_SDK_VERSION

        testInstrumentationRunner = BuildAndroidConfig.TEST_INSTRUMENTATION_RUNNER
        consumerProguardFiles(BuildAndroidConfig.CONSUMER_PROGUARD_FILES)

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        externalNativeBuild {
            cmake {
                arguments(
                    "-DOpenCV_DIR=" + file("../ekyc").absolutePath + "/src/main/cpp/sdk/native/jni",
                    "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_STL=c++_shared"
                )
                cppFlags("-std=c++11")
            }
        }
    }

    externalNativeBuild {
        cmake {
            path(file("src/main/cpp/CMakeLists.txt"))
            version = "3.18.1+"
        }
    }


    buildTypes {
        getByName("release") {
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
            isMinifyEnabled = true

            ndk.abiFilters.clear()
            ndk.abiFilters.add("armeabi-v7a")
            ndk.abiFilters.add("arm64-v8a")
        }
        getByName("debug") {
            isMinifyEnabled = false
            ndk.abiFilters.clear()
            ndk.abiFilters.add("armeabi-v7a")
            ndk.abiFilters.add("arm64-v8a")
            ndk.abiFilters.add("x86")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    // Do NOT compress tflite model files (need to call out to developers!)
    aaptOptions {
        noCompress("tflite")
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
        }
        getByName("test") {
            java.setSrcDirs(setOf("src/test/kotlin", "src/sharedTest/kotlin"))
            resources.setSrcDirs(setOf("src/sharedTestResources"))
        }
        getByName("androidTest") {
            java.setSrcDirs(setOf("src/androidTest/kotlin", "src/sharedTest/kotlin"))
            assets.setSrcDirs(setOf("src/sharedTestResources"))
        }
    }

    buildFeatures {
        dataBinding = true
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

//    implementation(project(":opencv"))

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