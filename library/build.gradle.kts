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
    }

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)

        klib {
            enabled.set(true)
        }
    }
}

android {
    namespace = "dev.mihon.image.decoder"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        externalNativeBuild { cmake {  } }
    }

    ndkVersion = "28.2.13676358"

    externalNativeBuild {
        cmake {
            path = file("src/commonMain/cpp/CMakeLists.txt")
        }
    }
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
