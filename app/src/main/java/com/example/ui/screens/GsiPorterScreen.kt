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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.PhonelinkSetup
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FodDifferentialReport
import com.example.model.UnpackedProject
import com.example.ui.components.CyberBadge
import com.example.ui.components.ProjectSelectorDropdown
import com.example.ui.components.SectionHeader

@Composable
fun GsiPorterScreen(
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  isRoot: Boolean,
  fodReport: FodDifferentialReport?,
  isBusy: Boolean,
  busyMessage: String,
  onSelectProject: (UnpackedProject) -> Unit,
  onExtractBlobs: () -> Unit,
  onMakeGsiSpecific: (UnpackedProject) -> Unit,
  onRunFodFixer: (UnpackedProject) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "GSI PORTER Studio",
        subtitle = "Adaptation matérielle • Arbre LineageOS Xiaomi Tucana",
        icon = Icons.Rounded.PhonelinkSetup
      )
    }

    // TARGET HARDWARE CARD
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
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Rounded.Smartphone,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Xiaomi Tucana (Mi Note 10)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Snapdragon 730G (sm7150) • Kernel 4.14",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            CyberBadge(
              text = if (isRoot) "Root Détecté" else "Profil Intégré",
              color = if (isRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Écran incurvé AMOLED 1080x2340 avec capteur d'empreinte optique sous l'écran (FOD Goodix GF5288), caméra pentacapteur 108MP et architecture audio Hi-Res.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))
          OutlinedButton(
            onClick = onExtractBlobs,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_extract_blobs"),
            shape = RoundedCornerShape(12.dp),
            enabled = !isBusy
          ) {
            Icon(Icons.Rounded.Hardware, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              if (isRoot) "Extraire HALs & Blobs via Root (/vendor)" else "Charger l'arbre matériel Tucana",
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    item {
      Text(
        text = "Image GSI unpackée cible",
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
      // MAKE GSI SPECIFIC BUTTON & ACTIONS
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
                text = "Make GSI Specific",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = "Vendor Match 100%", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Adapte l'image générique au matériel physique : injection des overlays RRO spécifiques Tucana (découpe d'écran, profil batterie), liaison VNDK 34 et propriétés système Qualcomm sm7150.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { onMakeGsiSpecific(selectedProject) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_make_gsi_specific"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              shape = RoundedCornerShape(12.dp),
              enabled = !isBusy
            ) {
              Icon(Icons.Rounded.Tune, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("MAKE GSI SPECIFIC (TRANSFORMER EN ROM DÉDIÉE)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }

      // FOD FIXER v2
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
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "FOD Fixer v2 (Capteur Sous Écran)",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              CyberBadge(
                text = if (fodReport?.isFixed == true) "FOD Opérationnel" else "Différentiel Prêt",
                color = if (fodReport?.isFixed == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnostic & Differential Matrix Box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(14.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  "ANALYSE DIFFÉRENTIELLE & CALIBRATION FOD :",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text("• Capteur optique : Goodix GF5288 In-Display (Tucana)", fontSize = 12.sp)
                Text("• Coordonnées physiques : X=440, Y=1830, Rayon=95px", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text("• DimLayer OLED : ro.vendor.fod.dimlayer.enable=1 (Gamma 0.85)", fontSize = 12.sp)
                Text("• HAL Propriétaire : vendor.xiaomi.hardware.fingerprintextension@1.0", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (fodReport?.solutionPatches?.isNotEmpty() == true) {
              Text("Actions & Solutions appliquées :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              fodReport.solutionPatches.forEach { patch ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                  Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(patch, style = MaterialTheme.typography.bodySmall)
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { onRunFodFixer(selectedProject) },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_run_fod_fixer"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
              ),
              shape = RoundedCornerShape(12.dp),
              enabled = !isBusy
            ) {
              Icon(Icons.Rounded.Fingerprint, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("RÉPARER LE LECTEUR D'EMPREINTE SOUS ÉCRAN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
          Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.5.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(text = busyMessage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
