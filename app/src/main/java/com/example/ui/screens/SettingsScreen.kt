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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberCore
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen

@Composable
fun SettingsScreen(
  themeMode: AppThemeMode,
  isRoot: Boolean,
  baseForgerPath: String,
  onThemeChanged: (AppThemeMode) -> Unit,
  onRootChanged: (Boolean) -> Unit
) {
  var showReadme by remember { mutableStateOf(false) }
  var showChangelog by remember { mutableStateOf(false) }
  var showTerminalHelp by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      SectionHeader(
        title = "Paramètres & Documentation",
        subtitle = "Configuration de GENESIS Kitchen • Guide & Manuels",
        icon = Icons.Rounded.Settings,
        accentColor = CyberCyan
      )
    }

    // THEME SELECTOR
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Palette, contentDescription = null, tint = CyberCyan)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Thème de l'application",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val modes = listOf(
              Triple(AppThemeMode.SYSTEM, "Système", Icons.Rounded.PhoneAndroid),
              Triple(AppThemeMode.LIGHT, "Clair", Icons.Rounded.LightMode),
              Triple(AppThemeMode.DARK, "Sombre", Icons.Rounded.DarkMode)
            )

            modes.forEach { (mode, label, icon) ->
              val isSel = themeMode == mode
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSel) CyberCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                  .border(1.dp, if (isSel) CyberCyan else Color.Transparent, RoundedCornerShape(12.dp))
                  .clickable { onThemeChanged(mode) }
                  .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSel) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSel) CyberCyan else MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
      }
    }

    // ROOT MODE TOGGLE
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Security, contentDescription = null, tint = if (isRoot) EmeraldGreen else AmberCore)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Mode Root (Magisk / KernelSU / APatch)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isRoot) "Accès root /vendor actif pour extraction live" else "Mode sans root : profils et arbres matériels intégrés",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Switch(
            checked = isRoot,
            onCheckedChange = onRootChanged,
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen),
            modifier = Modifier.testTag("switch_root_mode")
          )
        }
      }
    }

    // STORAGE PATH INFO
    item {
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Folder, contentDescription = null, tint = AmberCore)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Répertoire de Travail : FORGER",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = baseForgerPath,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(10.dp))
          Text("• FORGER/UNPACKED : Dossiers extraits (UKA)", fontSize = 11.5.sp)
          Text("• FORGER/PACKED   : Images compilées prêtes au flash (Tool-Tree)", fontSize = 11.5.sp)
          Text("• FORGER/KEY      : Paires de clés AOSP .pk8 / .pem / .jks", fontSize = 11.5.sp)
          Text("• FORGER/SIGNED   : APKs signés certifiés release-keys", fontSize = 11.5.sp)
          Text("• FORGER/REPORTS  : Rapports d'analyse déclarative Dr Roid", fontSize = 11.5.sp)
        }
      }
    }

    // TERMINAL COMMANDS GUIDE (EXPANDABLE)
    item {
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showTerminalHelp = !showTerminalHelp }
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.Terminal, contentDescription = null, tint = CyberCyan)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Manuel des Commandes du Terminal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            CyberBadge(text = if (showTerminalHelp) "Masquer" else "Afficher", color = CyberCyan)
          }

          AnimatedVisibility(visible = showTerminalHelp) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("• help : Affiche la liste des commandes et leur syntaxe", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• forger : Détaille le contenu et l'arborescence des dossiers FORGER", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• unpack <img_path> -p <partition> : Décompresse une image", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• repack <folder> [erofs|ext4|sparse] : Recompile un dossier", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• dr-roid analyze <folder> : Génère le rapport AST et vérifie SELinux", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• dr-roid make-it <folder> : Rebuild complet avec scellement", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• gsi fix-fod <folder> : Injecte le patch du capteur d'empreinte Tucana", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• keys make-aosp : Crée la suite des 6 clés officielles AOSP", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• sign --all : Signe tous les APKs trouvés dans FORGER", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
              Text("• clear : Réinitialise l'écran du terminal", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
          }
        }
      }
    }

    // README (EXPANDABLE)
    item {
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showReadme = !showReadme }
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.Book, contentDescription = null, tint = AmberCore)
              Spacer(modifier = Modifier.width(8.dp))
              Text("GENESIS Kitchen : Documentation & Philosophie", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            CyberBadge(text = if (showReadme) "Masquer" else "Lire", color = AmberCore)
          }

          AnimatedVisibility(visible = showReadme) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "GENESIS Kitchen est un laboratoire de ROM On-Device de nouvelle génération inspiré par UKA et Tool-Tree, conçu pour fonctionner aussi bien sur des terminaux rootés que sans root.",
                fontSize = 12.sp
              )
              Text(
                text = "Grâce à son moteur Dr Roid, l'application analyse les arborescences de micrologiciels en AST (Abstract Syntax Tree), valide la conformité des contextes SELinux et résout les problèmes de compatibilité matérielle (comme le FOD sur Xiaomi Tucana) avant de reconstruire des images scellées et signées.",
                fontSize = 12.sp
              )
              Text(
                text = "L'interface Pixel Studio évite les listes interminables au profit de surfaces en verre liquide translucides, rapides et ergonomiques.",
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // CHANGELOG
    item {
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showChangelog = !showChangelog }
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.History, contentDescription = null, tint = EmeraldGreen)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Changelog (Journal des versions)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            CyberBadge(text = "v1.0 Release", color = EmeraldGreen)
          }

          AnimatedVisibility(visible = showChangelog) {
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Version 1.0 (Initial Public Release) :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("• Moteur Unpack/Repack haute fidélité (UKA & Tool-Tree)", fontSize = 11.5.sp)
              Text("• GSI PORTER avec support dédié Xiaomi Tucana (Mi Note 10) & FOD Fixer", fontSize = 11.5.sp)
              Text("• SIGNER complet : v1/v2/v3/v4 & Recréation de confiance A à Z", fontSize = 11.5.sp)
              Text("• KEY MAKER : Suite AOSP officielle (platform, media, releasekey...)", fontSize = 11.5.sp)
              Text("• FILE GENERATOR : Nettoyage/Génération Vdex, Odex, Oat, fs_config, fsvmeta", fontSize = 11.5.sp)
              Text("• Dr Roid : Super cerveau d'orchestration & fonctionnalité MAKE IT !", fontSize = 11.5.sp)
              Text("• Volet Terminal interactif avec support commandes et sortie live", fontSize = 11.5.sp)
              Text("• Thème Pixel Studio avec liquid glass, clair/sombre/système", fontSize = 11.5.sp)
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
