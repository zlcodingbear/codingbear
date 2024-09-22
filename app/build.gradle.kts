plugins {
    id("com.android.application")
}

android {
    namespace = "sku.jyj.example.silvia"
    compileSdk = 34

    defaultConfig {
        applicationId = "sku.jyj.example.silvia"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildFeatures {
        viewBinding =  true
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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12") // 서버 통신에 필요한 라이브러리 설치
    implementation("androidx.preference:preference-ktx:1.2.1") //설정을 위한 preference 라이브러리를 추가
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Retrofit2
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") //
    implementation("com.squareup.retrofit2:converter-scalars:2.6.4") // Scalars 변환기 라이브러리
    implementation("de.hdodenhof:circleimageview:3.1.0") //원형 imageView 사용 라이브러리
 }