# GENESIS Kitchen
### *Ultimate On-Device ROM Lab*

GENESIS Kitchen est un atelier mobile autonome de reverse engineering, modification chirurgicale, portage de GSI et reconstruction d'images de micrologiciels Android (ROMs).

Inspiré des outils légendaires **UKA** (*Universal Kitchen for Android*) et **Tool-Tree**, GENESIS Kitchen opère directement sur appareil Android (avec ou sans accès Root) avec une interface **100% design Android 16 / 17 Material You** (sans artifice de verre liquide, épurée, fluide et hautement optimisée) intégrant les modes Clair, Sombre et Suivi du Système.

---

## 🏛️ Architecture & Organisation de l'Interface

L'application est architecturée autour d'un panneau latéral (*Drawer*) et d'une barre de navigation inférieure à 3 volets :

### 1. Volets Inférieurs (Navigation)
- **Actions** : Affiche l'atelier correspondant à l'outil sélectionné dans la barre latérale.
- **Terminal** : Console interactive temps réel affichant l'historique complet des opérations, les journaux de diagnostic détaillés, ainsi qu'une invite de commande pour exécuter des commandes internes (`dr-roid`, `gsi`, `forger`, `keys`, `sign`) ou des scripts shell système (`su` ou `sh`).
- **Paramètres** : Sélecteur de thème (Suivre le système, Clair, Sombre), bascule du mode Root, gestionnaire de chemins de stockage `FORGER`, manuel des commandes, changelog et documentation intégrée.

### 2. Outils de la Barre Latérale
- 📦 **Unpack - Repack** : Décompression 100% automatique d'images (détection instantanée du format binaire et de la partition sans sélection manuelle) et recompilation intelligente (détection automatique du format d'origine ou conversion vers *ext4*, *EROFS*, *F2FS*, *sparse* ou *raw*).
- 🌳 **Architecture Explorer (NOUVEAU)** : Explorateur de fichiers et d'architecture intelligent dédié aux images unpackées. Il comprend l'agencement Android, catégorise les composants (*Framework*, *HALs & Services*, *APEX*, *Init & Configs*, *SELinux*, *Binaires*) et cartographie les **renvois et interdépendances** entre fichiers (qui dépend de quoi, liaisons init `.rc`, types sepolicy et consommateurs).
- 📱 **GSI PORTER v2** : Adaptation avancée de GSI pour appareil physique, intégrant l'arbre matériel LineageOS **Xiaomi Tucana (Mi Note 10 / Snapdragon 730G)**.
  - **Make GSI specific** : Transforme une image GSI générique en ROM dédiée (liaison VNDK 34, overlays RRO, manifestes VINTF, propriétés `build.prop`).
  - **FOD fixer v2** : Analyse différentielle et calibration du capteur d'empreinte optique sous l'écran (*Goodix GF5288* : X=440 Y=1830), injection des wrappers HIDL et calibrage DimLayer OLED (Gamma 0.85).
- 🔐 **SIGNER** : Explorateur de fichiers dédié au dossier `FORGER`, signature individuelle ou par lot d'APKs, vérification des certificats (v1, v2, v3, v4, SHA-256, SHA-1, MD5) et **Recréation du système de confiance de A à Z** (re-signature de toutes les applications système, recalcul `otacerts.zip`, `mac_permissions.xml` et `ro.build.tags=release-keys`).
- 🔑 **KEY MAKER** : Générateur de clés cryptographiques AOSP officielles (`platform`, `shared`, `media`, `releasekey`, `networkstack`, `bluetooth`) aux formats `.pk8` et `.x509.pem`, ainsi que création de keystores personnalisés.
- ⚙️ **FILE GENERATOR** : Dé-odexing chirurgical (suppression des `.odex`/`.vdex`/`.oat` et restitution des `classes.dex`), compilation AOT dex2oat, génération chirurgicale des tables POSIX `fs_config` et des métadonnées `fsvmeta` (fs-verity / dm-verity).
- ⚡ **MAKE IT ! (Moteur Dr Roid v5.0)** : Super cerveau d'orchestration hors-ligne avec **matrice de diagnostic 6-étapes** :
  1. Topologie & AST déclaratif
  2. Politiques SELinux & contextes MAC (zéro violation neverallow)
  3. APEX Runtime & Bionic linker64
  4. Intégrité AVB 2.0 / dm-verity
  5. Géométrie des blocs & alignement mémoire 4KB
  6. Simulation d'amorçage Zero-Defect et repack 1-clic garanti sans bootloop.

---

## 📁 Structure du Dossier FORGER

Tous les artefacts générés sont structurés dans le répertoire de travail `FORGER` :
```text
FORGER/
├── UNPACKED/     # Arborescences extraites des partitions (system, vendor...)
├── PACKED/       # Images binaires recompilées prêtes au flash fastboot
├── KEY/          # Paires de clés AOSP officielles et certificats x509
├── SIGNED/       # APKs et applications système re-signées
├── REPORTS/      # Rapports d'analyse déclarative AST au format JSON
└── PROFILES/     # Arbres matériels et extractions vendor
```

---

## 🛠️ Compilation & Intégration Continue (GitHub Actions)

Ce projet est prêt pour GitHub et GitHub Actions :
- Configuration Gradle Kotlin DSL moderne (`build.gradle.kts`)
- Target SDK 36, Min SDK 24
- Jetpack Compose & Material 3 Expressive (Android 16 / 17 Design)
- Workflow CI inclus dans `.github/workflows/android.yml`
