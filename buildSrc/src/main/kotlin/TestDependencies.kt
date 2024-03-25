package dependencies

/**
 * Project test dependencies, makes it easy to include external binaries or
 * other library modules to build.
 */
object TestDependencies {
    const val JUNIT = "junit:junit:${BuildDependenciesVersions.JUNIT}"
    const val JUNIT_EXT = "androidx.test.ext:junit:${BuildDependenciesVersions.JUNIT_EXT}"
    const val JUNIT_EXT_KTX = "androidx.test.ext:junit-ktx:${BuildDependenciesVersions.JUNIT_EXT}"
    const val TEST_RUNNER = "androidx.test:runner:${BuildDependenciesVersions.EXT}"
    const val TEST_CORE = "androidx.test:core:${BuildDependenciesVersions.EXT}"

    const val TEST_EXT = "androidx.test.ext:junit:${BuildDependenciesVersions.EXT}"
    const val ESPRESSO_CORE =
        "androidx.test.espresso:espresso-core:${BuildDependenciesVersions.ESPRESSO}"
    const val ESPRESSO_INTENT =
        "androidx.test.espresso:espresso-intents:${BuildDependenciesVersions.ESPRESSO}"
    const val ESPRESSO_CONTRIB =
        "androidx.test.espresso:espresso-contrib:${BuildDependenciesVersions.ESPRESSO}"

    const val MOCKITO_CORE = "org.mockito:mockito-core:${BuildDependenciesVersions.MOCKITO}"
    const val MOCKITO_INLINE = "org.mockito:mockito-inline:${BuildDependenciesVersions.MOCKITO}"
    const val COROUTINE_TEST =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${BuildDependenciesVersions.COROUTINES}"
    const val WORK_TEST =
        "androidx.work:work-testing:${BuildDependenciesVersions.WORK_MANAGER}"
    const val MOCK_WEB_SERVER =
        "com.squareup.okhttp3:mockwebserver:${BuildDependenciesVersions.OKHTTP}"
}

