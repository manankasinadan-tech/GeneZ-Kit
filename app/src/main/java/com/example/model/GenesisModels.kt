package com.example.model

enum class ActionType(val label: String, val iconName: String, val subtitle: String) {
  UNPACK_REPACK("Unpack - Repack", "Inventory2", "Images & GSI Unpacker / Repacker"),
  EXPLORER("Architecture Explorer", "AccountTree", "Arborescence & Interdépendances"),
  GSI_PORTER("GSI PORTER", "PhonelinkSetup", "Tucana & Vendor Hardware Adapter"),
  SIGNER("SIGNER", "VerifiedUser", "APK & Full System Trust Authority"),
  KEY_MAKER("KEY MAKER", "VpnKey", "AOSP Release Keys Studio"),
  FILE_GENERATOR("FILE GENERATOR", "Code", "Vdex, Odex, Oat & fs_config Factory"),
  MAKE_IT("MAKE IT !", "AutoAwesome", "Dr Roid Surgical Orchestrator")
}

enum class VoletTab(val title: String, val iconName: String) {
  ACTIONS("Actions", "Tune"),
  TERMINAL("Terminal", "Terminal"),
  SETTINGS("Paramètres", "Settings")
}

enum class RomFormat(val displayName: String, val extension: String, val description: String) {
  AUTO_DETECT("Auto (Détection)", "", "Préserve le format source d'origine"),
  EXT4("ext4", ".img", "Linux ext4 classique lecture/écriture"),
  EROFS("EROFS", ".img", "Système de fichiers moderne haute compression"),
  F2FS("F2FS", ".img", "Optimisé pour mémoires flash NAND"),
  SPARSE_IMG("Sparse Image", ".img", "Format sparse décompressé par fastboot"),
  RAW_IMG("Raw Partition", ".raw", "Image brute bloc sans entête sparse"),
  SUPER_IMG("Super Container", ".img", "Conteneur dynamique multi-partitions LP"),
  PAYLOAD_BIN("OTA Payload", ".bin", "Archive de mise à jour A/B Android")
}

data class UnpackedProject(
  val id: String,
  val name: String,
  val partitionName: String,
  val path: String,
  val originalFormat: RomFormat,
  var targetFormat: RomFormat = originalFormat,
  val sizeBytes: Long,
  val fileCount: Int,
  val androidVersion: String = "14.0 (UDC)",
  val targetDevice: String = "tucana (Mi Note 10)",
  val buildFlavor: String = "lineage_tucana-userdebug",
  val hasApex: Boolean = true,
  val hasFodHal: Boolean = true,
  val sepolicyCount: Int = 1840,
  val lastModified: Long = System.currentTimeMillis()
)

enum class ArchitectureCategory(val label: String) {
  ALL("Tous"),
  FRAMEWORK("Framework & DEX"),
  HALS_SERVICES("HALs & Services"),
  APEX("Modules APEX"),
  INIT_CONFIG("Init & Configs"),
  SELINUX("SELinux & Politiques"),
  BINARIES("Binaires Natifs")
}

data class ArchitectureNode(
  val path: String,
  val name: String,
  val category: ArchitectureCategory,
  val sizeBytes: Long,
  val uidGid: String = "0:0 (root:root)",
  val permissions: String = "0755 (-rwxr-xr-x)",
  val selinuxContext: String = "u:object_r:system_file:s0",
  val roleDescription: String,
  val dependencies: List<String> = emptyList(),
  val dependedOnBy: List<String> = emptyList(),
  val initServiceBinding: String? = null,
  val isFolder: Boolean = false,
  val children: List<ArchitectureNode> = emptyList()
)

data class ApkItem(
  val path: String,
  val name: String,
  val sizeBytes: Long,
  val isSigned: Boolean = true,
  val schemeV1: Boolean = true,
  val schemeV2: Boolean = true,
  val schemeV3: Boolean = true,
  val schemeV4: Boolean = false,
  val certSha256: String = "4F:E2:B1:89:9A:3C:D0:6F:42:10:8C:F5:8A:13:B9:4C",
  val certSha1: String = "7D:89:C4:21:05:BF:EA:33:91:20:AC:48",
  val certMd5: String = "3A:5B:C9:82:1D:44:0E:17",
  val issuer: String = "CN=Android, OU=GENESIS Kitchen, O=AOSP Lab, C=FR",
  val isSystemApk: Boolean = false,
  var isSelected: Boolean = false
)

data class KeyProfile(
  val id: String,
  val name: String,
  val alias: String,
  val algorithm: String = "RSA 4096",
  val validityYears: Int = 25,
  val commonName: String = "Genesis Release Key",
  val organization: String = "Genesis Rom Lab",
  val generatedFiles: List<String> = emptyList(),
  val createdAt: Long = System.currentTimeMillis()
)

data class FodDifferentialReport(
  val detectedSensor: String = "Goodix GF5288 In-Display Optical",
  val deviceVendorHal: String = "vendor.xiaomi.hardware.fingerprintextension@1.0",
  val sourceGsiHal: String = "android.hardware.biometrics.fingerprint@2.1-service",
  val screenResolution: String = "1080x2340 AMOLED (6.47\")",
  val fodCoordinates: String = "X=440, Y=1830, Radius=95",
  val dimLayerRequired: Boolean = true,
  val dimLayerGamma: Float = 0.85f,
  val vendorSepolicyMatched: Boolean = true,
  val solutionPatches: List<String> = listOf(
    "Injection du wrapper HIDL FOD Xiaomi Tucana",
    "Patch framework-res config_fodPosition et dim_layer_color",
    "Génération du script d'init vendor.fod.rc dans /vendor/etc/init",
    "Alignement des contextes SELinux hal_fingerprint_tucana_exec"
  ),
  val isFixed: Boolean = false
)

enum class LogLevel {
  INFO,
  SUCCESS,
  WARNING,
  ERROR,
  COMMAND,
  DR_ROID
}

data class TerminalEntry(
  val id: Long = System.currentTimeMillis() + (0..999).random(),
  val timestamp: Long = System.currentTimeMillis(),
  val level: LogLevel = LogLevel.INFO,
  val message: String,
  val tag: String = "SYSTEM"
)

data class DrRoidDiagnosticStep(
  val title: String,
  val description: String,
  val status: String = "PASSED", // PASSED, WARNING, FAILED, RUNNING
  val detail: String = ""
)

data class DrRoidReport(
  val id: String,
  val projectName: String,
  val timestamp: Long,
  val totalFiles: Int,
  val astNodesCount: Int,
  val symlinksCount: Int,
  val sepolicyContextsVerified: Int,
  val apexIntegrityPassed: Boolean,
  val geometrySparsityRatio: Float,
  val estimatedRepackSizeBytes: Long,
  val sanityVerdict: String,
  val isReadyForPacking: Boolean,
  val diagnosticSteps: List<DrRoidDiagnosticStep> = emptyList(),
  val auditLog: List<String>
)
