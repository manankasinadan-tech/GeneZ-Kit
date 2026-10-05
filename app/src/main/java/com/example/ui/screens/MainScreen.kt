package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PhonelinkSetup
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.VoletTab
import com.example.ui.components.CyberBadge
import com.example.viewmodel.GenesisViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: GenesisViewModel) {
  val currentTab by viewModel.currentTab.collectAsState()
  val currentAction by viewModel.currentAction.collectAsState()
  val isRoot by viewModel.isRootEnabled.collectAsState()
  val themeMode by viewModel.themeMode.collectAsState()
  val logs by viewModel.logs.collectAsState()

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier
          .width(310.dp)
          .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
        ) {
          // Drawer Header - Android 16/17 Clean Flat Style
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.Psychology,
                contentDescription = "Logo Genesis Kitchen",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "GENESIS Kitchen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "On-Device ROM Lab",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          HorizontalDivider(
            modifier = Modifier.padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
          )

          Text(
            text = "OUTILS DU LABORATOIRE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
          )

          // Lateral Action Items
          val actions = listOf(
            ActionType.UNPACK_REPACK to Icons.Rounded.Inventory2,
            ActionType.EXPLORER to Icons.Rounded.AccountTree,
            ActionType.GSI_PORTER to Icons.Rounded.PhonelinkSetup,
            ActionType.SIGNER to Icons.Rounded.VerifiedUser,
            ActionType.KEY_MAKER to Icons.Rounded.VpnKey,
            ActionType.FILE_GENERATOR to Icons.Rounded.Code,
            ActionType.MAKE_IT to Icons.Rounded.AutoAwesome
          )

          actions.forEach { (action, icon) ->
            val isSelected = currentAction == action && currentTab == VoletTab.ACTIONS
            val isMakeIt = action == ActionType.MAKE_IT

            NavigationDrawerItem(
              icon = {
                Icon(
                  imageVector = icon,
                  contentDescription = action.label,
                  tint = if (isSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              label = {
                Column {
                  Text(
                    text = action.label,
                    fontWeight = if (isSelected || isMakeIt) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = action.subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              },
              selected = isSelected,
              onClick = {
                viewModel.setAction(action)
                scope.launch { drawerState.close() }
              },
              modifier = Modifier
                .padding(vertical = 3.dp)
                .testTag("drawer_item_${action.name.lowercase()}"),
              colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
              ),
              shape = RoundedCornerShape(14.dp)
            )
          }

          Spacer(modifier = Modifier.weight(1f))

          // Drawer Footer Status
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(MaterialTheme.colorScheme.surfaceContainerHigh)
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Dr Roid v5.0", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                CyberBadge(text = if (isRoot) "Root" else "Standard", color = if (isRoot) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
              }
              Text("ROM Lab 100% On-Device • Tucana ready", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    }
  ) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.background,
      topBar = {
        // Safe status bar top app header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.testTag("btn_open_drawer")
              ) {
                Icon(
                  imageVector = Icons.Rounded.Menu,
                  contentDescription = "Ouvrir la barre latérale",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              Column {
                Text(
                  text = "GENESIS Kitchen",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Ultimate On-Device ROM Lab",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              CyberBadge(
                text = currentAction.label,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      },
      bottomBar = {
        // Bottom Navigation Bar with 3 volets (Actions, Terminal, Settings)
        NavigationBar(
          modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
          tonalElevation = 2.dp
        ) {
          NavigationBarItem(
            selected = currentTab == VoletTab.ACTIONS,
            onClick = { viewModel.setTab(VoletTab.ACTIONS) },
            icon = {
              Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = "Actions"
              )
            },
            label = { Text("Actions", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_volet_actions")
          )

          NavigationBarItem(
            selected = currentTab == VoletTab.TERMINAL,
            onClick = { viewModel.setTab(VoletTab.TERMINAL) },
            icon = {
              Icon(
                imageVector = Icons.Rounded.Terminal,
                contentDescription = "Terminal"
              )
            },
            label = { Text("Terminal", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_volet_terminal")
          )

          NavigationBarItem(
            selected = currentTab == VoletTab.SETTINGS,
            onClick = { viewModel.setTab(VoletTab.SETTINGS) },
            icon = {
              Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Paramètres"
              )
            },
            label = { Text("Paramètres", fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("tab_volet_settings")
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "VoletTabTransition"
        ) { targetTab ->
          when (targetTab) {
            VoletTab.ACTIONS -> {
              ActionHostScreen(
                viewModel = viewModel,
                currentAction = currentAction
              )
            }

            VoletTab.TERMINAL -> {
              TerminalScreen(
                logs = logs,
                isRoot = isRoot,
                onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                onClearLogs = { viewModel.clearLogs() }
              )
            }

            VoletTab.SETTINGS -> {
              SettingsScreen(
                themeMode = themeMode,
                isRoot = isRoot,
                baseForgerPath = viewModel.workspace.baseForgerDir.absolutePath,
                onThemeChanged = { viewModel.setThemeMode(it) },
                onRootChanged = { viewModel.setRootEnabled(it) }
              )
            }
          }
        }
      }
    }
  }
}
