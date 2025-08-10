import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)

        klib {
            enabled.set(true)
        }
    }
    sourceSets.androidInstrumentedTest.dependencies {
        implementation(kotlin("test"))
    }
}

android {
    namespace = "dev.mihon.image.decoder"
    compileSdk = 36
    ndkVersion = "28.2.13676358"

    defaultConfig {
        minSdk = 21

        consumerProguardFiles += file("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                targets += "imagedecoder" // Changed from ep_image-decoder
            }
        }
    }

    buildTypes {
        release {

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt") // Changed path
        }
    }
}

dependencies {
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
}

mavenPublishing {
    coordinates("dev.mihon.image", "decoder", "0.0.1")

    pom {
        name.set("image-decoder")
        description.set("Image decoder for Mihon")
        url.set("https://github.com/mihon/image-decoder")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("MPL-2.0")
                url.set("https://www.mozilla.org/en-US/MPL/2.0")
                distribution.set("repo")
            }
        }

        organization {
            name.set("Mihon Open Source Project")
            url.set("https://github.com/mihon")
        }

        developers {
            developer {
                id.set("antsylich")
                name.set("AntsyLich")
                url.set("https://github.com/AntsyLich")
            }
        }

        scm {
            url.set("https://github.com/mihon/image-decoder")
            connection.set("scm:git:https://github.com/mihon/image-decoder.git")
            developerConnection.set("scm:git:ssh://git@github.com/mihon/image-decoder.git")
        }

        issueManagement {
            system.set("github")
            url.set("https://github.com/mihon/image-decoder/issues")
        }

        ciManagement {
            system.set("github")
            url.set("https://github.com/mihon/image-decoder/actions")
        }
    }
}
