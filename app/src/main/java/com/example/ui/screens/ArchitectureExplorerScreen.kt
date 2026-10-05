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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArchitectureCategory
import com.example.model.ArchitectureNode
import com.example.model.UnpackedProject
import com.example.ui.components.CyberBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.ProjectSelectorDropdown
import com.example.ui.components.SectionHeader

@Composable
fun ArchitectureExplorerScreen(
  projects: List<UnpackedProject>,
  selectedProject: UnpackedProject?,
  architectureNodes: List<ArchitectureNode>,
  selectedNode: ArchitectureNode?,
  onSelectProject: (UnpackedProject) -> Unit,
  onSelectNode: (ArchitectureNode) -> Unit
) {
  var selectedCategory by remember { mutableStateOf(ArchitectureCategory.ALL) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredNodes = architectureNodes.filter { node ->
    val matchesCategory = selectedCategory == ArchitectureCategory.ALL || node.category == selectedCategory
    val matchesQuery = searchQuery.isBlank() ||
        node.name.contains(searchQuery, ignoreCase = true) ||
        node.path.contains(searchQuery, ignoreCase = true) ||
        node.roleDescription.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesQuery
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      SectionHeader(
        title = "Architecture Explorer",
        subtitle = "Cartographie sémantique & Réseau d'interdépendances",
        icon = Icons.Rounded.AccountTree
      )
    }

    item {
      ProjectSelectorDropdown(
        projects = projects,
        selectedProject = selectedProject,
        onProjectSelected = onSelectProject
      )
    }

    // Category Filter Pills
    item {
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(ArchitectureCategory.values()) { cat ->
          val isSel = selectedCategory == cat
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isSel) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .clickable { selectedCategory = cat }
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(
              text = cat.label,
              fontSize = 12.sp,
              fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
              color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Count and Status Summary
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${filteredNodes.size} composants architecturaux",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )

        CyberBadge(
          text = "Interdépendances actives",
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    // Node Cards with Cross-References and Interdependencies
    items(filteredNodes) { node ->
      val isSelected = selectedNode?.path == node.path

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onSelectNode(node) }
          .testTag("arch_node_${node.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
          else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
          width = if (isSelected) 2.dp else 1.dp,
          brush = androidx.compose.ui.graphics.SolidColor(
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Header with icon and category
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
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
                  imageVector = when (node.category) {
                    ArchitectureCategory.FRAMEWORK -> Icons.Rounded.Layers
                    ArchitectureCategory.HALS_SERVICES -> Icons.Rounded.Hardware
                    ArchitectureCategory.APEX -> Icons.Rounded.Extension
                    ArchitectureCategory.INIT_CONFIG -> Icons.Rounded.Terminal
                    ArchitectureCategory.SELINUX -> Icons.Rounded.Security
                    ArchitectureCategory.BINARIES -> Icons.Rounded.Code
                    ArchitectureCategory.ALL -> Icons.Rounded.AccountTree
                  },
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = node.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = node.path,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            CyberBadge(
              text = node.category.label,
              color = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Architectural Role
          Text(
            text = node.roleDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Security & POSIX Metadata Pill Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Context: ${node.selinuxContext}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${node.permissions} • ${node.uidGid}",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Interdependencies Section (Renvois entre composants)
          if (node.dependencies.isNotEmpty() || node.dependedOnBy.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Dependencies
              if (node.dependencies.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(
                    Icons.Rounded.ArrowDownward,
                    contentDescription = "Dépend de",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = "DÉPEND DIRECTEMENT DE :",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    node.dependencies.forEach { dep ->
                      Text(
                        text = "• $dep",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }

              // Depended on by
              if (node.dependedOnBy.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = "Requis par",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = "REQUIS PAR (CONSOMMATEURS) :",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.secondary
                    )
                    node.dependedOnBy.forEach { client ->
                      Text(
                        text = "• $client",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }
              }

              // Init Service Binding
              if (node.initServiceBinding != null) {
                Row(verticalAlignment = Alignment.Top) {
                  Icon(
                    Icons.Rounded.Link,
                    contentDescription = "Liaison Init",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = "LIAISON INIT RC & DÉMARRAGE :",
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                      text = node.initServiceBinding,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(24.dp)) }
  }
}
