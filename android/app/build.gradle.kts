plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.einkaufsliste"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.einkaufsliste"
        minSdk = 26
        targetSdk = 35
        versionCode = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionName = "1.0." + (System.getenv("GITHUB_RUN_NUMBER") ?: "0")
    }

    // Fester Schlüssel, damit neue Versionen sich über die alte installieren lassen (ohne Datenverlust)
    signingConfigs {
        getByName("debug") {
            storeFile = file("einkaufsliste.jks")
            storePassword = "einkaufsliste"
            keyAlias = "einkaufsliste"
            keyPassword = "einkaufsliste"
        }
    }

    // Die Oberfläche ist dieselbe Web-App wie in docs/
    sourceSets["main"].assets.srcDirs("../../docs")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
