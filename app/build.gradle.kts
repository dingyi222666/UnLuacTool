plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {

    compileSdk = 32

    defaultConfig {
        applicationId = "com.dingyi.unluactool"
        minSdk = 26
        targetSdk = 31

        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
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

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0-alpha01")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("dev.chrisbanes.insetter:insetter:0.6.1")


    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0-alpha01")
    implementation("androidx.fragment:fragment-ktx:1.5.2")

    implementation("net.lingala.zip4j:zip4j:2.11.1")


    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation(kotlin("reflect"))

    // https://mvnrepository.com/artifact/org.apache.commons/commons-vfs2
    implementation("org.apache.commons:commons-vfs2:2.9.0") {
        exclude("org.apache.hadoop")
    }


}
