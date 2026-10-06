package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ArchitectureExplorerEngine
import com.example.engine.DrRoidEngine
import com.example.engine.FileGeneratorEngine
import com.example.engine.GsiPorterEngine
import com.example.engine.KeyMakerEngine
import com.example.engine.RomWorkspaceManager
import com.example.engine.SignerEngine
import com.example.engine.TerminalExecutor
import com.example.model.ActionType
import com.example.model.ApkItem
import com.example.model.ArchitectureNode
import com.example.model.DrRoidReport
import com.example.model.FodDifferentialReport
import com.example.model.KeyProfile
import com.example.model.LogLevel
import com.example.model.RomFormat
import com.example.model.TerminalEntry
import com.example.model.UnpackedProject
import com.example.model.VoletTab
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GenesisViewModel(application: Application) : AndroidViewModel(application) {

  val workspace = RomWorkspaceManager(application)
  val drRoid = DrRoidEngine(workspace)
  val gsiPorter = GsiPorterEngine(workspace)
  val signer = SignerEngine(workspace)
  val keyMaker = KeyMakerEngine(workspace)
  val fileGen = FileGeneratorEngine(workspace)
  val archExplorer = ArchitectureExplorerEngine()
  val terminalExecutor = TerminalExecutor(workspace, drRoid, gsiPorter, signer, keyMaker, fileGen)

  // Navigation & Volets
  private val _currentTab = MutableStateFlow(VoletTab.ACTIONS)
  val currentTab: StateFlow<VoletTab> = _currentTab.asStateFlow()

  private val _currentAction = MutableStateFlow(ActionType.UNPACK_REPACK)
  val currentAction: StateFlow<ActionType> = _currentAction.asStateFlow()

  // App Settings (Android 16/17 Material You Theme)
  private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  private val _isRootEnabled = MutableStateFlow(false)
  val isRootEnabled: StateFlow<Boolean> = _isRootEnabled.asStateFlow()

  // Storage permission status
  private val _hasStoragePermission = MutableStateFlow(checkStoragePermissionInternal())
  val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

  // Data States
  private val _unpackedProjects = MutableStateFlow<List<UnpackedProject>>(emptyList())
  val unpackedProjects: StateFlow<List<UnpackedProject>> = _unpackedProjects.asStateFlow()

  private val _selectedProject = MutableStateFlow<UnpackedProject?>(null)
  val selectedProject: StateFlow<UnpackedProject?> = _selectedProject.asStateFlow()

  private val _architectureTree = MutableStateFlow<List<ArchitectureNode>>(emptyList())
  val architectureTree: StateFlow<List<ArchitectureNode>> = _architectureTree.asStateFlow()

  private val _selectedArchitectureNode = MutableStateFlow<ArchitectureNode?>(null)
  val selectedArchitectureNode: StateFlow<ArchitectureNode?> = _selectedArchitectureNode.asStateFlow()

  private val _availableApks = MutableStateFlow<List<ApkItem>>(emptyList())
  val availableApks: StateFlow<List<ApkItem>> = _availableApks.asStateFlow()

  private val _existingKeys = MutableStateFlow<List<KeyProfile>>(emptyList())
  val existingKeys: StateFlow<List<KeyProfile>> = _existingKeys.asStateFlow()

  private val _logs = MutableStateFlow<List<TerminalEntry>>(emptyList())
  val logs: StateFlow<List<TerminalEntry>> = _logs.asStateFlow()

  private val _latestDrRoidReport = MutableStateFlow<DrRoidReport?>(null)
  val latestDrRoidReport: StateFlow<DrRoidReport?> = _latestDrRoidReport.asStateFlow()

  private val _latestFodReport = MutableStateFlow<FodDifferentialReport?>(FodDifferentialReport())
  val latestFodReport: StateFlow<FodDifferentialReport?> = _latestFodReport.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _busyMessage = MutableStateFlow("")
  val busyMessage: StateFlow<String> = _busyMessage.asStateFlow()

  private val _unpackProgress = MutableStateFlow<Float?>(null)
  val unpackProgress: StateFlow<Float?> = _unpackProgress.asStateFlow()

  private val _unpackStepText = MutableStateFlow("")
  val unpackStepText: StateFlow<String> = _unpackStepText.asStateFlow()

  init {
    loadInitialData()
  }

  private fun checkStoragePermissionInternal(): Boolean {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
      } else {
        true
      }
    } catch (_: Throwable) {
      false
    }
  }

  fun refreshStoragePermission() {
    _hasStoragePermission.value = checkStoragePermissionInternal()
  }

  fun setTab(tab: VoletTab) {
    _currentTab.value = tab
  }

  fun setAction(action: ActionType) {
    _currentAction.value = action
    _currentTab.value = VoletTab.ACTIONS
    if (action == ActionType.EXPLORER) {
      _selectedProject.value?.let { loadArchitectureTree(it) }
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
  }

  fun setRootEnabled(enabled: Boolean) {
    _isRootEnabled.value = enabled
    addLog(TerminalEntry(level = LogLevel.INFO, message = if (enabled) "Mode Root activé (accès su direct)" else "Mode sans root actif (profils matériel embarqués)", tag = "ROOT"))
  }

  fun selectProject(project: UnpackedProject) {
    _selectedProject.value = project
    loadArchitectureTree(project)
  }

  fun deleteProject(project: UnpackedProject) {
    viewModelScope.launch {
      workspace.deleteProject(project.id)
      addLog(TerminalEntry(level = LogLevel.INFO, message = "Projet supprimé : ${project.name}", tag = "WORKSPACE"))
      refreshProjects()
      if (_selectedProject.value?.id == project.id) {
        _selectedProject.value = _unpackedProjects.value.firstOrNull()
        _selectedProject.value?.let { loadArchitectureTree(it) }
      }
    }
  }

  fun cleanTempProjects() {
    viewModelScope.launch {
      val removed = workspace.cleanTempProjects()
      addLog(TerminalEntry(level = LogLevel.SUCCESS, message = "Nettoyage : $removed projets temporaires supprimés.", tag = "WORKSPACE"))
      refreshProjects()
      if (_selectedProject.value != null && !_unpackedProjects.value.any { it.id == _selectedProject.value?.id }) {
        _selectedProject.value = _unpackedProjects.value.firstOrNull()
        _selectedProject.value?.let { loadArchitectureTree(it) }
      }
    }
  }

  fun loadArchitectureTree(project: UnpackedProject) {
    viewModelScope.launch {
      val nodes = archExplorer.buildArchitectureTree(project)
      _architectureTree.value = nodes
      _selectedArchitectureNode.value = nodes.firstOrNull()
    }
  }

  fun selectArchitectureNode(node: ArchitectureNode) {
    _selectedArchitectureNode.value = node
  }

  private fun addLog(entry: TerminalEntry) {
    _logs.value = _logs.value + entry
  }

  fun clearLogs() {
    _logs.value = listOf(TerminalEntry(level = LogLevel.INFO, message = "Historique et logs du terminal effacés.", tag = "TERMINAL"))
  }

  fun loadInitialData() {
    viewModelScope.launch {
      addLog(TerminalEntry(level = LogLevel.INFO, message = "Démarrage de GENESIS Kitchen - Ultimate On-Device ROM Lab", tag = "GENESIS"))
      val projects = workspace.initializeWorkspace { addLog(it) }
      _unpackedProjects.value = projects
      if (projects.isNotEmpty() && _selectedProject.value == null) {
        val initial = projects.first()
        _selectedProject.value = initial
        loadArchitectureTree(initial)
      }
      _availableApks.value = signer.listAvailableApks()
      _existingKeys.value = keyMaker.listExistingKeys()
    }
  }

  fun executeTerminalCommand(cmd: String) {
    if (cmd.trim().equals("clear", ignoreCase = true)) {
      clearLogs()
      return
    }
    viewModelScope.launch {
      terminalExecutor.executeCommand(cmd, _isRootEnabled.value) { addLog(it) }
      refreshProjects()
    }
  }

  fun unpackFromUri(uri: Uri, displayName: String) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Décompression de $displayName..."
      _unpackProgress.value = 0.05f
      _unpackStepText.value = "Initialisation de la décompression..."
      try {
        val proj = workspace.unpackFromUri(
          uri = uri,
          displayName = displayName,
          onProgress = { p, step ->
            _unpackProgress.value = p
            _unpackStepText.value = step
          },
          onLog = { addLog(it) }
        )
        refreshProjects()
        _selectedProject.value = proj
        loadArchitectureTree(proj)
        _availableApks.value = signer.listAvailableApks()
        addLog(TerminalEntry(level = LogLevel.SUCCESS, message = "Projet '${proj.name}' prêt (${proj.fileCount} fichiers extraits).", tag = "UNPACK"))
      } catch (e: Exception) {
        addLog(TerminalEntry(level = LogLevel.ERROR, message = "Erreur décompression : ${e.localizedMessage}", tag = "UNPACK"))
      } finally {
        _isBusy.value = false
        _unpackProgress.value = null
        _unpackStepText.value = ""
      }
    }
  }

  fun loadSampleDemoProject() {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Déploiement du projet de référence Xiaomi Tucana..."
      _unpackProgress.value = 0.20f
      _unpackStepText.value = "Génération de l'arborescence AOSP Tucana..."
      try {
        val proj = workspace.seedSampleProject { addLog(it) }
        refreshProjects()
        _selectedProject.value = proj
        loadArchitectureTree(proj)
        _availableApks.value = signer.listAvailableApks()
      } catch (e: Exception) {
        addLog(TerminalEntry(level = LogLevel.ERROR, message = "Erreur déploiement modèle : ${e.localizedMessage}", tag = "WORKSPACE"))
      } finally {
        _isBusy.value = false
        _unpackProgress.value = null
        _unpackStepText.value = ""
      }
    }
  }

  fun autoUnpackImage(sourceName: String) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Détection et extraction automatique..."
      _unpackProgress.value = 0.10f
      _unpackStepText.value = "Analyse de $sourceName..."
      try {
        val proj = workspace.autoUnpackImage(
          sourceName = sourceName,
          onProgress = { p, step ->
            _unpackProgress.value = p
            _unpackStepText.value = step
          },
          onLog = { addLog(it) }
        )
        refreshProjects()
        _selectedProject.value = proj
        loadArchitectureTree(proj)
        _availableApks.value = signer.listAvailableApks()
      } finally {
        _isBusy.value = false
        _unpackProgress.value = null
        _unpackStepText.value = ""
      }
    }
  }

  fun repackProject(project: UnpackedProject, format: RomFormat) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Repack intelligent Tool-Tree..."
      try {
        workspace.repackFolder(project, format) { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun makeGsiSpecific(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Intégration matérielle Xiaomi Tucana..."
      try {
        gsiPorter.makeGsiSpecific(project) { addLog(it) }
        refreshProjects()
        loadArchitectureTree(project)
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun runFodFixer(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Calibration & Patch FOD optique..."
      try {
        val report = gsiPorter.runFodFixer(project) { addLog(it) }
        _latestFodReport.value = report
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun extractDeviceBlobs() {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Extraction des HAL & Blobs du device..."
      try {
        gsiPorter.extractBlobsAndHal(_isRootEnabled.value) { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun toggleApkSelection(apk: ApkItem) {
    _availableApks.value = _availableApks.value.map {
      if (it.path == apk.path) it.copy(isSelected = !it.isSelected) else it
    }
  }

  fun signSingleApk(apk: ApkItem) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Signature de ${apk.name}..."
      try {
        val signed = signer.signSingleApk(apk, "releasekey") { addLog(it) }
        _availableApks.value = _availableApks.value.map { if (it.path == apk.path) signed else it }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun batchSignSelectedApks() {
    val selected = _availableApks.value.filter { it.isSelected }
    val toSign = if (selected.isEmpty()) _availableApks.value else selected
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Signature par lot (${toSign.size} APKs)..."
      try {
        signer.batchSignApks(toSign, "releasekey") { addLog(it) }
        _availableApks.value = signer.listAvailableApks()
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun rebuildSystemTrust(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Recréation du système de confiance de A à Z..."
      try {
        signer.rebuildEntireSystemTrust(project) { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun generateAospKeys() {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Génération de la suite de clés AOSP..."
      try {
        keyMaker.generateAospKeySuite(onLog = { addLog(it) })
        _existingKeys.value = keyMaker.listExistingKeys()
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun generateCustomKey(alias: String, cn: String, org: String, years: Int, algo: String) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Génération de la clé $alias..."
      try {
        keyMaker.generateCustomKey(alias, cn, org, years, algo) { addLog(it) }
        _existingKeys.value = keyMaker.listExistingKeys()
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun deodexRom(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Dé-odexing chirurgical de la ROM..."
      try {
        fileGen.deodexRom(project) { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun generateOdexVdex(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Génération Odex & Vdex AOT..."
      try {
        fileGen.generateOdexVdex(project, "arm64") { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun generateFsConfig(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Génération de la table fs_config..."
      try {
        fileGen.generateFsConfig(project) { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun generateFsvmeta(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Signature fsvmeta dm-verity..."
      try {
        fileGen.generateFsvmeta(project) { addLog(it) }
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun inspectDrRoid(project: UnpackedProject) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "Dr Roid : Matrice diagnostique 6-étapes..."
      try {
        val report = drRoid.inspectAndSynthesize(project) { addLog(it) }
        _latestDrRoidReport.value = report
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun executeMakeIt(project: UnpackedProject, format: RomFormat) {
    viewModelScope.launch {
      _isBusy.value = true
      _busyMessage.value = "MAKE IT ! Synthèse chirurgicale en cours..."
      try {
        drRoid.executeMakeIt(project, format) { addLog(it) }
        refreshProjects()
      } finally {
        _isBusy.value = false
      }
    }
  }

  fun refreshProjects() {
    _unpackedProjects.value = workspace.listUnpackedProjects()
    _availableApks.value = signer.listAvailableApks()
  }
}
