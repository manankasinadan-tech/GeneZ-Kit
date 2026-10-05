package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkItem
import com.example.model.UnpackedProject
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.ProjectSelectorDropdown
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberCore
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen

@Composable
fun SignerScreen(
  apks: List<ApkItem>,
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  isBusy: Boolean,
  busyMessage: String,
  onToggleApkSelection: (ApkItem) -> Unit,
  onSignSingleApk: (ApkItem) -> Unit,
  onBatchSignApks: () -> Unit,
  onRebuildSystemTrust: (UnpackedProject) -> Unit,
  onSelectProject: (UnpackedProject) -> Unit
) {
  var inspectedApk by remember { mutableStateOf<ApkItem?>(apks.firstOrNull()) }
  val selectedCount = apks.count { it.isSelected }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "SIGNER & Authority Studio",
        subtitle = "Signature v1/v2/v3/v4 & Recréation du Système de Confiance A à Z",
        icon = Icons.Rounded.VerifiedUser,
        accentColor = CyberCyan
      )
    }

    // SYSTEM TRUST RECREATION FROM A TO Z
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
              Icon(Icons.Rounded.LockReset, contentDescription = null, tint = AmberCore)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Recréer le Système de Confiance de A à Z",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }
            CyberBadge(text = "Système Complet", color = AmberCore)
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Resigne toutes les applications système (/system/app, /priv-app), recalcule otacerts.zip, regénère mac_permissions.xml et force 'ro.build.tags=release-keys' pour garantir la certification Play Protect.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))
          ProjectSelectorDropdown(
            projects = projects,
            selectedProject = selectedProject,
            onProjectSelected = onSelectProject
          )

          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = { if (selectedProject != null) onRebuildSystemTrust(selectedProject) },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_rebuild_system_trust"),
            colors = ButtonDefaults.buttonColors(containerColor = AmberCore, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            enabled = selectedProject != null && !isBusy
          ) {
            Icon(Icons.Rounded.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("RECRÉER LE SYSTÈME DE CONFIANCE (A À Z)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // APK LIST & ACTIONS
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Gestionnaire d'APKs (${apks.size} trouvés dans FORGER)",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )

        Button(
          onClick = onBatchSignApks,
          colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("btn_batch_sign"),
          enabled = !isBusy
        ) {
          Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (selectedCount > 0) "Signer ($selectedCount)" else "Signer tous",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }

    // INSPECTION CARD (IF AN APK IS SELECTED)
    if (inspectedApk != null) {
      item {
        val apk = inspectedApk!!
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = CyberCyan
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Vérification de Signature : ${apk.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CyberCyan
              )
              CyberBadge(
                text = if (apk.isSigned) "Signé & Valide" else "Non Signé",
                color = if (apk.isSigned) EmeraldGreen else Color.Red
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("• Schémas actifs : [v1: ${apk.schemeV1}] [v2: ${apk.schemeV2}] [v3: ${apk.schemeV3}] [v4: ${apk.schemeV4}]", fontSize = 12.sp)
            Text("• Émetteur (Issuer) : ${apk.issuer}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("• Empreinte SHA-256 : ${apk.certSha256}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text("• Empreinte SHA-1   : ${apk.certSha1}", fontSize = 11.sp)
            Text("• Empreinte MD5     : ${apk.certMd5}", fontSize = 11.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { onSignSingleApk(apk) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("btn_sign_single"),
                enabled = !isBusy
              ) {
                Text("Signer cet APK", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // LIST OF APKS
    items(apks) { apk ->
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { inspectedApk = apk }
          .testTag("apk_card_${apk.name}")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = apk.isSelected,
            onCheckedChange = { onToggleApkSelection(apk) },
            colors = CheckboxDefaults.colors(checkedColor = CyberCyan)
          )

          Icon(
            imageVector = Icons.Rounded.Android,
            contentDescription = null,
            tint = if (apk.isSystemApk) AmberCore else CyberCyan,
            modifier = Modifier.size(28.dp)
          )

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = apk.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "${apk.sizeBytes / 1024} Ko • ${if (apk.isSystemApk) "Priv-App Système" else "App Utilisateur"}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = { onSignSingleApk(apk) },
            enabled = !isBusy
          ) {
            Icon(Icons.Rounded.Lock, contentDescription = "Signer", tint = CyberCyan)
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
