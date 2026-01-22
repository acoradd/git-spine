import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(21)

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.navigationCompose)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeVM)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs) { exclude(group = "org.jetbrains.compose.material") }
            implementation(libs.kotlinx.coroutinesSwing)

            // Jewel (IntelliJ UI)
            implementation(libs.jewel)
            implementation(libs.jewel.decorated)
            implementation(libs.jewel.icons)
            implementation(libs.jewel.markdown.core)
            implementation(libs.jewel.markdown.intUiStandaloneStyling)

            // JGit
            implementation(libs.jgit)
            implementation(libs.jgit.ssh.apache)

            // JNA (Native OS dialogs)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }
    }
}


compose.desktop {
    application {
        mainClass = "fr.accoradd.gitspine.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "fr.accoradd.gitspine"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(
                    project.file("src/commonMain/composeResources/drawable/logo.ico")
                )
            }

            macOS {
                iconFile.set(
                    project.file("src/commonMain/composeResources/drawable/icon.icns")
                )
            }

            linux {
                iconFile.set(
                    project.file("src/commonMain/composeResources/drawable/logo-256.png")
                )
            }

        }
    }
}
