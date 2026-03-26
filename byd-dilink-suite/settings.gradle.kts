pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "byd-dilink-suite"

include(":core:ui")
include(":core:data")
include(":app-parking")
include(":app-dashboard")
include(":app-maintenance")
include(":app-media")
