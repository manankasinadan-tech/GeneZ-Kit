package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.model.ActionType
import com.example.viewmodel.GenesisViewModel

@Composable
fun ActionHostScreen(
  viewModel: GenesisViewModel,
  currentAction: ActionType
) {
  val projects by viewModel.unpackedProjects.collectAsState()
  val selectedProject by viewModel.selectedProject.collectAsState()
  val apks by viewModel.availableApks.collectAsState()
  val keys by viewModel.existingKeys.collectAsState()
  val drRoidReport by viewModel.latestDrRoidReport.collectAsState()
  val fodReport by viewModel.latestFodReport.collectAsState()
  val isBusy by viewModel.isBusy.collectAsState()
  val busyMsg by viewModel.busyMessage.collectAsState()
  val unpackProgress by viewModel.unpackProgress.collectAsState()
  val unpackStepText by viewModel.unpackStepText.collectAsState()
  val isRoot by viewModel.isRootEnabled.collectAsState()
  val archTree by viewModel.architectureTree.collectAsState()
  val selectedNode by viewModel.selectedArchitectureNode.collectAsState()
  val hasStoragePerm by viewModel.hasStoragePermission.collectAsState()

  when (currentAction) {
    ActionType.UNPACK_REPACK -> {
      UnpackRepackScreen(
        projects = projects,
        selectedProject = selectedProject,
        hasStoragePermission = hasStoragePerm,
        isBusy = isBusy,
        busyMessage = busyMsg,
        unpackProgress = unpackProgress,
        unpackStepText = unpackStepText,
        onSelectProject = { viewModel.selectProject(it) },
        onDeleteProject = { viewModel.deleteProject(it) },
        onCleanTempProjects = { viewModel.cleanTempProjects() },
        onUnpackUri = { uri, fileName -> viewModel.unpackFromUri(uri, fileName) },
        onRepack = { proj, fmt -> viewModel.repackProject(proj, fmt) },
        onLoadSampleDemo = { viewModel.loadSampleDemoProject() },
        onRefreshWorkspace = { viewModel.refreshProjects() },
        onRefreshStoragePermission = { viewModel.refreshStoragePermission() },
        onNavigateToAction = { viewModel.setAction(it) }
      )
    }

    ActionType.EXPLORER -> {
      ArchitectureExplorerScreen(
        projects = projects,
        selectedProject = selectedProject,
        architectureNodes = archTree,
        selectedNode = selectedNode,
        onSelectProject = { viewModel.selectProject(it) },
        onSelectNode = { viewModel.selectArchitectureNode(it) }
      )
    }

    ActionType.GSI_PORTER -> {
      GsiPorterScreen(
        projects = projects,
        selectedProject = selectedProject,
        isRoot = isRoot,
        fodReport = fodReport,
        isBusy = isBusy,
        busyMessage = busyMsg,
        onSelectProject = { viewModel.selectProject(it) },
        onExtractBlobs = { viewModel.extractDeviceBlobs() },
        onMakeGsiSpecific = { viewModel.makeGsiSpecific(it) },
        onRunFodFixer = { viewModel.runFodFixer(it) }
      )
    }

    ActionType.SIGNER -> {
      SignerScreen(
        apks = apks,
        projects = projects,
        selectedProject = selectedProject,
        isBusy = isBusy,
        busyMessage = busyMsg,
        onToggleApkSelection = { viewModel.toggleApkSelection(it) },
        onSignSingleApk = { viewModel.signSingleApk(it) },
        onBatchSignApks = { viewModel.batchSignSelectedApks() },
        onRebuildSystemTrust = { viewModel.rebuildSystemTrust(it) },
        onSelectProject = { viewModel.selectProject(it) }
      )
    }

    ActionType.KEY_MAKER -> {
      KeyMakerScreen(
        keys = keys,
        isBusy = isBusy,
        busyMessage = busyMsg,
        onGenerateAospKeys = { viewModel.generateAospKeys() },
        onGenerateCustomKey = { alias, cn, org, years, algo ->
          viewModel.generateCustomKey(alias, cn, org, years, algo)
        }
      )
    }

    ActionType.FILE_GENERATOR -> {
      FileGeneratorScreen(
        projects = projects,
        selectedProject = selectedProject,
        isBusy = isBusy,
        busyMessage = busyMsg,
        onSelectProject = { viewModel.selectProject(it) },
        onDeodex = { viewModel.deodexRom(it) },
        onGenerateOdexVdex = { viewModel.generateOdexVdex(it) },
        onGenerateFsConfig = { viewModel.generateFsConfig(it) },
        onGenerateFsvmeta = { viewModel.generateFsvmeta(it) }
      )
    }

    ActionType.MAKE_IT -> {
      MakeItScreen(
        projects = projects,
        selectedProject = selectedProject,
        report = drRoidReport,
        isBusy = isBusy,
        busyMessage = busyMsg,
        onSelectProject = { viewModel.selectProject(it) },
        onInspectDrRoid = { viewModel.inspectDrRoid(it) },
        onExecuteMakeIt = { proj, fmt -> viewModel.executeMakeIt(proj, fmt) }
      )
    }
  }
}
