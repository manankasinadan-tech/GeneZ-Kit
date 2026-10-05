package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogLevel
import com.example.model.TerminalEntry
import com.example.ui.components.CyberBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberCore
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalPrompt
import com.example.ui.theme.TerminalText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalScreen(
  logs: List<TerminalEntry>,
  isRoot: Boolean,
  onExecuteCommand: (String) -> Unit,
  onClearLogs: () -> Unit
) {
  var commandText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  val context = LocalContext.current
  val clipboard = LocalClipboardManager.current
  val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

  // Auto-scroll on new log entries
  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      listState.animateScrollToItem(logs.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Top Bar of Terminal
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Terminal, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Terminal & Historique",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        CyberBadge(
          text = if (isRoot) "Root (su)" else "User (sh)",
          color = if (isRoot) EmeraldGreen else CyberCyan
        )
        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = {
            val allText = logs.joinToString("\n") { "[${it.tag}] ${it.message}" }
            clipboard.setText(AnnotatedString(allText))
            Toast.makeText(context, "Logs copiés dans le presse-papiers", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.size(36.dp).testTag("btn_copy_logs")
        ) {
          Icon(Icons.Rounded.ContentCopy, contentDescription = "Copier logs", modifier = Modifier.size(18.dp))
        }

        IconButton(
          onClick = onClearLogs,
          modifier = Modifier.size(36.dp).testTag("btn_clear_logs")
        ) {
          Icon(Icons.Rounded.CleaningServices, contentDescription = "Effacer logs", modifier = Modifier.size(18.dp))
        }
      }
    }

    // Quick Command Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      val quickCommands = listOf(
        "help",
        "forger",
        "dr-roid make-it",
        "gsi fix-fod",
        "keys make-aosp",
        "clear",
        "ls -la",
        "df -h",
        "uname -a"
      )
      items(quickCommands) { cmd ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable {
              commandText = cmd
              onExecuteCommand(cmd)
              commandText = ""
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(text = cmd, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberCyan)
        }
      }
    }

    // Main Terminal Console Viewport
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(TerminalBg)
        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        .padding(12.dp)
    ) {
      if (logs.isEmpty()) {
        Text(
          text = "Aucun log pour le moment.\nTapez 'help' ou lancez une action pour voir la sortie.",
          color = Color.Gray,
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          modifier = Modifier.align(Alignment.Center)
        )
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(logs) { entry ->
            val color = when (entry.level) {
              LogLevel.INFO -> TerminalText
              LogLevel.SUCCESS -> EmeraldGreen
              LogLevel.WARNING -> AmberCore
              LogLevel.ERROR -> CrimsonAlert
              LogLevel.COMMAND -> TerminalPrompt
              LogLevel.DR_ROID -> CyberCyan
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                text = "${timeFormat.format(Date(entry.timestamp))} [${entry.tag}] ",
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
              Text(
                text = entry.message,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                fontWeight = if (entry.level == LogLevel.COMMAND || entry.level == LogLevel.DR_ROID) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Interactive Command Input
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = commandText,
        onValueChange = { commandText = it },
        placeholder = { Text("Taper commande (ex: help, forger, dr-roid)...", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
        modifier = Modifier
          .weight(1f)
          .testTag("input_terminal_cmd"),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (commandText.isNotBlank()) {
              onExecuteCommand(commandText)
              commandText = ""
            }
          }
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CyberCyan,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (commandText.isNotBlank()) {
            onExecuteCommand(commandText)
            commandText = ""
          }
        },
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CyberCyan)
          .testTag("btn_send_cmd")
      ) {
        Icon(Icons.Rounded.Send, contentDescription = "Exécuter", tint = Color(0xFF00363D))
      }
    }
  }
}
