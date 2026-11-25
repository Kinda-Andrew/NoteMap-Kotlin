import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.maps.secrets)
}

secrets {
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the root directory (the same folder as the root local.properties file).
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.defaults.properties"
}

android {
    namespace = "com.example.setupformaps"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.setupformaps"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        // Set this to the languages you actually use, otherwise you'll include resource strings
        // for all Navigation SDK supported languages.
        localeFilters += "en"
    }

    buildTypes {
        release {
            optimization {
                isMinifyEnabled = true
                isShrinkResources = true
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.androidx.activity.ktx)
    // Include the Google Navigation SDK.
    // Note: remember to exclude Google Play services Maps SDK from your transitive
    // dependencies to avoid duplicate copies of the Google Maps SDK.
    implementation(libs.google.maps.navigation)

    // Add: android.useDeprecatedNdk=true
    // to local.properties.
    implementation(libs.cronet.fallback)

    //include the Google Places SDK, which is used by this example, but
    // isn't required by the Navigation SDK.
    implementation(libs.google.places)
    implementation(libs.google.material)

    implementation(libs.kotlin.stdlib)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    //basic android dependencies
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.customview)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.tracing)
    implementation(libs.androidx.transition)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.window)

    implementation(libs.play.services.location)

    implementation(libs.jts.core)

    implementation(libs.glide)
    implementation(libs.datatransport.api)
    implementation(libs.datatransport.backend.cct)
    implementation(libs.datatransport.runtime)
    implementation(libs.joda.time)




    /*
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.play.services.location)
    //implementation(libs.play.services.maps) //conflicts with Navigation SDK
    implementation("org.locationtech.jts:jts-core:1.20.0")
    implementation("com.google.android.libraries.navigation:navigation:8.1.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.1.5") //required for navigation SDK for some reason
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
*/

    // If you include the Maps Utils library, you must exclude the independent
    // play-services-maps dependency, as the Navigation SDK provides its own
    // bundled, compatible Map implementation.
    implementation(libs.google.maps.utils) {
        exclude(group = "com.google.android.gms", module = "play-services-maps")
    }

}