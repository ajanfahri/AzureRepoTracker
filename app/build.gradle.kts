import java.io.IOException
import java.util.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.azurerepotracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.azurerepotracker"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        // local.properties dosyasından değerleri oku
        val localProperties = Properties()
        localProperties.load(project.rootProject.file("local.properties").inputStream())

        // Değeri BuildConfig'e ekle
        buildConfigField("String", "AZURE_TOKEN", getLocalProperty("AZURE_TOKEN") ?: "default_value")
        buildConfigField("String", "ORGANIZATION_NAME", getLocalProperty("ORGANIZATION_NAME") ?: "default_value")
        buildConfigField("String", "PROJECT_NAME", getLocalProperty("PROJECT_NAME") ?: "default_value")
        buildConfigField("String", "REPO_NAME", getLocalProperty("REPO_NAME") ?: "default_value")

        buildConfigField("String", "REPO_LIST", "\"${getLocalProperty("REPO_LIST") ?: ""}\"")


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
       // kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
        // kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()


    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getLocalProperty(name: String): String? {
    return try {
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        properties[name] as String?
    } catch (e: IOException) {
        null
    }
}


// RepositoryInfo veri sınıfı
data class RepositoryInfo(val owner: String, val name: String)
dependencies {
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM'u kullanın
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)


    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.1.1") // Veya daha yeni

    // Eski sürüm: implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")  // Uygun sürümü kullanın

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0") // Veya güncel sürümü kullanın
    implementation("com.squareup.okhttp3:okhttp:4.10.0") // Veya güncel sürümü kullanın
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Test için de BOM'u kullanın



}