package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UnpackedProject

// Android 16 / 17 Material You Flat Surface Card (Zero Glassmorphism)
@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  shape: RoundedCornerShape = RoundedCornerShape(18.dp),
  content: @Composable () -> Unit
) {
  Card(
    modifier = modifier,
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = BorderStroke(1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    content()
  }
}

// Android 16 / 17 Tonal Status Pill (No awkward multiline wrap)
@Composable
fun CyberBadge(
  text: String,
  color: Color = MaterialTheme.colorScheme.primary,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(CircleShape)
      .background(color.copy(alpha = 0.14f))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = text,
      color = color,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

// Android 16 / 17 Clean Section Header
@Composable
fun SectionHeader(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color = MaterialTheme.colorScheme.primary
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun ProjectSelectorDropdown(
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  onProjectSelected: (UnpackedProject) -> Unit,
  onDeleteProject: ((UnpackedProject) -> Unit)? = null,
  onCleanTempProjects: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  GlassCard(
    modifier = modifier
      .fillMaxWidth()
      .clickable { expanded = true }
      .testTag("project_selector_card"),
    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.Folder,
          contentDescription = "Dossier source",
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Projet actif (FORGER/UNPACKED)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = selectedProject?.name ?: "Aucun projet décompressé",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      Icon(
        imageVector = Icons.Rounded.KeyboardArrowDown,
        contentDescription = "Ouvrir",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )

      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
      ) {
        if (projects.isEmpty()) {
          DropdownMenuItem(
            text = { Text("Aucun projet trouvé dans FORGER/UNPACKED") },
            onClick = { expanded = false }
          )
        } else {
          projects.forEach { proj ->
            val isSelected = proj.id == selectedProject?.id
            DropdownMenuItem(
              text = {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = proj.name,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "${proj.partitionName} • ${proj.originalFormat.displayName} • ${proj.fileCount} fichiers",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  if (onDeleteProject != null) {
                    IconButton(
                      onClick = {
                        onDeleteProject(proj)
                        expanded = false
                      },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Rounded.Folder,
                  contentDescription = null,
                  tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              onClick = {
                onProjectSelected(proj)
                expanded = false
              }
            )
          }

          if (onCleanTempProjects != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
              text = {
                Text(
                  "Nettoyer les projets temporaires",
                  color = MaterialTheme.colorScheme.error,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              },
              leadingIcon = {
                Icon(
                  Icons.Rounded.CleaningServices,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(18.dp)
                )
              },
              onClick = {
                onCleanTempProjects()
                expanded = false
              }
            )
          }
        }
      }
    }
  }
}
