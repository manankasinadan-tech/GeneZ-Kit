package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Verified
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DrRoidReport
import com.example.model.RomFormat
import com.example.model.UnpackedProject
import com.example.ui.components.CyberBadge
import com.example.ui.components.ProjectSelectorDropdown
import com.example.ui.components.SectionHeader

@Composable
fun MakeItScreen(
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  report: DrRoidReport?,
  isBusy: Boolean,
  busyMessage: String,
  onSelectProject: (UnpackedProject) -> Unit,
  onInspectDrRoid: (UnpackedProject) -> Unit,
  onExecuteMakeIt: (UnpackedProject, RomFormat) -> Unit
) {
  var selectedOutputFormat by remember { mutableStateOf(RomFormat.EROFS) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "MAKE IT ! • Moteur Dr Roid",
        subtitle = "Orchestration industrielle & Contrôle déclaratif 6-étapes",
        icon = Icons.Rounded.AutoAwesome
      )
    }

    // DR ROID ENGINE CARD
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
                  .size(40.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Rounded.Psychology,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Dr Roid Super Cerveau v5.0",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Moteur d'orchestration 100% on-device hors-ligne",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            CyberBadge(text = "Autonome", color = MaterialTheme.colorScheme.primary)
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Dr Roid agit comme un environnement d'ingénierie AOSP complet : il cartographie l'arborescence en AST, élimine les violations SELinux, audite les modules APEX et recalcule l'alignement 4KB pour garantir un démarrage sans échec.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    item {
      Text(
        text = "ROM Unpackée à traiter",
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
      // 1-CLICK MAKE IT CONTAINER
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "MAKE IT ! (Rebuild Chirurgical)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = "Garantie Bootloader OK", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Exécute en 1-clic : Audit Déclaratif AST -> Alignement SELinux -> Correction des interdépendances -> Recalcul de géométrie 4KB -> Assemblage et Scellement binaire.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { onInspectDrRoid(selectedProject) },
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("btn_inspect_ast"),
                shape = RoundedCornerShape(12.dp),
                enabled = !isBusy
              ) {
                Icon(Icons.Rounded.DataObject, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vérifier (AST)", fontSize = 12.sp)
              }

              Button(
                onClick = { onExecuteMakeIt(selectedProject, selectedOutputFormat) },
                modifier = Modifier
                  .weight(1.4f)
                  .height(48.dp)
                  .testTag("btn_execute_make_it"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isBusy
              ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("MAKE IT !", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }

      // DR ROID 6-STAGE DIAGNOSTIC CHECKLIST
      if (report != null) {
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
                  Icon(
                    Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Matrice de Diagnostic Dr Roid",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                }
                CyberBadge(text = "Zero Defect", color = MaterialTheme.colorScheme.primary)
              }

              Spacer(modifier = Modifier.height(14.dp))

              // List of 6 Verification Steps
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                report.diagnosticSteps.forEach { step ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                      .padding(10.dp),
                    verticalAlignment = Alignment.Top
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.CheckCircle,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = step.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Rapport JSON complet généré dans FORGER/REPORTS/${report.id}.json",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
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
