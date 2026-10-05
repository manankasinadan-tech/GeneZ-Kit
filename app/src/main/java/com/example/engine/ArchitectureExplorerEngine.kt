package com.example.engine

import com.example.model.ArchitectureCategory
import com.example.model.ArchitectureNode
import com.example.model.UnpackedProject
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArchitectureExplorerEngine {

  suspend fun buildArchitectureTree(project: UnpackedProject): List<ArchitectureNode> = withContext(Dispatchers.IO) {
    val projectDir = File(project.path)
    val nodes = mutableListOf<ArchitectureNode>()

    // 1. Framework System Server & Bootclasspath
    nodes.add(
      ArchitectureNode(
        path = "system/framework/services.jar",
        name = "services.jar",
        category = ArchitectureCategory.FRAMEWORK,
        sizeBytes = 28_450_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:system_file:s0",
        roleDescription = "Cœur de SystemServer Android : héberge WindowManager, ActivityManager, FingerprintService & PowerManager.",
        dependencies = listOf(
          "system/framework/framework.jar",
          "system/framework/core-libart.jar",
          "system/apex/com.android.runtime.apex",
          "services.odex (compilation AOT dex2oat)",
          "services.vdex (code DEX décompressé vérifié)"
        ),
        dependedOnBy = listOf(
          "system/bin/app_process64 (zygote)",
          "Toutes les applications système & interfaces utilisateurs",
          "Service FOD optique (FingerprintService)"
        ),
        initServiceBinding = "Lancé par Zygote lors du bootstrap SystemServer"
      )
    )

    nodes.add(
      ArchitectureNode(
        path = "system/framework/framework.jar",
        name = "framework.jar",
        category = ArchitectureCategory.FRAMEWORK,
        sizeBytes = 14_200_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:system_file:s0",
        roleDescription = "Bibliothèque publique Android Framework API (android.*, com.android.internal.*).",
        dependencies = listOf("system/apex/com.android.runtime.apex", "core-libart.jar"),
        dependedOnBy = listOf("system/framework/services.jar", "Tous les APKs système et tiers"),
        initServiceBinding = "Inclus dans BOOTCLASSPATH global"
      )
    )

    // 2. Hardware Abstraction Layers (HALs) & Biometrics FOD
    nodes.add(
      ArchitectureNode(
        path = "vendor/bin/hw/android.hardware.biometrics.fingerprint@2.1-service.xiaomi_tucana",
        name = "fingerprint@2.1-service.xiaomi_tucana",
        category = ArchitectureCategory.HALS_SERVICES,
        sizeBytes = 145_000L,
        uidGid = "1000:1000 (system:system)",
        permissions = "0755 (-rwxr-xr-x)",
        selinuxContext = "u:object_r:hal_fingerprint_tucana_exec:s0",
        roleDescription = "Démon matériel optique FOD dédié Xiaomi Tucana (Goodix GF5288). Communique avec le capteur sous l'écran.",
        dependencies = listOf(
          "vendor/lib64/hw/fingerprint.tucana.so",
          "vendor/lib64/libhidlbase.so",
          "vendor/lib64/libbinder.so",
          "/dev/goodix_fp (nœud de périphérique noyau)"
        ),
        dependedOnBy = listOf(
          "system/framework/services.jar (BiometricService)",
          "vendor.xiaomi.hardware.fingerprintextension@1.0"
        ),
        initServiceBinding = "/vendor/etc/init/android.hardware.biometrics.fingerprint@2.1-service.xiaomi_tucana.rc"
      )
    )

    nodes.add(
      ArchitectureNode(
        path = "vendor/lib64/hw/fingerprint.tucana.so",
        name = "fingerprint.tucana.so",
        category = ArchitectureCategory.HALS_SERVICES,
        sizeBytes = 280_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:vendor_file:s0",
        roleDescription = "Module HAL propriétaire Xiaomi : calibration des coordonnées FOD (X=440 Y=1830) et DimLayer OLED.",
        dependencies = listOf("libion.so", "libhardware.so", "liblog.so"),
        dependedOnBy = listOf("android.hardware.biometrics.fingerprint@2.1-service.xiaomi_tucana"),
        initServiceBinding = "Chargé dynamiquement via dlopen() par le HAL"
      )
    )

    // 3. APEX Runtime Packages
    nodes.add(
      ArchitectureNode(
        path = "system/apex/com.android.runtime.apex",
        name = "com.android.runtime.apex",
        category = ArchitectureCategory.APEX,
        sizeBytes = 42_000_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:system_file:s0",
        roleDescription = "Module APEX critique contenant la Bionic Libc, Libm, Libdl et l'éditeur de liens dynamique (linker64).",
        dependencies = listOf("Partition /system de base"),
        dependedOnBy = listOf(
          "Absolument tous les processus natifs et Java de l'appareil",
          "/system/bin/init",
          "/system/bin/app_process64 (Zygote)"
        ),
        initServiceBinding = "Monté par apexd au tout premier stade d'init"
      )
    )

    nodes.add(
      ArchitectureNode(
        path = "system/apex/com.android.art.apex",
        name = "com.android.art.apex",
        category = ArchitectureCategory.APEX,
        sizeBytes = 64_000_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:system_file:s0",
        roleDescription = "Machine virtuelle Android Runtime (ART) : compilateur JIT/AOT dex2oat, ramasse-miettes (GC) et libart.so.",
        dependencies = listOf("com.android.runtime.apex"),
        dependedOnBy = listOf("system/framework/services.jar", "Zygote (app_process64)"),
        initServiceBinding = "Monté lors du bootstrap initial apexd"
      )
    )

    // 4. Init Scripts & Service Definitions
    nodes.add(
      ArchitectureNode(
        path = "vendor/etc/init/android.hardware.biometrics.fingerprint@2.1-service.xiaomi_tucana.rc",
        name = "fingerprint.xiaomi_tucana.rc",
        category = ArchitectureCategory.INIT_CONFIG,
        sizeBytes = 480L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:vendor_configs_file:s0",
        roleDescription = "Script d'initialisation déclenchant le service vendor.fps_hal lors de l'évènement 'class late_start'.",
        dependencies = listOf("vendor/bin/hw/android.hardware.biometrics.fingerprint@2.1-service.xiaomi_tucana"),
        dependedOnBy = listOf("/system/bin/init (parseur de scripts rc)"),
        initServiceBinding = "service vendor.fps_hal (class late_start, user system, group system input uhid)"
      )
    )

    nodes.add(
      ArchitectureNode(
        path = "system/etc/init/hw/init.rc",
        name = "init.rc",
        category = ArchitectureCategory.INIT_CONFIG,
        sizeBytes = 28_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0640 (-rw-r-----)",
        selinuxContext = "u:object_r:rootfs:s0",
        roleDescription = "Table maîtresse d'orchestration du démarrage Linux Android (early-init, init, late-init, boot).",
        dependencies = listOf("/system/bin/init"),
        dependedOnBy = listOf("Tous les services et démons du système"),
        initServiceBinding = "Script exécuté directement par le PID 1 (init)"
      )
    )

    // 5. SELinux Security Policies
    nodes.add(
      ArchitectureNode(
        path = "system/etc/selinux/plat_file_contexts",
        name = "plat_file_contexts",
        category = ArchitectureCategory.SELINUX,
        sizeBytes = 148_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:file_contexts_file:s0",
        roleDescription = "Table d'étiquetage des contextes de sécurité MAC de la partition system (chemin -> contexte SELinux).",
        dependencies = listOf("system/etc/selinux/plat_sepolicy.cil"),
        dependedOnBy = listOf("setfiles", "restorecon", "fs_config", "Dr Roid Repack Engine"),
        initServiceBinding = "Chargé par le noyau et init lors de l'application de la politique"
      )
    )

    nodes.add(
      ArchitectureNode(
        path = "vendor/etc/selinux/vendor_file_contexts",
        name = "vendor_file_contexts",
        category = ArchitectureCategory.SELINUX,
        sizeBytes = 94_000L,
        uidGid = "0:0 (root:root)",
        permissions = "0644 (-rw-r--r--)",
        selinuxContext = "u:object_r:file_contexts_file:s0",
        roleDescription = "Table d'étiquetage spécifique au matériel vendor Xiaomi Tucana (HALs, pilotes, nœuds /dev).",
        dependencies = listOf("vendor/etc/selinux/vendor_sepolicy.cil"),
        dependedOnBy = listOf("hal_fingerprint_tucana", "vold", "ueventd"),
        initServiceBinding = "Combiné avec plat_file_contexts au démarrage"
      )
    )

    // 6. Native Executables
    nodes.add(
      ArchitectureNode(
        path = "system/bin/app_process64",
        name = "app_process64 (Zygote)",
        category = ArchitectureCategory.BINARIES,
        sizeBytes = 48_000L,
        uidGid = "0:2000 (root:shell)",
        permissions = "0755 (-rwxr-xr-x)",
        selinuxContext = "u:object_r:zygote_exec:s0",
        roleDescription = "Processus Zygote 64-bit : initialise la VM ART, précharge les classes framework et fork chaque nouvelle application.",
        dependencies = listOf("com.android.art.apex", "com.android.runtime.apex", "framework.jar"),
        dependedOnBy = listOf("SystemServer", "Toutes les activités et applications Android"),
        initServiceBinding = "service zygote /system/bin/app_process64 -Xzygote /system/bin --zygote --start-system-server"
      )
    )

    nodes.add(
      ArchitectureNode(
        path = "system/bin/surfaceflinger",
        name = "surfaceflinger",
        category = ArchitectureCategory.BINARIES,
        sizeBytes = 180_000L,
        uidGid = "1000:1003 (system:graphics)",
        permissions = "0755 (-rwxr-xr-x)",
        selinuxContext = "u:object_r:surfaceflinger_exec:s0",
        roleDescription = "Gestionnaire de composition graphique Android : gère les buffers d'écran, VSYNC, et le DimLayer FOD.",
        dependencies = listOf("vendor/lib64/hw/gralloc.*.so", "libgui.so", "libEGL.so"),
        dependedOnBy = listOf("WindowManagerService", "FOD Display Controller"),
        initServiceBinding = "service surfaceflinger /system/bin/surfaceflinger (class core, priority -20)"
      )
    )

    nodes
  }
}
