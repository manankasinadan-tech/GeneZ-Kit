package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.KeyProfile
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberCore
import com.example.ui.theme.CyberCyan

@Composable
fun KeyMakerScreen(
  keys: List<KeyProfile>,
  isBusy: Boolean,
  busyMessage: String,
  onGenerateAospKeys: () -> Unit,
  onGenerateCustomKey: (alias: String, cn: String, org: String, years: Int, algo: String) -> Unit
) {
  var alias by remember { mutableStateOf("rom_release") }
  var cn by remember { mutableStateOf("Genesis Android Team") }
  var org by remember { mutableStateOf("AOSP On-Device Lab") }
  var validityYears by remember { mutableIntStateOf(25) }
  var selectedAlgo by remember { mutableStateOf("RSA 4096") }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "KEY MAKER Studio",
        subtitle = "Génération de clés cryptographiques AOSP & PKCS#8",
        icon = Icons.Rounded.VpnKey,
        accentColor = CyberCyan
      )
    }

    // AOSP SUITE QUICK BUILDER
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
            Text(
              text = "Suite Complète de Clés AOSP",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = CyberCyan
            )
            CyberBadge(text = "6 Clés Essentielles", color = CyberCyan)
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Génère automatiquement les 6 paires de clés officielles AOSP pour construire une ROM personnalisée complète : platform, shared, media, releasekey, networkstack et bluetooth (.pk8 + .x509.pem).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = onGenerateAospKeys,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_gen_aosp_suite"),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D)),
            shape = RoundedCornerShape(12.dp),
            enabled = !isBusy
          ) {
            Icon(Icons.Rounded.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GÉNÉRER LA SUITE AOSP (6 PAIRES PK8/PEM)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // CUSTOM KEY CREATOR
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Création de Clé Personnalisée",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            label = { Text("Alias / Nom de la clé") },
            modifier = Modifier.fillMaxWidth().testTag("input_key_alias"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = cn,
            onValueChange = { cn = it },
            label = { Text("Common Name (CN)") },
            modifier = Modifier.fillMaxWidth().testTag("input_key_cn"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = org,
            onValueChange = { org = it },
            label = { Text("Organisation (O)") },
            modifier = Modifier.fillMaxWidth().testTag("input_key_org"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyberCyan,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text("Algorithme cryptographique :", style = MaterialTheme.typography.labelSmall)
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("RSA 4096", "RSA 2048", "EC P-256").forEach { algo ->
              val isSel = selectedAlgo == algo
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) AmberCore.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                  .border(1.dp, if (isSel) AmberCore else Color.Transparent, RoundedCornerShape(8.dp))
                  .clickable { selectedAlgo = algo }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = algo,
                  fontSize = 11.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) AmberCore else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { onGenerateCustomKey(alias, cn, org, validityYears, selectedAlgo) },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_create_custom_key"),
            colors = ButtonDefaults.buttonColors(containerColor = AmberCore, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            enabled = alias.isNotBlank() && !isBusy
          ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GÉNÉRER ET STOCKER DANS FORGER/KEY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }

    // LIST OF EXISTING KEYS
    item {
      Text(
        text = "Clés disponibles dans FORGER/KEY (${keys.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    items(keys) { key ->
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(CyberCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Rounded.Key, contentDescription = null, tint = CyberCyan)
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = key.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(8.dp))
              CyberBadge(text = key.algorithm, color = AmberCore)
            }
            Text(
              text = "Validité : ${key.validityYears} ans • ${key.organization}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Fichiers : ${key.generatedFiles.joinToString()}",
              fontSize = 10.sp,
              color = CyberCyan
            )
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
