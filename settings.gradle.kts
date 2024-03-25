//dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
//    repositories {
//        google()
//        mavenCentral()
//        jcenter() // Warning: this repository is going to shut down soon
//    }
//}
//rootProject.name = "EKYC"
//include ':app'

rootProject.name = "FastEKYC"

include(
    ":app",
    ":ekyc",
//    ":opencv",
)

//project(":opencv").projectDir =
//    File(rootProject.projectDir, "./sdk")
