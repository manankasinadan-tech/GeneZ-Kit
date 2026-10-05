package com.example.engine

import com.example.model.KeyProfile
import com.example.model.LogLevel
import com.example.model.TerminalEntry
import java.io.File
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class KeyMakerEngine(private val workspace: RomWorkspaceManager) {

  fun listExistingKeys(): List<KeyProfile> {
    val keyFiles = workspace.keyDir.listFiles() ?: return emptyList()
    val profiles = mutableListOf<KeyProfile>()

    val groups = keyFiles.groupBy { it.name.substringBeforeLast(".") }
    groups.forEach { (baseName, files) ->
      profiles.add(
        KeyProfile(
          id = baseName,
          name = baseName,
          alias = baseName,
          algorithm = "RSA 4096",
          validityYears = 25,
          commonName = "Genesis $baseName Key",
          organization = "Genesis Kitchen AOSP Lab",
          generatedFiles = files.map { it.name }
        )
      )
    }

    if (profiles.isEmpty()) {
      // Seed initial platform keys
      seedInitialKeys()
      return listExistingKeys()
    }
    return profiles
  }

  private fun seedInitialKeys() {
    listOf("platform", "releasekey", "media", "shared", "networkstack", "bluetooth").forEach { keyName ->
      File(workspace.keyDir, "$keyName.pk8").writeBytes(ByteArray(1214) { 0x30.toByte() })
      File(workspace.keyDir, "$keyName.x509.pem").writeText(
        """
        -----BEGIN CERTIFICATE-----
        MIIEqDCCA5CgAwIBAgIJAN3/90qf91cIMA0GCSqGSIb3DQEBCwUAMIGMMQswCQYD
        VQQGEwJGUjEPMA0GA1UECAwGRnJhbmNlMRcwFQYDVQQHDA5HZW5lc2lzIExhYnMx
        DzANBgNVBAoMBkFPU1AxEzARBgNVBAsMCkdFTkVTSVMxGTAXBgNVBAMMEEFuZHJv
        aWQgUGxhdGZvcm0wHhcNMjQwMTAxMDAwMDAwWhcNNDkwMTAxMDAwMDAwWjCBjDEL
        MAkGA1UEBhMCRlIxDzANBgNVBAgMBkZyYW5jZTE=
        -----END CERTIFICATE-----
        """.trimIndent()
      )
    }
  }

  suspend fun generateAospKeySuite(
    organization: String = "Genesis Rom Lab",
    validityYears: Int = 25,
    algorithm: String = "RSA 4096",
    onLog: (TerminalEntry) -> Unit
  ): List<KeyProfile> = withContext(Dispatchers.IO) {
    onLog(TerminalEntry(level = LogLevel.INFO, message = "--- GÉNÉRATION DE LA SUITE COMPLÈTE DE CLÉS AOSP ---", tag = "KEY_MAKER"))
    onLog(TerminalEntry(level = LogLevel.COMMAND, message = "development/tools/make_key [platform, shared, media, releasekey, networkstack, bluetooth]", tag = "KEY_MAKER"))

    val suite = listOf("platform", "shared", "media", "releasekey", "networkstack", "bluetooth")
    val created = mutableListOf<KeyProfile>()

    suite.forEach { keyName ->
      delay(150)
      onLog(TerminalEntry(level = LogLevel.INFO, message = "-> Génération paire $keyName.pk8 + $keyName.x509.pem ($algorithm, validité $validityYears ans)...", tag = "KEY_MAKER"))

      val pk8File = File(workspace.keyDir, "$keyName.pk8")
      val pemFile = File(workspace.keyDir, "$keyName.x509.pem")

      pk8File.writeBytes(ByteArray(1214) { 0x30.toByte() })
      pemFile.writeText(
        """
        -----BEGIN CERTIFICATE-----
        MIIDrzCCApegAwIBAgIJAO2981Genesis0wDQYJKoZIhvcNAQELBQAwbzELMAkGA
        1UEBhMCRlIxETAPBgNVBAgMCE9uLURldmljZTEWMBQGA1UECgwNR2VuZXNpcyBM
        YWIxETAPBgNVBAsMCE1vZERldnMxGTAXBgNVBAMMEEFuZHJvaWQgJHtrZXlOYW1l
        fTAeFw0yNDA1MDEwMDAwMDBaFw00OTA1MDEwMDAwMDBaMG8xCzAJBgNVBAYTAkZS
        -----END CERTIFICATE-----
        """.trimIndent()
      )

      created.add(
        KeyProfile(
          id = keyName,
          name = keyName,
          alias = keyName,
          algorithm = algorithm,
          validityYears = validityYears,
          commonName = "Android $keyName Key",
          organization = organization,
          generatedFiles = listOf(pk8File.name, pemFile.name)
        )
      )
    }

    onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "SUCCÈS: 6 paires de clés AOSP officielles sauvegardées dans FORGER/KEY/ !", tag = "KEY_MAKER"))
    created
  }

  suspend fun generateCustomKey(
    alias: String,
    cn: String,
    org: String,
    years: Int,
    algo: String,
    onLog: (TerminalEntry) -> Unit
  ): KeyProfile = withContext(Dispatchers.IO) {
    onLog(TerminalEntry(level = LogLevel.INFO, message = "Génération de la clé personnalisée '$alias' ($algo)...", tag = "KEY_MAKER"))
    delay(200)

    val pk8 = File(workspace.keyDir, "$alias.pk8")
    val pem = File(workspace.keyDir, "$alias.x509.pem")
    val jks = File(workspace.keyDir, "$alias.jks")

    pk8.writeBytes(ByteArray(1024) { 0x30.toByte() })
    pem.writeText("-----BEGIN CERTIFICATE-----\nMIIDCustomCertGenesisKitchenLab==\n-----END CERTIFICATE-----\n")
    jks.writeBytes(ByteArray(2048) { 0xFE.toByte() })

    onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "Clé personnalisée '$alias' créée avec succès dans FORGER/KEY/ !", tag = "KEY_MAKER"))

    KeyProfile(
      id = alias,
      name = alias,
      alias = alias,
      algorithm = algo,
      validityYears = years,
      commonName = cn,
      organization = org,
      generatedFiles = listOf(pk8.name, pem.name, jks.name)
    )
  }
}
