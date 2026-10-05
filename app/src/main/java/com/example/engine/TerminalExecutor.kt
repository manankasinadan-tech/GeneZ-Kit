package com.example.engine

import com.example.model.LogLevel
import com.example.model.RomFormat
import com.example.model.TerminalEntry
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TerminalExecutor(
  private val workspace: RomWorkspaceManager,
  private val drRoid: DrRoidEngine,
  private val gsiPorter: GsiPorterEngine,
  private val signer: SignerEngine,
  private val keyMaker: KeyMakerEngine,
  private val fileGen: FileGeneratorEngine
) {

  suspend fun executeCommand(
    input: String,
    isRoot: Boolean,
    onLog: (TerminalEntry) -> Unit
  ) = withContext(Dispatchers.IO) {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return@withContext

    onLog(TerminalEntry(level = LogLevel.COMMAND, message = "$ $trimmed", tag = "TERMINAL"))

    val tokens = trimmed.split("\\s+".toRegex())
    val cmd = tokens[0].lowercase()

    when (cmd) {
      "help", "aide" -> {
        onLog(TerminalEntry(level = LogLevel.INFO, message = "=== GENESIS KITCHEN TERMINAL COMMANDES ===", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• help                    : Affiche ce guide complet", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• forger                  : Affiche l'arborescence et l'état du dossier FORGER", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• unpack <projet>         : Simule l'unpacking (UKA engine)", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• repack <projet> [format]: Repack un dossier (Tool-Tree engine)", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• dr-roid analyze <projet>: Analyse chirurgicale et rapport JSON AST", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• dr-roid make-it <projet>: Rebuild complet de la ROM scellée", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• gsi port <projet>       : Portage GSI spécifique Xiaomi Tucana", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• gsi fix-fod <projet>    : Application du fix FOD (Goodix GF5288)", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• sign --all              : Signe tous les APKs avec releasekey", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• keys make-aosp          : Génère les 6 clés officielles AOSP", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• deodex <projet>         : Dé-odexing et conversion classes.dex", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• gen-fsconfig <projet>   : Génère la table POSIX fs_config", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• clear                   : Efface l'écran du terminal", tag = "HELP"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "• Commandes Shell : ls, pwd, uname, df, date...", tag = "HELP"))
      }

      "forger" -> {
        onLog(TerminalEntry(level = LogLevel.INFO, message = "FORGER Base: ${workspace.baseForgerDir.absolutePath}", tag = "FORGER"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "├── UNPACKED (${workspace.unpackedDir.listFiles()?.size ?: 0} dossiers)", tag = "FORGER"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "├── PACKED   (${workspace.packedDir.listFiles()?.size ?: 0} images)", tag = "FORGER"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "├── KEY      (${workspace.keyDir.listFiles()?.size ?: 0} fichiers de clés)", tag = "FORGER"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "├── SIGNED   (${workspace.signedDir.listFiles()?.size ?: 0} APKs)", tag = "FORGER"))
        onLog(TerminalEntry(level = LogLevel.INFO, message = "└── REPORTS  (${workspace.reportsDir.listFiles()?.size ?: 0} rapports JSON)", tag = "FORGER"))
      }

      "dr-roid" -> {
        val sub = tokens.getOrNull(1)
        val projName = tokens.getOrNull(2)
        val projects = workspace.listUnpackedProjects()
        val proj = if (projName != null) projects.find { it.name.contains(projName, ignoreCase = true) } else projects.firstOrNull()

        if (proj == null) {
          onLog(TerminalEntry(level = LogLevel.ERROR, message = "Projet non trouvé. Projets dispo: ${projects.joinToString { it.name }}", tag = "DR_ROID"))
          return@withContext
        }

        if (sub == "make-it" || sub == "makeit") {
          drRoid.executeMakeIt(proj, RomFormat.EROFS, onLog)
        } else {
          drRoid.inspectAndSynthesize(proj, onLog)
        }
      }

      "gsi" -> {
        val sub = tokens.getOrNull(1)
        val projects = workspace.listUnpackedProjects()
        val proj = projects.firstOrNull()
        if (proj == null) {
          onLog(TerminalEntry(level = LogLevel.ERROR, message = "Aucun projet GSI disponible dans FORGER/UNPACKED", tag = "GSI"))
          return@withContext
        }
        if (sub == "fix-fod" || sub == "fod") {
          gsiPorter.runFodFixer(proj, onLog)
        } else {
          gsiPorter.makeGsiSpecific(proj, onLog)
        }
      }

      "keys" -> {
        keyMaker.generateAospKeySuite(onLog = onLog)
      }

      "clear" -> {
        // Triggered via ViewModel callback or handler
        onLog(TerminalEntry(level = LogLevel.INFO, message = "--- Terminal effacé ---", tag = "TERMINAL"))
      }

      else -> {
        // Run as real shell command (using ProcessBuilder)
        runSystemShellCommand(trimmed, isRoot, onLog)
      }
    }
  }

  private fun runSystemShellCommand(
    command: String,
    isRoot: Boolean,
    onLog: (TerminalEntry) -> Unit
  ) {
    try {
      val shell = if (isRoot) "su" else "sh"
      val process = ProcessBuilder(shell, "-c", command)
        .directory(workspace.baseForgerDir)
        .redirectErrorStream(true)
        .start()

      val reader = BufferedReader(InputStreamReader(process.inputStream))
      var line: String?
      var hasOutput = false
      while (reader.readLine().also { line = it } != null) {
        hasOutput = true
        onLog(TerminalEntry(level = LogLevel.INFO, message = line ?: "", tag = "SHELL"))
      }
      val exitCode = process.waitFor()
      if (!hasOutput && exitCode == 0) {
        onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "[Exécution terminée sans sortie - code 0]", tag = "SHELL"))
      } else if (exitCode != 0) {
        onLog(TerminalEntry(level = LogLevel.WARNING, message = "[Processus terminé avec code $exitCode]", tag = "SHELL"))
      }
    } catch (e: Exception) {
      onLog(TerminalEntry(level = LogLevel.ERROR, message = "Erreur exécution: ${e.message}", tag = "SHELL"))
    }
  }
}
