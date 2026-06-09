import java.io.FileInputStream
import java.security.MessageDigest

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}

/**
 * Root-level task: Generate and print SHA1 fingerprints from the release keystore
 * for both Control Panel (app) and Target Device (client) modules.
 *
 * Usage: ./gradlew printSha1Keys
 *
 * The output will contain the SHA1 keys that must be added to Firebase Console
 * under Project Settings > Your Apps > SHA certificate fingerprints.
 */
tasks.register("printSha1Keys") {
    group = "verification"
    description = "Prints SHA1 fingerprints from release keystore for both apps"

    doLast {
        val keystorePaths = listOf(
            "app/abuzahra-release.jks" to "Control Panel (com.abuzahra.manager)",
            "client/abuzahra-release.jks" to "Target Device (com.abuzahra.tracker)"
        )

        println("")
        println("============================================================")
        println("  SHA1 Key Generation - Abu Zahra Project")
        println("============================================================")
        println("  These SHA1 keys must be added to Firebase Console:")
        println("  Firebase Console > Project Settings > Your Apps > SHA fingerprints")
        println("============================================================")
        println("")

        for ((keystorePath, appName) in keystorePaths) {
            val keystoreFile = file(keystorePath)
            if (!keystoreFile.exists()) {
                println("  [$appName]")
                println("  ERROR: Keystore not found at: $keystorePath")
                println("")
                continue
            }

            try {
                val fis = FileInputStream(keystoreFile)
                val ks = java.security.KeyStore.getInstance("JKS")
                ks.load(fis, "abuzahra2024".toCharArray())
                fis.close()

                println("  ╔══════════════════════════════════════════════════════╗")
                println("  ║  $appName")
                println("  ╚══════════════════════════════════════════════════════╝")

                val alias = "abuzahra"
                val cert = ks.getCertificate(alias)
                val md = MessageDigest.getInstance("SHA1")
                val sha1Bytes = md.digest(cert.encoded)

                val sha1Hex = sha1Bytes.joinToString(":") {
                    String.format("%02X", it)
                }

                // Also generate SHA-256
                val md256 = MessageDigest.getInstance("SHA-256")
                val sha256Bytes = md256.digest(cert.encoded)
                val sha256Hex = sha256Bytes.joinToString(":") {
                    String.format("%02X", it)
                }

                // Also generate MD5
                val md5 = MessageDigest.getInstance("MD5")
                val md5Bytes = md5.digest(cert.encoded)
                val md5Hex = md5Bytes.joinToString(":") {
                    String.format("%02X", it)
                }

                println("  SHA1:   $sha1Hex")
                println("  SHA256: $sha256Hex")
                println("  MD5:    $md5Hex")
                println("  Keystore: $keystorePath")
                println("  Alias: $alias")
                println("")
            } catch (e: Exception) {
                println("  [$appName]")
                println("  ERROR reading keystore: ${e.message}")
                println("")
            }
        }

        println("============================================================")
        println("  Copy the SHA1 keys above and add them to Firebase Console:")
        println("  https://console.firebase.google.com/")
        println("  > Select project: abu-zahra-control-787676787951")
        println("  > Project Settings (gear icon)")
        println("  > Your Apps")
        println("  > Select each app")
        println("  > Add SHA certificate fingerprint")
        println("============================================================")
        println("")
    }
}

/**
 * Per-module SHA1 tasks
 */
subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.application")) {
            tasks.register("printSha1") {
                group = "verification"
                description = "Print SHA1 fingerprint for this module"
                doLast {
                    val keystorePath = if (name == "app") "app/abuzahra-release.jks" else "client/abuzahra-release.jks"
                    val keystoreFile = rootProject.file(keystorePath)
                    if (!keystoreFile.exists()) {
                        println("ERROR: Keystore not found at: $keystorePath")
                        return@doLast
                    }

                    val fis = FileInputStream(keystoreFile)
                    val ks = java.security.KeyStore.getInstance("JKS")
                    ks.load(fis, "abuzahra2024".toCharArray())
                    fis.close()

                    val cert = ks.getCertificate("abuzahra")
                    val md = MessageDigest.getInstance("SHA1")
                    val sha1Bytes = md.digest(cert.encoded)
                    val sha1Hex = sha1Bytes.joinToString(":") {
                        String.format("%02X", it)
                    }

                    println("")
                    println("  Module: $name")
                    println("  Package: ${android.defaultConfig.applicationId}")
                    println("  SHA1: $sha1Hex")
                    println("")
                }
            }
        }
    }
}
