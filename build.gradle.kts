group = "ru.egor9814.app"
version = "0.1.0-SNAPSHOT"

plugins {
    kotlin("multiplatform") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// Define build types for application and native libraries
val buildType = "Debug"

// Determine host
val hostOs = System.getProperty("os.name") ?: ""
val isArm64 = System.getProperty("os.arch") == "aarch64"
val isMacFamily = hostOs.startsWith("mac os", true)
val isLinuxFamily = hostOs.startsWith("linux", true)
val isWindowsFamily = hostOs.startsWith("windows", true)

// Directory for built libraries
val libsDir = layout.buildDirectory.dir("libs").get()
// Directory for SDL sources
val sdlDir = layout.projectDirectory.dir("src/nativeInterop/cinterop/sdl")
// Directory for SDL built files and CMake cache
val sdlBuildDir = sdlDir.dir("build")

kotlin {
    val nativeTarget = when {
        isMacFamily && isArm64 -> macosArm64("native")
        isMacFamily && !isArm64 -> macosX64("native")
        isLinuxFamily && isArm64 -> linuxArm64("native")
        isLinuxFamily && !isArm64 -> linuxX64("native")
        isWindowsFamily -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable(
                "nfspe", buildTypes = setOf(
                    when (buildType) {
                        "Debug" -> DEBUG
                        "Release" -> RELEASE
                        else -> throw GradleException("Unknown native build type: $buildType")
                    }
                )
            ) {
//                entryPoint = "main"
            }
        }

        val main by compilations.getting {
            cinterops {
                val sdl by creating {
                    defFile(sdlDir.file("sdl.def"))
                    packageName("org.sdl")
                    includeDirs {
                        allHeaders(libsDir.dir("include"))
                    }
//                    extraOpts("-libraryPath", libsDir)
                }
            }
        }
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC")
                implementation("org.jetbrains.kotlinx:atomicfu:0.23.1")
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0-RC")
            }
        }
    }
}

tasks {
    // CMake configuration
    val sdlConfigure by creating(Exec::class) {
        workingDir(sdlDir)
        commandLine = listOf(
            "cmake", // CMake from PATH variable
            "-DCMAKE_BUILD_TYPE=${buildType}", // Specifying build type
            "-DSDL_TOOLCHAIN=${cmakeToolchain()}", // Specifying compilers
            "-DSDL_ARTIFACTS_DIR=${libsDir}", // Specifying output directory for headers and library
            "-B$sdlBuildDir", // Specifying directory for building and cache
            "-S$sdlDir", // Specifying source directory
        )
    }

    // CMake building
    val sdlBuild by creating(Exec::class) {
        workingDir(sdlDir)
        commandLine = listOf(
            "cmake",
            "--build", sdlBuildDir.toString(), // Specifying build directory
            "--target", "sdl_cinterop", // Specifying target for build
        )
        dependsOn(sdlConfigure)
    }

    // Copy SDL dll/so library to runtime directory
    val sdlCopyRuntimeLibrary by creating(Copy::class) {
        from(libsDir.file("SDL3.${if (isWindowsFamily) "dll" else "so"}"))
        into(layout.buildDirectory.dir("bin/native/nfspe${buildType}Executable"))
        dependsOn(sdlBuild)
    }

    // Make SDL library
    val sdlMake by creating {
        dependsOn(sdlCopyRuntimeLibrary)
    }
}

tasks {
    // Task for building all native libraries
    register("buildInteropLibs") {
        group = "interop"
        description = "Build C interop libraries."

        dependsOn("sdlMake")
    }

    clean {
        delete.add(sdlBuildDir.dir("_deps/sdl3-build")) // Remove real SDL build dir
        delete.add(sdlBuildDir.dir("CMakeFiles")) // Remove cache dir
        delete.add(sdlBuildDir.file("CMakeCache.txt")) // Remove cache file
    }
}

// Get toolchain name for CMake for building SDL library
fun cmakeToolchain(): String {
    // Family name
    val family = when {
        isMacFamily -> throw GradleException("Mac not support yet")
        isLinuxFamily -> throw GradleException("Linux not support yet")
        isWindowsFamily -> "windows"
        else -> throw GradleException("Not supported Host OS")
    }
    val arch = when {
        isArm64 -> "aarch64"
        // TODO: other arch support
        else -> "x64"
    }
    return "$family-$arch-llvm"
}
