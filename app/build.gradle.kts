import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Para convertir el JSON de Steam en clases de Kotlin
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
}

android {
    namespace = "me.kevinbayona.steamsalesapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "me.kevinbayona.steamsalesapp"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Lee la API key de IsThereAnyDeal desde local.properties (ese archivo NO se sube a Git)
        // y la deja disponible en el código como BuildConfig.ITAD_KEY
        val propiedades = Properties()
        val archivo = rootProject.file("local.properties")
        if (archivo.exists()) archivo.inputStream().use { propiedades.load(it) }
        buildConfigField("String", "ITAD_KEY", "\"${propiedades.getProperty("ITAD_KEY", "")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true   // para poder usar BuildConfig.ITAD_KEY
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Íconos Material extendidos (mando, explorar, ajustes, etc.)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Carga de imágenes desde URL (portadas de Steam)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ViewModel para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    // Retrofit: para llamar a la API de Steam
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    // Para recibir respuestas de texto (XML del perfil y verificación del login)
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}