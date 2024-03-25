/**
 * Project annotation processor dependencies, makes it easy to include external binaries or
 * other library modules to build.
 */
object AnnotationProcessorsDependencies {
    const val ROOM_COMPILER = "androidx.room:room-compiler:${BuildDependenciesVersions.ROOM}"
    const val DAGGER_COMPILER =
        "com.google.dagger:dagger-compiler:${BuildDependenciesVersions.DAGGER}"
    const val DAGGER_ANDROID_PROCESSOR =
        "com.google.dagger:dagger-android-processor:${BuildDependenciesVersions.DAGGER}"
    const val GLIDE_COMPILER =
        "com.github.bumptech.glide:compiler:${BuildDependenciesVersions.GLIDE}"

}
