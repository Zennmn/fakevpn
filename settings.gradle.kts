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
        // Xposed Framework API repository
        maven { url = uri("https://api.xposed.info/") }
    }
}

rootProject.name = "FakeVPN"
include(":app")
