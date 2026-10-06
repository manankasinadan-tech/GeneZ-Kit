package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PhonelinkSetup
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.RomFormat
import com.example.model.UnpackedProject
import com.example.ui.components.CyberBadge
import com.example.ui.components.ProjectSelectorDropdown
import com.example.ui.components.SectionHeader

@Composable
fun UnpackRepackScreen(
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  hasStoragePermission: Boolean,
  isBusy: Boolean,
  busyMessage: String,
  unpackProgress: Float?,
  unpackStepText: String,
  onSelectProject: (UnpackedProject) -> Unit,
  onDeleteProject: (UnpackedProject) -> Unit,
  onCleanTempProjects: () -> Unit,
  onUnpackUri: (uri: Uri, fileName: String) -> Unit,
  onRepack: (project: UnpackedProject, format: RomFormat) -> Unit,
  onLoadSampleDemo: () -> Unit,
  onRefreshWorkspace: () -> Unit,
  onRefreshStoragePermission: () -> Unit,
  onNavigateToAction: (ActionType) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Unpack, 1: Repack
  val context = LocalContext.current

  // Selected picked file state
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var selectedFileName by remember { mutableStateOf<String?>(null) }
  var selectedFileSizeStr by remember { mutableStateOf<String?>(null) }
  var chosenRepackFormat by remember { mutableStateOf(RomFormat.AUTO_DETECT) }

  // Real Android File Picker Launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedFileUri = uri
      var name = "firmware_image.img"
      var sizeBytes: Long = 0
      try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
          }
          if (sizeIndex != -1) {
            sizeBytes = cursor.getLong(sizeIndex)
          }
        }
      } catch (_: Exception) {}
      selectedFileName = name
      selectedFileSizeStr = if (sizeBytes > 0) {
        val mb = sizeBytes / (1024.0 * 1024.0)
        String.format("%.1f Mo", mb)
      } else null
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "Unpack & Repack Studio",
        subtitle = "Sélection de fichier binaire • Moteur UKA & Tool-Tree",
        icon = Icons.Rounded.Inventory2
      )
    }

    // Storage Permission Warning & Action Banner
    if (!hasStoragePermission) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
          )
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Accès au stockage requis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "GENESIS Kitchen a besoin de l'autorisation de stockage pour lire et écrire dans /sdcard/FORGER.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = {
                try {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                      data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                  } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                      data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                  }
                } catch (_: Exception) {
                  val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                  context.startActivity(intent)
                }
              },
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
              )
            ) {
              Icon(Icons.Rounded.Security, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Accorder l'accès aux fichiers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Active Processing Card with Progress Bar
    if (isBusy) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
          )
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = busyMessage,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (unpackStepText.isNotBlank()) {
                  Text(
                    text = unpackStepText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                  )
                }
              }
              if (unpackProgress != null) {
                Text(
                  text = "${(unpackProgress * 100).toInt()}%",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            if (unpackProgress != null) {
              Spacer(modifier = Modifier.height(12.dp))
              LinearProgressIndicator(
                progress = { unpackProgress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }
        }
      }
    }

    // Mode Tabs: Unpack vs Repack
    item {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("UNPACK IMAGE", fontWeight = FontWeight.Bold) },
          modifier = Modifier.testTag("tab_unpack")
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("REPACK DOSSIER", fontWeight = FontWeight.Bold) },
          modifier = Modifier.testTag("tab_repack")
        )
      }
    }

    if (selectedTab == 0) {
      // 1. UNPACK PANEL
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Choisir une image ROM à décompresser",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = "Auto-détection", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Sélectionnez n'importe quel fichier (.img, .bin, .raw, .zip, GSI, payload) depuis votre téléphone. Le format (EROFS, EXT4, Sparse) et la partition sont identifiés automatiquement.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // BIG FILE PICKER BUTTON
            Button(
              onClick = { filePickerLauncher.launch("*/*") },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_pick_file"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Rounded.FileOpen, contentDescription = null, modifier = Modifier.size(22.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = if (selectedFileName != null) "Changer de fichier image" else "Parcourir le téléphone (.img, .bin, GSI, .zip)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
              )
            }

            // SELECTED FILE DETAILS CARD & UNPACK TRIGGER
            if (selectedFileName != null && selectedFileUri != null) {
              Spacer(modifier = Modifier.height(14.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                  .padding(14.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Fichier sélectionné :",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                    if (selectedFileSizeStr != null) {
                      CyberBadge(text = selectedFileSizeStr!!, color = MaterialTheme.colorScheme.secondary)
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = selectedFileName!!,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Dossier cible : FORGER/UNPACKED/${selectedFileName!!.substringBeforeLast(".")}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  onUnpackUri(selectedFileUri!!, selectedFileName!!)
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("btn_execute_unpack"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isBusy
              ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DÉCOMPRESSER CETTE IMAGE (UKA)", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // ACTIVE PROJECT SUMMARY (IF AVAILABLE)
      if (selectedProject != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Projet actif décompressé",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                }
                CyberBadge(text = selectedProject.originalFormat.displayName, color = MaterialTheme.colorScheme.primary)
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text("• Dossier : ${selectedProject.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("• Partition : ${selectedProject.partitionName} • Fichiers répertoriés : ${selectedProject.fileCount}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("• Chemin : ${selectedProject.path}", style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Actions rapides sur ce projet :",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = { onNavigateToAction(ActionType.EXPLORER) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Rounded.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Explorer", fontSize = 11.sp)
                }

                OutlinedButton(
                  onClick = { onNavigateToAction(ActionType.GSI_PORTER) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Rounded.PhonelinkSetup, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("GSI Porter", fontSize = 11.sp)
                }

                OutlinedButton(
                  onClick = { onNavigateToAction(ActionType.SIGNER) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Signer", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }

      // OPTIONAL DEMO / SAMPLE LOADER
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Besoin d'un gabarit de démonstration ?",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Si vous n'avez pas de fichier ROM sous la main, vous pouvez générer un modèle de test Xiaomi Tucana complet avec APKs, binaires et HAL FOD.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = onLoadSampleDemo,
              enabled = !isBusy,
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Générer l'exemple Xiaomi Tucana (AOSP 14)", fontSize = 11.5.sp)
            }
          }
        }
      }
    } else {
      // 2. REPACK PANEL
      item {
        Text(
          text = "Sélection du dossier à repacker",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
      }

      item {
        ProjectSelectorDropdown(
          projects = projects,
          selectedProject = selectedProject,
          onProjectSelected = onSelectProject,
          onDeleteProject = onDeleteProject,
          onCleanTempProjects = onCleanTempProjects
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onCleanTempProjects,
            modifier = Modifier.testTag("btn_clean_temp"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Nettoyer temporaires", fontSize = 11.5.sp)
          }

          CyberBadge(
            text = "Sortie : FORGER/PACKED",
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      if (selectedProject != null) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Métadonnées du dossier unpacké",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                CyberBadge(
                  text = selectedProject.originalFormat.displayName,
                  color = MaterialTheme.colorScheme.secondary
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text("• Nom du projet : ${selectedProject.name}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
              Text("• Cible matérielle : ${selectedProject.targetDevice}", style = MaterialTheme.typography.bodySmall)
              Text("• Version Android : ${selectedProject.androidVersion}", style = MaterialTheme.typography.bodySmall)
              Text("• Partition : ${selectedProject.partitionName}", style = MaterialTheme.typography.bodySmall)
              Text("• Fichiers répertoriés : ${selectedProject.fileCount} éléments", style = MaterialTheme.typography.bodySmall)

              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Format de sortie pour le repack :",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                  RomFormat.AUTO_DETECT,
                  RomFormat.EROFS,
                  RomFormat.EXT4,
                  RomFormat.SPARSE_IMG,
                  RomFormat.SUPER_IMG
                ).forEach { fmt ->
                  val isSel = chosenRepackFormat == fmt
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .background(
                        if (isSel) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                      )
                      .clickable { chosenRepackFormat = fmt }
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = if (fmt == RomFormat.AUTO_DETECT) "Auto" else fmt.name,
                      fontSize = 11.5.sp,
                      fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = { onDeleteProject(selectedProject) },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                  )
                ) {
                  Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Supprimer", fontSize = 12.sp)
                }

                Button(
                  onClick = { onRepack(selectedProject, chosenRepackFormat) },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_execute_repack"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                  ),
                  shape = RoundedCornerShape(12.dp),
                  enabled = !isBusy
                ) {
                  Icon(Icons.Rounded.Compress, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("REPACK (TOOL-TREE)", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Rounded.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Aucun dossier à repacker",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Décompressez d'abord une image dans l'onglet 'UNPACK IMAGE' pour pouvoir la repacker.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
