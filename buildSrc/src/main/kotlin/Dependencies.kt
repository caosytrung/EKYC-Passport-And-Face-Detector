package dependencies

object Dependencies {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${BuildDependenciesVersions.KOTLIN}"
    const val COROUTINES =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${BuildDependenciesVersions.COROUTINES}"
    const val COROUTINES_ANDROID =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${BuildDependenciesVersions.COROUTINES}"
    const val CORE_KTX = "androidx.core:core-ktx:${BuildDependenciesVersions.CORE_KTX}"

    const val RECYCLER_VIEW =
        "androidx.recyclerview:recyclerview:${BuildDependenciesVersions.RECYCLER_VIEW}"
    const val APPCOMPAT = "androidx.appcompat:appcompat:${BuildDependenciesVersions.APPCOMPAT}"
    const val FRAGMENT = "androidx.fragment:fragment-ktx:${BuildDependenciesVersions.FRAGMENT}"
    const val NAVIGATION =
        "androidx.navigation:navigation-fragment-ktx:${BuildDependenciesVersions.NAVIGATION}"
    const val NAVIGATION_UI =
        "androidx.navigation:navigation-ui-ktx:${BuildDependenciesVersions.NAVIGATION}"
    const val CONSTRAIN_LAYOUT =
        "androidx.constraintlayout:constraintlayout:${BuildDependenciesVersions.CONSTRAIN_LAYOUT}"
    const val RECYLERVIEW =
        "androidx.recyclerview:recyclerview:${BuildDependenciesVersions.REYCLERVIEW}"
    const val MATERIAL_COMPONENTS =
        "com.google.android.material:material:${BuildDependenciesVersions.MATERIAL_COMPONENTS}"
    const val FLEX_BOX = "com.google.android.flexbox:flexbox:${BuildDependenciesVersions.FLEX_BOX}"
    const val SPINNER_DATE_PICKER =
        "com.github.drawers:SpinnerDatePicker:${BuildDependenciesVersions.SPINNER_DATE_PICKER}"
    const val SHIMMER_FRAME_LAYOUT =
        "com.facebook.shimmer:shimmer:${BuildDependenciesVersions.SHIMMER_FRAME_LAYOUT}"
    const val MULTIDEX = "androidx.multidex:multidex:${BuildDependenciesVersions.MULTIDEX}"
    const val LIFECYCLE_VIEWMODEL =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${BuildDependenciesVersions.LIFECYCLE}"
    const val LIFECYCLE_LIVEDATA =
        "androidx.lifecycle:lifecycle-livedata-ktx:${BuildDependenciesVersions.LIFECYCLE}"
    const val LIFECYCLE_RUNTIME =
        "androidx.lifecycle:lifecycle-runtime-ktx:${BuildDependenciesVersions.LIFECYCLE}"
    const val LIFECYCLE_COMMON_JAVA8 =
        "androidx.lifecycle:lifecycle-common-java8:${BuildDependenciesVersions.LIFECYCLE}"
    const val LIFECYCLE_PROCESS =
        "androidx.lifecycle:lifecycle-process:${BuildDependenciesVersions.LIFECYCLE}"
    const val RETROFIT = "com.squareup.retrofit2:retrofit:${BuildDependenciesVersions.RETROFIT}"
    const val RETROFIT_CONVERTER =
        "com.squareup.retrofit2:converter-gson:${BuildDependenciesVersions.RETROFIT}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${BuildDependenciesVersions.OKHTTP}"
    const val OKHTTP_LOGGING =
        "com.squareup.okhttp3:logging-interceptor:${BuildDependenciesVersions.OKHTTP}"
    const val GSON = "com.google.code.gson:gson:${BuildDependenciesVersions.GSON}"
    const val ROOM = "androidx.room:room-ktx:${BuildDependenciesVersions.ROOM}"
    const val ROOM_RUNTIME = "androidx.room:room-runtime:${BuildDependenciesVersions.ROOM}"
    const val TIMBER = "com.jakewharton.timber:timber:${BuildDependenciesVersions.TIMBER}"
    const val DAGGER = "com.google.dagger:dagger:${BuildDependenciesVersions.DAGGER}"
    const val DAGGER_ANDROID =
        "com.google.dagger:dagger-android-support:${BuildDependenciesVersions.DAGGER}"
    const val WORK_MANAGER =
        "androidx.work:work-runtime-ktx:${BuildDependenciesVersions.WORK_MANAGER}"

    const val SWIPE_REFRESH_LAYOUT =
        "androidx.swiperefreshlayout:swiperefreshlayout:${BuildDependenciesVersions.SWIPE_REFRESH}"
    const val PAGING = "androidx.paging:paging-runtime-ktx:${BuildDependenciesVersions.PAGING}"
    const val PAGING_3 = "androidx.paging:paging-runtime-ktx:${BuildDependenciesVersions.PAGING_3}"
    const val GLIDE = "com.github.bumptech.glide:glide:${BuildDependenciesVersions.GLIDE}"
    const val CARDVIEW = "androidx.cardview:cardview:${BuildDependenciesVersions.CARDVIEW}"

