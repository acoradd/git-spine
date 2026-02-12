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

            // Coil
            implementation(libs.coil)
            implementation(libs.coil.okhttp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs) { exclude(group = "org.jetbrains.compose.material") }
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.kotlinx.serialization.json)

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
            packageName = "gitspine"
            packageVersion = "1.0.4"
            description = "GitSpine"
            vendor = "Accoradd"

            windows {
                iconFile.set(
                    project.file("src/commonMain/composeResources/drawable/logo.ico")
                )
                packageName = "GitSpine"
                menuGroup = "GitSpine"
                shortcut = true
                dirChooser = true
                upgradeUuid = "7F38F64C-F584-4285-B653-DF0A3EA4F508"
            }

            macOS {
                iconFile.set(
                    project.file("src/commonMain/composeResources/drawable/icon.icns")
                )
                bundleID = "fr.accoradd.gitspine"
                packageName = "GitSpine"
                packageBuildVersion  = packageVersion
                dmgPackageVersion = packageVersion
            }

            linux {
                iconFile.set(
                    project.file("src/commonMain/composeResources/drawable/logo-256.png")
                )
                packageName = "gitspine"
                debMaintainer = "thomas.darocha@accoradd.fr"
                menuGroup = "Development"
                appCategory = "Development"
                shortcut = true
            }

        }
    }
}
