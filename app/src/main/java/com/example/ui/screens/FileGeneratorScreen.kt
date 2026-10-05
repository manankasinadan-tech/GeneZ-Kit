package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UnpackedProject
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.ProjectSelectorDropdown
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberCore
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen

@Composable
fun FileGeneratorScreen(
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  isBusy: Boolean,
  busyMessage: String,
  onSelectProject: (UnpackedProject) -> Unit,
  onDeodex: (UnpackedProject) -> Unit,
  onGenerateOdexVdex: (UnpackedProject) -> Unit,
  onGenerateFsConfig: (UnpackedProject) -> Unit,
  onGenerateFsvmeta: (UnpackedProject) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "FILE GENERATOR Studio",
        subtitle = "Génération & Nettoyage Vdex, Odex, Oat, fsvmeta & fs_config",
        icon = Icons.Rounded.Code,
        accentColor = CyberCyan
      )
    }

    item {
      Text(
        text = "Projet cible (FORGER/UNPACKED)",
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

    if (selectedProject != null) {
      // 1. DEODEX MODULE
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = CrimsonAlert.copy(alpha = 0.5f)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = CrimsonAlert)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Dé-odexing Chirurgical (Universel)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
              CyberBadge(text = "Nettoyage Binaire", color = CrimsonAlert)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Supprime les fichiers pré-optimisés .odex, .vdex, .art et .oat pour ré-extraire et ré-injecter directement les fichiers classes.dex dans chaque APK et JAR. Rend la ROM modifiable et compatible tout appareil.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { onDeodex(selectedProject) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_deodex_rom"),
              colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert, contentColor = Color.White),
              shape = RoundedCornerShape(12.dp),
              enabled = !isBusy
            ) {
              Text("DÉ-ODEXER LA ROM (SUPPRIMER PREBUILTS ET RÉTABLIR CLASSES.DEX)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }

      // 2. ODEX / VDEX COMPILATION
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = AmberCore
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Speed, contentDescription = null, tint = AmberCore)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Compilation AOT (Odex, Vdex, Oat)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
              CyberBadge(text = "dex2oat Accélération", color = AmberCore)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Recompile les dex en code natif machine (arm64-v8a) et génère les archives .vdex et .odex pour services.jar et framework.jar afin d'optimiser la vitesse de premier démarrage.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { onGenerateOdexVdex(selectedProject) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_gen_odex_vdex"),
              colors = ButtonDefaults.buttonColors(containerColor = AmberCore, contentColor = Color.White),
              shape = RoundedCornerShape(12.dp),
              enabled = !isBusy
            ) {
              Text("GÉNÉRER LES FICHIERS VDEX, ODEX & OAT (ARM64)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }

      // 3. FS_CONFIG GENERATOR
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = CyberCyan
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.FormatListNumbered, contentDescription = null, tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Générateur de Table fs_config",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
              CyberBadge(text = "Permissions POSIX", color = CyberCyan)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Parcourt l'arborescence complète et calcule la table POSIX stricte (UID=0 root, GID=2000 shell, permissions 755/644, symlinks et capabilities linux) pour garantir qu'aucun fichier critique ne soit verrouillé au boot.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { onGenerateFsConfig(selectedProject) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_gen_fs_config"),
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
              shape = RoundedCornerShape(12.dp),
              enabled = !isBusy
            ) {
              Text("GÉNÉRER LA TABLE FS_CONFIG (UID / GID / CHMOD)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }

      // 4. FSVMETA GENERATOR
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = EmeraldGreen
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Security, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Générateur fsvmeta (fs-verity)",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
              CyberBadge(text = "Anti-Bootloop AVB", color = EmeraldGreen)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Signe les fichiers avec fs-verity et génère les métadonnées fsvmeta requises par Android 11+ pour les partitions en lecture seule, évitant le rejet par dm-verity.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { onGenerateFsvmeta(selectedProject) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_gen_fsvmeta"),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color(0xFF003314)),
              shape = RoundedCornerShape(12.dp),
              enabled = !isBusy
            ) {
              Text("GÉNÉRER LES SIGNATURES FSVMETA (DM-VERITY)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    if (isBusy) {
      item {
        GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = CyberCyan) {
          Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyberCyan, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = busyMessage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
