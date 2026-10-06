package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
  onSelectProject: (UnpackedProject) -> Unit,
  onDeleteProject: (UnpackedProject) -> Unit,
  onCleanTempProjects: () -> Unit,
  onUnpackUri: (uri: Uri, fileName: String) -> Unit,
  onRepack: (project: UnpackedProject, format: RomFormat) -> Unit,
  onRefreshWorkspace: () -> Unit,
  onRefreshStoragePermission: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Unpack, 1: Repack
  val context = LocalContext.current

  // Selected picked file state
  var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
  var selectedFileName by remember { mutableStateOf<String?>(null) }
  var chosenRepackFormat by remember { mutableStateOf(RomFormat.AUTO_DETECT) }

  // Real Android File Picker Launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedFileUri = uri
      var name = "custom_image.img"
      try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
          }
        }
      } catch (_: Exception) {}
      selectedFileName = name
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
        subtitle = "Sélection de fichier binaire • UKA & Tool-Tree",
        icon = Icons.Rounded.Inventory2
      )
    }

    // Storage Permission Banner if not granted on Android 11+
    if (!hasStoragePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
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
              text = "Pour lire et écrire les fichiers ROM dans /sdcard/FORGER, accordez l'accès complet aux fichiers.",
              fontSize = 11.5.sp,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = {
                try {
                  val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                  }
                  context.startActivity(intent)
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
              Text("Autoriser l'accès aux fichiers", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

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
      // 1. FILE PICKER UNPACK PANEL
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
                text = "Choisir une image à décompresser",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = "Auto-détection", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Sélectionnez n'importe quel fichier (.img, .bin, .raw, GSI, super partition, payload) depuis votre téléphone. Le format et la partition sont identifiés automatiquement.",
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
                text = if (selectedFileName != null) "Changer de fichier" else "Parcourir le téléphone...",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            // DISPLAY SELECTED FILE DETAILS
            if (selectedFileName != null && selectedFileUri != null) {
              Spacer(modifier = Modifier.height(14.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                  .padding(12.dp)
              ) {
                Column {
                  Text(
                    text = "Fichier sélectionné :",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = selectedFileName!!,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = "Dossier de destination : FORGER/UNPACKED/${selectedFileName!!.substringBeforeLast(".")}",
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
                  .height(48.dp)
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

      // PRESET IMAGES TO TEST IMMEDIATELY
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Ou tester avec un gabarit pré-inclus :",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("gsi_lineage21_arm64.img", "tucana_vendor.img").forEach { sample ->
                OutlinedButton(
                  onClick = {
                    selectedFileName = sample
                    selectedFileUri = Uri.parse("android.resource://${context.packageName}/raw/$sample")
                  },
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text(sample, fontSize = 11.sp)
                }
              }
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
      }
    }

    if (isBusy) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = MaterialTheme.colorScheme.primary,
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
              text = busyMessage,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
