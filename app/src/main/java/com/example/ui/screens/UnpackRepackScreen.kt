package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
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
  isBusy: Boolean,
  busyMessage: String,
  onSelectProject: (UnpackedProject) -> Unit,
  onAutoUnpack: (sourceName: String) -> Unit,
  onRepack: (project: UnpackedProject, format: RomFormat) -> Unit,
  onImportFolder: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Unpack, 1: Repack
  var imageFileName by remember { mutableStateOf("system_arm64_lineage21.img") }
  var chosenRepackFormat by remember { mutableStateOf(RomFormat.AUTO_DETECT) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "Unpack & Repack Studio",
        subtitle = "Détection automatique universelle • UKA & Tool-Tree",
        icon = Icons.Rounded.Inventory2
      )
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
      // 100% AUTOMATIC UNPACK PANEL
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
                text = "Décompression Automatique d'Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = "100% Détection Auto", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Aucune sélection manuelle requise : l'outil inspecte l'entête binaire, détecte automatiquement le type d'image (.img, GSI, payload.bin, super partition, sparse, raw, erofs, ext4) ainsi que la partition cible.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
              value = imageFileName,
              onValueChange = { imageFileName = it },
              label = { Text("Fichier d'image binaire à décompresser") },
              modifier = Modifier.fillMaxWidth().testTag("input_unpack_file"),
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
              ),
              shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Example Chips
            Text("Images d'exemples prêtes à tester :", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("gsi_arm64_udc.img", "super_tucana.img", "payload.bin").forEach { sample ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imageFileName = sample }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(text = sample, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = { onAutoUnpack(imageFileName) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_execute_unpack"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              shape = RoundedCornerShape(12.dp),
              enabled = imageFileName.isNotBlank() && !isBusy
            ) {
              Icon(Icons.Rounded.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("LANCER L'UNPACK AUTO (UKA ENGINE)", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      // REPACK PANEL
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
          onProjectSelected = onSelectProject
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = onImportFolder,
            modifier = Modifier.testTag("btn_import_folder"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Actualiser le dossier FORGER", fontSize = 12.sp)
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

              Button(
                onClick = { onRepack(selectedProject, chosenRepackFormat) },
                modifier = Modifier
                  .fillMaxWidth()
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
                Text("REPACK INTELLIGENT (TOOL-TREE)", fontWeight = FontWeight.Bold)
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
