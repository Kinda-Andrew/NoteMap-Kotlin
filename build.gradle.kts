// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    // Required on the root classpath so AGP 9.2's built-in Kotlin support uses Kotlin 2.3.0
    // (which can read Kotlin 2.4 metadata in Places SDK 6.0.2) instead of AGP's bundled Kotlin 2.2.10.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.maps.secrets) apply false
}



allprojects {
    // Required: you must exclude the Google Play services Maps SDK from
    // your transitive dependencies. This ensures there won't be
    // multiple copies of Google Maps SDK in your binary, as Navigation SDK
    // already bundles the Google Maps SDK.
    configurations.all {
        exclude(group = "com.google.android.gms", module = "play-services-maps")
    }
}