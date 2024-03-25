buildscript {

    repositories {
        maven {
            url = uri("${rootProject.projectDir}/repo")
        }
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${BuildDependenciesVersions.GRADLE}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
        classpath("com.google.gms:google-services:${BuildDependenciesVersions.GOOGLE_SERVICES}")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${BuildDependenciesVersions.NAVIGATION}")
        classpath("junit:junit:${BuildDependenciesVersions.JUNIT}")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
