import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
    id("com.google.gms.google-services")
}

val keystoreProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        load(FileInputStream(propertiesFile))
    }
}

android {
    namespace = "com.ncw6fg.nxhw18e.pdfreader"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ncw6fg.nxhw18e.pdfreader"
        minSdk = 24
        targetSdk = 36
        versionCode = 103
        versionName = "1.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        setProperty("archivesBaseName","PDR-V${versionName}")
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties.getProperty("signing.storeFile")?.let { file(it) }
            storePassword = keystoreProperties.getProperty("signing.storePassword")
            keyAlias = keystoreProperties.getProperty("signing.keyAlias")
            keyPassword = keystoreProperties.getProperty("signing.keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.hilt.android)
    // 使用 ksp 替代 kapt
    ksp(libs.hilt.compiler)
    // ViewModel 基础支持
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // 状态观察优化（比 collectAsState 更安全）
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(files("libs/ymg-pdf-viewer-release.aar"))
    implementation("io.github.oothp:pdfium-android:1.9.5-beta01")

    // Room 数据库依赖
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // 使用 ksp 处理 Room 注解
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.ktx)


    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation ("com.google.firebase:firebase-messaging")
    //AD
    implementation("com.google.android.gms:play-services-ads:25.0.0")
    implementation("com.google.ads.mediation:applovin:13.5.1.0")
    implementation("com.google.ads.mediation:vungle:7.7.1.0")
    implementation("com.google.ads.mediation:facebook:6.21.0.1")
    implementation("com.google.ads.mediation:mintegral:17.0.91.0")
    implementation("com.google.ads.mediation:pangle:7.9.1.0.0")
    //fb
    implementation ("com.facebook.android:facebook-android-sdk:18.1.3")
    //installer
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}