plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

object PluginsVersions {
    const val GRADLE_ANDROID = "7.0.2"
    const val KOTLIN = "1.6.21"
    const val GSON = "2.8.9"
}

dependencies {
    implementation("com.android.tools.build:gradle:${PluginsVersions.GRADLE_ANDROID}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${PluginsVersions.KOTLIN}")
    implementation("org.jetbrains.kotlin:kotlin-serialization:${PluginsVersions.KOTLIN}")
    implementation("com.google.code.gson:gson:${PluginsVersions.GSON}")
}