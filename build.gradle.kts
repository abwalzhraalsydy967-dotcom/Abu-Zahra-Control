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
        val apps = listOf(
            Triple("app/abuzahra-release.jks", "Control Panel (com.abuzahra.manager)", "com.abuzahra.manager"),
            Triple("client/abuzahra-release.jks", "Target Device (com.abuzahra.tracker)", "com.abuzahra.tracker")
        )

        println("")
        println("============================================================")
        println("  SHA1 Key Generation - Abu Zahra Project")
        println("============================================================")
        println("  These SHA1 keys must be added to Firebase Console:")
        println("  Firebase Console > Project Settings > Your Apps > SHA fingerprints")
        println("============================================================")
        println("")

        for ((keystorePath, appName, packageName) in apps) {
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

                println("  +----------------------------------------------------------+")
                println("  |  $appName")
                println("  +----------------------------------------------------------+")

                val alias = "abuzahra"
                val cert = ks.getCertificate(alias)

                // SHA1
                val md1 = MessageDigest.getInstance("SHA1")
                val sha1Hex = md1.digest(cert.encoded).joinToString(":") { String.format("%02X", it) }

                // SHA-256
                val md256 = MessageDigest.getInstance("SHA-256")
                val sha256Hex = md256.digest(cert.encoded).joinToString(":") { String.format("%02X", it) }

                // MD5
                val md5 = MessageDigest.getInstance("MD5")
                val md5Hex = md5.digest(cert.encoded).joinToString(":") { String.format("%02X", it) }

                println("  Package: $packageName")
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
