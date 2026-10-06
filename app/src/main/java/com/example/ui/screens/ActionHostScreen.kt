package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.model.ActionType
import com.example.viewmodel.GenesisViewModel

@Composable
fun ActionHostScreen(
  viewModel: GenesisViewModel,
  currentAction: ActionType
) {
  val projects = viewModel.unpackedProjects.value
  val selectedProject = viewModel.selectedProject.value
  val apks = viewModel.availableApks.value
  val keys = viewModel.existingKeys.value
  val drRoidReport = viewModel.latestDrRoidReport.value
  val fodReport = viewModel.latestFodReport.value
  val isBusy = viewModel.isBusy.value
  val busyMsg = viewModel.busyMessage.value
  val isRoot = viewModel.isRootEnabled.value
  val archTree = viewModel.architectureTree.value
  val selectedNode = viewModel.selectedArchitectureNode.value
  val hasStoragePerm = viewModel.hasStoragePermission.value

  when (currentAction) {
    ActionType.UNPACK_REPACK -> {
      UnpackRepackScreen(
        projects = projects,
        selectedProject = selectedProject,
        hasStoragePermission = hasStoragePerm,
        isBusy = isBusy,
        busyMessage = busyMsg,
        onSelectProject = { viewModel.selectProject(it) },
        onDeleteProject = { viewModel.deleteProject(it) },
        onCleanTempProjects = { viewModel.cleanTempProjects() },
        onUnpackUri = { uri, fileName -> viewModel.unpackFromUri(uri, fileName) },
        onRepack = { proj, fmt -> viewModel.repackProject(proj, fmt) },
        onRefreshWorkspace = { viewModel.loadInitialData() },
        onRefreshStoragePermission = { viewModel.refreshStoragePermission() }
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
