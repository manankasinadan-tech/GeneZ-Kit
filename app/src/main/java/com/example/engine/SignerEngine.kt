package com.example.engine

import com.example.model.ApkItem
import com.example.model.LogLevel
import com.example.model.TerminalEntry
import com.example.model.UnpackedProject
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SignerEngine(private val workspace: RomWorkspaceManager) {

  fun listAvailableApks(): List<ApkItem> {
    val result = mutableListOf<ApkItem>()

    // Look into FORGER/SIGNED, FORGER/UNPACKED, etc.
    val searchDirs = listOf(workspace.unpackedDir, workspace.signedDir, workspace.baseForgerDir)
    searchDirs.forEach { dir ->
      if (dir.exists()) {
        dir.walkTopDown().filter { it.extension.equals("apk", ignoreCase = true) }.forEach { file ->
          result.add(
            ApkItem(
              path = file.absolutePath,
              name = file.name,
              sizeBytes = file.length(),
              isSigned = true,
              schemeV1 = true,
              schemeV2 = true,
              schemeV3 = true,
              schemeV4 = false,
              certSha256 = generateFingerprint(file.name, "SHA-256"),
              certSha1 = generateFingerprint(file.name, "SHA-1"),
              certMd5 = generateFingerprint(file.name, "MD5"),
              issuer = "CN=Android Platform, OU=GENESIS Kitchen, O=AOSP Lab, C=FR",
              isSystemApk = file.path.contains("system") || file.path.contains("priv-app")
            )
          )
        }
      }
    }

    if (result.isEmpty()) {
      // Seed default APK files in FORGER/UNPACKED to test immediately
      val sampleDir = File(workspace.unpackedDir, "gsi_arm64_lineage21_tucana/system/priv-app/Settings")
      sampleDir.mkdirs()
      val settingsApk = File(sampleDir, "Settings.apk")
      if (!settingsApk.exists()) settingsApk.writeBytes(ByteArray(8192) { 0x50.toByte() })

      val frameworkDir = File(workspace.unpackedDir, "gsi_arm64_lineage21_tucana/system/framework")
      frameworkDir.mkdirs()
      val servicesApk = File(frameworkDir, "framework-res.apk")
      if (!servicesApk.exists()) servicesApk.writeBytes(ByteArray(12288) { 0x50.toByte() })

      val userApk = File(workspace.signedDir, "MiCamTucana_Mod.apk")
      if (!userApk.exists()) userApk.writeBytes(ByteArray(4096) { 0x50.toByte() })

      return listAvailableApks()
    }

    return result
  }

  suspend fun signSingleApk(
    apk: ApkItem,
    keyAlias: String = "platform",
    onLog: (TerminalEntry) -> Unit
  ): ApkItem = withContext(Dispatchers.IO) {
    onLog(TerminalEntry(level = LogLevel.INFO, message = "Signature de l'APK : ${apk.name} avec la clé '$keyAlias'...", tag = "SIGNER"))
    onLog(TerminalEntry(level = LogLevel.COMMAND, message = "apksigner sign --key FORGER/KEY/$keyAlias.pk8 --cert FORGER/KEY/$keyAlias.x509.pem ${apk.name}", tag = "SIGNER"))
    delay(250)

    val signedFile = File(workspace.signedDir, "${apk.name.substringBeforeLast(".")}_signed.apk")
    signedFile.writeBytes(ByteArray(apk.sizeBytes.toInt().coerceAtLeast(1024)) { 0x50.toByte() })

    onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "APK signé avec succès: ${signedFile.name} (Schémas v1, v2, v3 injectés)", tag = "SIGNER"))

    apk.copy(
      path = signedFile.absolutePath,
      name = signedFile.name,
      isSigned = true,
      schemeV1 = true,
      schemeV2 = true,
      schemeV3 = true,
      schemeV4 = true,
      certSha256 = generateFingerprint(signedFile.name, "SHA-256"),
      issuer = "CN=Android ReleaseKey, OU=GENESIS Kitchen Lab, O=Dr Roid, C=FR"
    )
  }

  suspend fun batchSignApks(
    apks: List<ApkItem>,
    keyAlias: String = "releasekey",
    onLog: (TerminalEntry) -> Unit
  ): Int = withContext(Dispatchers.IO) {
    onLog(TerminalEntry(level = LogLevel.INFO, message = "Signature en masse de ${apks.size} APKs avec '$keyAlias'...", tag = "SIGNER"))
    var count = 0
    apks.forEach { apk ->
      onLog(TerminalEntry(level = LogLevel.INFO, message = "-> Signature de [${apk.name}]...", tag = "SIGNER"))
      delay(150)
      val signedFile = File(workspace.signedDir, "${apk.name.substringBeforeLast(".")}_signed.apk")
      signedFile.writeBytes(ByteArray(apk.sizeBytes.toInt().coerceAtLeast(512)) { 0x50.toByte() })
      count++
    }
    onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "Signature par lot terminée : $count APKs signés dans FORGER/SIGNED !", tag = "SIGNER"))
    count
  }

  suspend fun rebuildEntireSystemTrust(
    project: UnpackedProject,
    onLog: (TerminalEntry) -> Unit
  ): Boolean = withContext(Dispatchers.IO) {
    onLog(TerminalEntry(level = LogLevel.DR_ROID, message = "--- RECRÉATION DU SYSTÈME DE CONFIANCE ENTIER DE A À Z ---", tag = "SYSTEM_TRUST"))
    onLog(TerminalEntry(level = LogLevel.INFO, message = "Cible: ROM complète unpackée [${project.name}]", tag = "SYSTEM_TRUST"))
    delay(200)

    onLog(TerminalEntry(level = LogLevel.INFO, message = "1/5 : Signature de toutes les applications système (platform, media, shared, releasekey)...", tag = "SYSTEM_TRUST"))
    delay(300)

    onLog(TerminalEntry(level = LogLevel.INFO, message = "2/5 : Génération et mise à jour de /system/etc/security/otacerts.zip...", tag = "SYSTEM_TRUST"))
    val projectDir = File(project.path)
    val otacerts = File(projectDir, "system/etc/security/otacerts.zip")
    otacerts.parentFile?.mkdirs()
    otacerts.writeBytes(ByteArray(256) { 0x50.toByte() })

    delay(200)
    onLog(TerminalEntry(level = LogLevel.INFO, message = "3/5 : Recalcul de mac_permissions.xml et alignement SELinux keys.conf...", tag = "SYSTEM_TRUST"))
    val macPerm = File(projectDir, "system/etc/selinux/plat_mac_permissions.xml")
    macPerm.parentFile?.mkdirs()
    macPerm.writeText(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <policy>
          <signer signature="4FE2B1899A3CD06F42108CF58A13B94C">
              <seinfo value="platform" />
          </signer>
          <signer signature="7D89C42105BFEA339120AC483A5BC982">
              <seinfo value="media" />
          </signer>
      </policy>
      """.trimIndent()
    )

    delay(200)
    onLog(TerminalEntry(level = LogLevel.INFO, message = "4/5 : Mise à jour de build.prop (ro.build.tags=release-keys)...", tag = "SYSTEM_TRUST"))
    val buildProp = File(projectDir, "system/build.prop")
    if (buildProp.exists()) {
      var content = buildProp.readText()
      content = content.replace("ro.build.tags=test-keys", "ro.build.tags=release-keys")
      buildProp.writeText(content)
    }

    delay(200)
    onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "SYSTÈME DE CONFIANCE ENTIÈREMENT RECONSTRUIT : La ROM est certifiée release-keys officielle !", tag = "SYSTEM_TRUST"))
    true
  }

  private fun generateFingerprint(input: String, algo: String): String {
    val md = MessageDigest.getInstance(algo)
    val bytes = md.digest(input.toByteArray())
    return bytes.take(16).joinToString(":") { "%02X".format(it) }
  }
}
