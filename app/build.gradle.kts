plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {

    compileSdk = 33

    namespace = "com.dingyi.unluactool"

    defaultConfig {
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
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
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false

        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    packagingOptions {
        resources.excludes.addAll(listOf("xsd/*", "license/*", "META-INF/*.SF", "META-INF/*.RSA"))
        resources.pickFirsts.addAll(listOf("kotlin/**"))
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(project(":unluac"))

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0-alpha05")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0-alpha05")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0-alpha05")
    implementation("androidx.fragment:fragment-ktx:1.5.5")
    implementation("com.google.android.material:material:1.9.0-alpha01")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("net.lingala.zip4j:zip4j:2.11.1")
    implementation("com.github.Commit451:ScrimInsetsLayout:1.1.0")
    implementation(kotlin("reflect"))
    implementation("org.apache.commons:commons-vfs2:2.9.0") {
        exclude("org.apache.hadoop")
    }

    implementation("io.github.dingyi222666:treeview:1.1.0")
    implementation("com.github.techinessoverloaded:progress-dialog:1.5.1")
    implementation(platform("io.github.Rosemoe.sora-editor:bom:0.21.0"))
    implementation("io.github.Rosemoe.sora-editor:editor")
    implementation("io.github.Rosemoe.sora-editor:language-textmate")


    testImplementation("junit:junit:4.13.2")

}
