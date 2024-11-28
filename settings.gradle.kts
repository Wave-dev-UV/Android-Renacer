pluginManagement {
    repositories {
        google()  // Mantén este repositorio
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }  // Añadido JitPack
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()  // Repositorio Google
        mavenCentral()  // Repositorio Maven Central
        maven { setUrl("https://jitpack.io") }  // Repositorio JitPack
    }
}

rootProject.name = "GestRenacer"
include(":app")
