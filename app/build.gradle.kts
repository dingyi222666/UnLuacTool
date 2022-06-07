plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {

    compileSdk = 32

    defaultConfig {
        applicationId = "com.dingyi.unluactool"
        minSdk = 21
        targetSdk = 31

        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            resources.srcDirs("src/main/res")

        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
            resources.srcDirs("src/test/res")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false

        }
    }
    buildFeatures {
        viewBinding = true
    }
    packagingOptions {
        resources.excludes.addAll(listOf("META-INF/**", "xsd/*", "license/*"))
        resources.pickFirsts.add("kotlin/**")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0-rc01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0-rc01")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.code.gson:gson:2.9.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}