    const val FIREBASE_ANALYTIC =
        "com.google.firebase:firebase-analytics:${BuildDependenciesVersions.FIREBASE_ANALYTIC}"
    const val FIREBASE_CRASHLYTIC =
        "com.google.firebase:firebase-crashlytics:${BuildDependenciesVersions.FIREBASE_CRASHLYTIC}"
    const val FIREBASE_DATABASE =
        "com.google.firebase:firebase-database-ktx:${BuildDependenciesVersions.FIREBASE_DATABASE}"
    const val FIREBASE_MESSAGING =
        "com.google.firebase:firebase-messaging:${BuildDependenciesVersions.FIREBASE_MESSAGING}"
    const val FIREBASE_AUTH =
        "com.google.firebase:firebase-auth:${BuildDependenciesVersions.FIREBASE_AUTH}"
    const val FIREBASE_PERF =
        "com.google.firebase:firebase-perf:${BuildDependenciesVersions.FIREBASE_PERF}"
    const val FIREBASE_REMOTE_CONFIG =
        "com.google.firebase:firebase-config:${BuildDependenciesVersions.FIREBASE_REMOTE_CONFIG}"
    const val FIREBASE_FIRESTORE =
        "com.google.firebase:firebase-firestore-ktx:${BuildDependenciesVersions.FIREBASE_FIRESTORE}"
    const val FIREBASE_DYNAMIC_LINK =
        "com.google.firebase:firebase-dynamic-links-ktx:${BuildDependenciesVersions.FIREBASE_DYNAMIC_LINK}"
    const val FIREBASE_BOM = "com.google.firebase:firebase-bom:${BuildDependenciesVersions.FIREBASE_BOM}"

    const val GOOGLE_PLAY_SERVICE_LOCATION =
        "com.google.android.gms:play-services-location:${BuildDependenciesVersions.GOOGLE_PLAY_SERVICES}"

    const val CAMERA_VIEW = "com.otaliastudios:cameraview:${BuildDependenciesVersions.CAMERA_VIEW}"

    const val CAMERAX_CAMERA2 = "androidx.camera:camera-camera2:${BuildDependenciesVersions.CAMERAX_CAMERA2}"
    const val CAMERAX_CORE = "androidx.camera:camera-core:${BuildDependenciesVersions.CAMERAX_CORE}"
    const val CAMERAX_LIFECYCLE = "androidx.camera:camera-lifecycle:${BuildDependenciesVersions.CAMERAX_LIFECYCLE}"
    const val CAMERAX_VIEW = "androidx.camera:camera-view:${BuildDependenciesVersions.CAMERAX_VIEW}"

    const val FIREBASE_ML_KIT =
        "com.google.firebase:firebase-ml-vision:${BuildDependenciesVersions.MLKIT}"
    const val FIREBASE_ML_KIT_BARCODE_MODEL =
        "com.google.firebase:firebase-ml-vision-barcode-model:${BuildDependenciesVersions.MLKIT_BARCODE_MODEL}"
    const val GOOGLE_ML_KIT_BARCODE_SCANNING =
        "com.google.mlkit:barcode-scanning:${BuildDependenciesVersions.GOOGLE_BARCODE_SCANNING}"
    const val GOOGLE_ML_KIT_BARCODE_MODEL =
        "com.google.android.gms:play-services-mlkit-barcode-scanning:${BuildDependenciesVersions.GOOGLE_BARCODE_MODEL}"

    const val COROUTINE_PLAY_SERVICE =
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${BuildDependenciesVersions.COROUTINE_PLAY_SERVICE}"

    const val SUNMI_PRINTER = "com.sunmi:printerlibrary:${BuildDependenciesVersions.SUNMI_PRINTER}"

    const val MP_CHART = "com.github.PhilJay:MPAndroidChart:v${BuildDependenciesVersions.MP_CHART}"

    const val MAP_STRUCT = "org.mapstruct:mapstruct:${BuildDependenciesVersions.MAP_STRUCT}"
    const val MAP_STRUCT_KOTLIN_BUILDER =
        "com.github.pozo:mapstruct-kotlin:${BuildDependenciesVersions.MAP_STRUCT_KOTLIN_BUILDER}"

    const val JSC_FLAVOR = "org.webkit:android-jsc:${BuildDependenciesVersions.JSC_FLAVOR}"

    const val KODEIN_DI =
        "org.kodein.di:kodein-di:${BuildDependenciesVersions.KODEIN_DI}"
    const val KODEIN_CONFIGURABLE =
        "org.kodein.di:kodein-di-conf:${BuildDependenciesVersions.KODEIN_DI}"


    const val PLAY_CORE = "com.google.android.play:core:${BuildDependenciesVersions.PLAY_CORE}"
    const val PLAY_CORE_KTX =
        "com.google.android.play:core-ktx:${BuildDependenciesVersions.PLAY_CORE_KTX}"
    const val PROTOBUF_JAVA =
        "com.google.protobuf:protobuf-java:${BuildDependenciesVersions.PROTOBUF}"
    const val PROTOBUF_JAVA_UTIL =
        "com.google.protobuf:protobuf-java-util:${BuildDependenciesVersions.PROTOBUF}"

    const val DESUGAR_JDK_LIBS =
        "com.android.tools:desugar_jdk_libs:${BuildDependenciesVersions.DESUGAR_JDK_LIBS}"
}
