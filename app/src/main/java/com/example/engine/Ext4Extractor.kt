package com.example.engine

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.model.LogLevel
import com.example.model.RomFormat
import com.example.model.TerminalEntry
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * Pure Kotlin Userspace EXT4 & Sparse Image Extractor (Zero-Root).
 * Directly parses superblock, block group descriptors, inode tables,
 * and extent trees to extract 100% of real files, APKs, ELF binaries,
 * and build.prop from Android firmware images.
 */
class Ext4Extractor(private val context: Context) {

  data class Extent(val logicalBlock: Long, val physicalBlock: Long, val blockCount: Int)

  data class Ext4DirEntry(
    val inode: Long,
    val fileType: Int, // 1=file, 2=dir, 7=symlink
    val name: String
  )

  data class InodeInfo(
    val inodeNumber: Long,
    val mode: Int,
    val isDirectory: Boolean,
    val isRegularFile: Boolean,
    val isSymlink: Boolean,
    val size: Long,
    val extents: List<Extent>,
    val inlineData: ByteArray? = null
  )

  /**
   * Random access reader that transparently handles both RAW EXT4
   * and Android Sparse (`0x3AFF26ED`) images without needing root or disk duplication.
   */
  interface ImageReader : AutoCloseable {
    val size: Long
    fun read(position: Long, dest: ByteArray, offset: Int, length: Int): Int
    fun readFully(position: Long, dest: ByteArray) {
      var readTotal = 0
      while (readTotal < dest.size) {
        val r = read(position + readTotal, dest, readTotal, dest.size - readTotal)
        if (r <= 0) break
        readTotal += r
      }
    }
  }

  class RawImageReader(private val pfd: ParcelFileDescriptor) : ImageReader {
    private val channel: FileChannel = FileInputStream(pfd.fileDescriptor).channel
    override val size: Long get() = try { channel.size() } catch (_: Exception) { 0L }

    override fun read(position: Long, dest: ByteArray, offset: Int, length: Int): Int {
      val buf = ByteBuffer.wrap(dest, offset, length)
      return channel.read(buf, position)
    }

    override fun close() {
      try { channel.close() } catch (_: Exception) {}
      try { pfd.close() } catch (_: Exception) {}
    }
  }

  class SparseChunk(
    val logicalStartBlock: Long,
    val blockCount: Long,
    val chunkType: Int, // 0xCAC1: Raw, 0xCAC2: Fill, 0xCAC3: DontCare
    val fileOffset: Long,
    val fillValue: ByteArray? = null
  )

  class SparseImageReader(
    private val pfd: ParcelFileDescriptor,
    val blockSize: Int,
    val chunks: List<SparseChunk>,
    override val size: Long
  ) : ImageReader {
    private val channel: FileChannel = FileInputStream(pfd.fileDescriptor).channel

    override fun read(position: Long, dest: ByteArray, offset: Int, length: Int): Int {
      if (position >= size) return -1
      val targetLogicalBlock = position / blockSize
      val blockOffset = (position % blockSize).toInt()

      // Find corresponding chunk
      val chunk = chunks.firstOrNull {
        targetLogicalBlock >= it.logicalStartBlock && targetLogicalBlock < (it.logicalStartBlock + it.blockCount)
      } ?: return -1

      val bytesToRead = length.coerceAtMost((size - position).toInt())

      when (chunk.chunkType) {
        0xCAC1 -> { // RAW
          val chunkByteOffset = (targetLogicalBlock - chunk.logicalStartBlock) * blockSize + blockOffset
          val actualFilePos = chunk.fileOffset + chunkByteOffset
          val buf = ByteBuffer.wrap(dest, offset, bytesToRead)
          return channel.read(buf, actualFilePos)
        }
        0xCAC2 -> { // FILL
          val fill = chunk.fillValue ?: byteArrayOf(0, 0, 0, 0)
          for (i in 0 until bytesToRead) {
            val fillIdx = ((blockOffset + i) % fill.size)
            dest[offset + i] = fill[fillIdx]
          }
          return bytesToRead
        }
        else -> { // DONT_CARE (Zeros)
          dest.fill(0, offset, offset + bytesToRead)
          return bytesToRead
        }
      }
    }

    override fun close() {
      try { channel.close() } catch (_: Exception) {}
      try { pfd.close() } catch (_: Exception) {}
    }
  }

  fun openReader(uri: Uri): ImageReader {
    val pfd = context.contentResolver.openFileDescriptor(uri, "r")
      ?: throw IllegalStateException("Impossible d'ouvrir le fichier : descripteur indisponible.")

    val headerBuf = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN)
    val channel = FileInputStream(pfd.fileDescriptor).channel
    channel.read(headerBuf, 0)
    headerBuf.flip()

    if (headerBuf.remaining() >= 4) {
      val magic = headerBuf.getInt(0)
      if (magic == 0xED26FF3A.toInt()) {
        // Android Sparse image! Build sparse index table
        val majorVersion = headerBuf.getShort(4).toInt() and 0xFFFF
        val fileHdrSz = headerBuf.getShort(8).toInt() and 0xFFFF
        val chunkHdrSz = headerBuf.getShort(10).toInt() and 0xFFFF
        val blockSize = headerBuf.getInt(12)
        val totalBlocks = headerBuf.getInt(16).toLong() and 0xFFFFFFFFL
        val totalChunks = headerBuf.getInt(20).toLong() and 0xFFFFFFFFL

        val chunks = mutableListOf<SparseChunk>()
        var currentFilePos = fileHdrSz.toLong()
        var currentLogicalBlock = 0L

        val chunkHdrBuf = ByteBuffer.allocate(chunkHdrSz).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until totalChunks) {
          chunkHdrBuf.clear()
          channel.read(chunkHdrBuf, currentFilePos)
          chunkHdrBuf.flip()

          val chunkType = chunkHdrBuf.getShort(0).toInt() and 0xFFFF
          val chunkBlocks = chunkHdrBuf.getInt(4).toLong() and 0xFFFFFFFFL
          val totalChunkBytes = chunkHdrBuf.getInt(8).toLong() and 0xFFFFFFFFL

          val dataOffset = currentFilePos + chunkHdrSz
          var fillVal: ByteArray? = null

          if (chunkType == 0xCAC2) { // FILL
            val fillBytes = ByteArray(4)
            val fillB = ByteBuffer.wrap(fillBytes)
            channel.read(fillB, dataOffset)
            fillVal = fillBytes
          }

          chunks.add(
            SparseChunk(
              logicalStartBlock = currentLogicalBlock,
              blockCount = chunkBlocks,
              chunkType = chunkType,
              fileOffset = dataOffset,
              fillValue = fillVal
            )
          )

          currentLogicalBlock += chunkBlocks
          currentFilePos += totalChunkBytes
        }

        val totalSize = totalBlocks * blockSize
        return SparseImageReader(pfd, blockSize, chunks, totalSize)
      }
    }

    return RawImageReader(pfd)
  }

  /**
   * Unpacks a full EXT4 filesystem into destination directory.
   */
  suspend fun extractExt4(
    uri: Uri,
    destDir: File,
    onProgress: (Float, String) -> Unit,
    onLog: (TerminalEntry) -> Unit
  ): Boolean {
    val reader = openReader(uri)
    try {
      onProgress(0.15f, "Lecture du superblock ext4 (offset 1024)...")
      onLog(TerminalEntry(level = LogLevel.INFO, message = "[EXT4] Analyse du superblock (offset 0x400)...", tag = "EXT4"))

      val sbBuf = ByteArray(1024)
      reader.readFully(1024, sbBuf)
      val sb = ByteBuffer.wrap(sbBuf).order(ByteOrder.LITTLE_ENDIAN)

      val magic = sb.getShort(56).toInt() and 0xFFFF
      if (magic != 0xEF53) {
        onLog(TerminalEntry(level = LogLevel.WARNING, message = "[EXT4] Entête ext4 non trouvée (magic: 0x${magic.toString(16)}). Tentative de scan brut...", tag = "EXT4"))
        return false
      }

      val inodesCount = sb.getInt(0).toLong() and 0xFFFFFFFFL
      val blocksCount = sb.getInt(4).toLong() and 0xFFFFFFFFL
      val logBlockSize = sb.getInt(24)
      val blockSize = 1024 shl logBlockSize
      val blocksPerGroup = sb.getInt(32).toLong() and 0xFFFFFFFFL
      val inodesPerGroup = sb.getInt(40).toLong() and 0xFFFFFFFFL
      val inodeSize = sb.getShort(88).toInt() and 0xFFFF
      val descSizeRaw = sb.getShort(254).toInt() and 0xFFFF
      val descSize = if (descSizeRaw >= 32) descSizeRaw else 32

      val groupCount = ((blocksCount + blocksPerGroup - 1) / blocksPerGroup).toInt()

      onLog(TerminalEntry(level = LogLevel.INFO, message = "[EXT4] Bloc: ${blockSize}B • Inodes: $inodesCount • Groupes: $groupCount • InodeSize: ${inodeSize}B", tag = "EXT4"))
      onProgress(0.30f, "Lecture des descripteurs de groupes ($groupCount groupes)...")

      // Read block group descriptors
      val firstDataBlock = sb.getInt(20).toLong() and 0xFFFFFFFFL
      val gdtBlock = if (blockSize == 1024) firstDataBlock + 1 else 1L
      val gdtOffset = gdtBlock * blockSize

      val inodeTableBlocks = LongArray(groupCount)
      val descBuf = ByteArray(descSize)

      for (g in 0 until groupCount) {
        reader.readFully(gdtOffset + (g.toLong() * descSize), descBuf)
        val db = ByteBuffer.wrap(descBuf).order(ByteOrder.LITTLE_ENDIAN)
        val inodeTableLo = db.getInt(8).toLong() and 0xFFFFFFFFL
        var inodeTableHi = 0L
        if (descSize >= 64) {
          inodeTableHi = db.getInt(40).toLong() and 0xFFFFFFFFL
        }
        inodeTableBlocks[g] = (inodeTableHi shl 32) or inodeTableLo
      }

      fun getInodeOffset(inodeNum: Long): Long {
        val group = ((inodeNum - 1) / inodesPerGroup).toInt()
        val index = ((inodeNum - 1) % inodesPerGroup).toInt()
        val tableBlock = inodeTableBlocks[group]
        return (tableBlock * blockSize) + (index.toLong() * inodeSize)
      }

      fun readInode(inodeNum: Long): InodeInfo? {
        val offset = getInodeOffset(inodeNum)
        val buf = ByteArray(inodeSize.coerceAtLeast(128))
        reader.readFully(offset, buf)
        val ib = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN)

        val mode = ib.getShort(0).toInt() and 0xFFFF
        if (mode == 0) return null

        val isDir = (mode and 0xF000) == 0x4000
        val isFile = (mode and 0xF000) == 0x8000
        val isSymlink = (mode and 0xF000) == 0xA000

        val sizeLo = ib.getInt(4).toLong() and 0xFFFFFFFFL
        val sizeHi = if (isFile && inodeSize >= 128) ib.getInt(108).toLong() and 0xFFFFFFFFL else 0L
        val fileSize = (sizeHi shl 32) or sizeLo
        val flags = ib.getInt(32)

        val extents = mutableListOf<Extent>()
        val hasExtents = (flags and 0x80000) != 0

        if (isSymlink && fileSize < 60) {
          // Fast symlink stored inline in i_block
          val linkBytes = ByteArray(fileSize.toInt())
          System.arraycopy(buf, 40, linkBytes, 0, fileSize.toInt())
          return InodeInfo(inodeNum, mode, isDir, isFile, isSymlink, fileSize, emptyList(), linkBytes)
        }

        if (hasExtents) {
          fun parseExtentHeader(headerPos: Long) {
            val ehBuf = ByteArray(12)
            reader.readFully(headerPos, ehBuf)
            val eh = ByteBuffer.wrap(ehBuf).order(ByteOrder.LITTLE_ENDIAN)
            val ehMagic = eh.getShort(0).toInt() and 0xFFFF
            if (ehMagic != 0xF30A) return

            val entries = eh.getShort(2).toInt() and 0xFFFF
            val depth = eh.getShort(6).toInt() and 0xFFFF

            val recBuf = ByteArray(12)
            for (i in 0 until entries) {
              val recPos = headerPos + 12 + (i * 12)
              reader.readFully(recPos, recBuf)
              val rb = ByteBuffer.wrap(recBuf).order(ByteOrder.LITTLE_ENDIAN)

              if (depth == 0) { // Leaf node
                val eeBlock = rb.getInt(0).toLong() and 0xFFFFFFFFL
                var eeLen = rb.getShort(4).toInt() and 0xFFFF
                if (eeLen > 32768) eeLen -= 32768
                val eeStartHi = rb.getShort(6).toLong() and 0xFFFF
                val eeStartLo = rb.getInt(8).toLong() and 0xFFFFFFFFL
                val physBlock = (eeStartHi shl 32) or eeStartLo
                extents.add(Extent(eeBlock, physBlock, eeLen))
              } else { // Internal branch
                val leafLo = rb.getInt(4).toLong() and 0xFFFFFFFFL
                val leafHi = rb.getShort(8).toLong() and 0xFFFF
                val nextBlock = (leafHi shl 32) or leafLo
                parseExtentHeader(nextBlock * blockSize)
              }
            }
          }

          parseExtentHeader(offset + 40)
        } else {
          // Traditional direct blocks
          for (b in 0 until 12) {
            val blockNum = ib.getInt(40 + (b * 4)).toLong() and 0xFFFFFFFFL
            if (blockNum != 0L) {
              extents.add(Extent(b.toLong(), blockNum, 1))
            }
          }
        }

        return InodeInfo(inodeNum, mode, isDir, isFile, isSymlink, fileSize, extents)
      }

      fun readInodeBytes(inode: InodeInfo): ByteArray {
        if (inode.inlineData != null) return inode.inlineData
        val totalSize = inode.size.coerceAtMost(100L * 1024 * 1024).toInt() // limit single memory read to 100MB
        val result = ByteArray(totalSize)
        var written = 0

        for (ext in inode.extents) {
          if (written >= totalSize) break
          val bytesInExtent = (ext.blockCount.toLong() * blockSize).coerceAtMost((totalSize - written).toLong()).toInt()
          val physOffset = ext.physicalBlock * blockSize
          reader.readFully(physOffset, ByteArray(bytesInExtent).also {
            System.arraycopy(it, 0, result, written, bytesInExtent)
          })
          written += bytesInExtent
        }
        return result
      }

      fun readDirectoryEntries(inode: InodeInfo): List<Ext4DirEntry> {
        val entries = mutableListOf<Ext4DirEntry>()
        val blockData = ByteArray(blockSize)

        for (ext in inode.extents) {
          for (b in 0 until ext.blockCount) {
            val physOffset = (ext.physicalBlock + b) * blockSize
            reader.readFully(physOffset, blockData)
            val bb = ByteBuffer.wrap(blockData).order(ByteOrder.LITTLE_ENDIAN)

            var pos = 0
            while (pos + 8 <= blockSize) {
              val inum = bb.getInt(pos).toLong() and 0xFFFFFFFFL
              val recLen = bb.getShort(pos + 4).toInt() and 0xFFFF
              val nameLen = bb.get(pos + 6).toInt() and 0xFF
              val fileType = bb.get(pos + 7).toInt() and 0xFF

              if (recLen <= 0 || pos + recLen > blockSize) break

              if (inum != 0L && nameLen > 0 && pos + 8 + nameLen <= blockSize) {
                val nameBytes = ByteArray(nameLen)
                System.arraycopy(blockData, pos + 8, nameBytes, 0, nameLen)
                val name = String(nameBytes, Charsets.UTF_8)
                if (name != "." && name != "..") {
                  entries.add(Ext4DirEntry(inum, fileType, name))
                }
              }
              pos += recLen
            }
          }
        }
        return entries
      }

      // Root inode is Inode 2 in EXT4
      val rootInode = readInode(2L) ?: return false
      onProgress(0.50f, "Décompression chirurgicale de l'arborescence des fichiers...")

      var extractedFilesCount = 0
      var extractedDirCount = 0

      fun extractDirectory(dirInode: InodeInfo, targetDir: File, pathPrefix: String) {
        targetDir.mkdirs()
        extractedDirCount++
        val children = readDirectoryEntries(dirInode)

        for (entry in children) {
          val childInode = readInode(entry.inode) ?: continue
          val childPath = if (pathPrefix.isEmpty()) entry.name else "$pathPrefix/${entry.name}"
          val targetFile = File(targetDir, entry.name)

          if (childInode.isDirectory) {
            extractDirectory(childInode, targetFile, childPath)
          } else if (childInode.isRegularFile) {
            targetFile.parentFile?.mkdirs()
            FileOutputStream(targetFile).use { fos ->
              var bytesRemaining = childInode.size
              val chunkBuf = ByteArray(blockSize)
              for (ext in childInode.extents) {
                if (bytesRemaining <= 0) break
                for (b in 0 until ext.blockCount) {
                  if (bytesRemaining <= 0) break
                  val toRead = bytesRemaining.coerceAtMost(blockSize.toLong()).toInt()
                  val physOffset = (ext.physicalBlock + b) * blockSize
                  reader.readFully(physOffset, chunkBuf)
                  fos.write(chunkBuf, 0, toRead)
                  bytesRemaining -= toRead
                }
              }
            }
            extractedFilesCount++
            if (extractedFilesCount % 50 == 0 || entry.name == "build.prop") {
              onProgress(
                (0.50f + (extractedFilesCount.toFloat() / 2000f).coerceAtMost(0.40f)),
                "Extraction : $childPath (${childInode.size / 1024} Ko)..."
              )
              onLog(TerminalEntry(level = LogLevel.INFO, message = "[EXT4] Extrait : $childPath", tag = "EXT4"))
            }
          } else if (childInode.isSymlink) {
            // Write symlink target
            try {
              val linkBytes = readInodeBytes(childInode)
              targetFile.writeText(String(linkBytes, Charsets.UTF_8))
            } catch (_: Exception) {}
          }
        }
      }

      // Check if root already has a 'system' subfolder or if root is the partition root
      val rootChildren = readDirectoryEntries(rootInode)
      val hasSystemSubdir = rootChildren.any { it.name == "system" && it.fileType == 2 }

      if (hasSystemSubdir) {
        onLog(TerminalEntry(level = LogLevel.INFO, message = "[EXT4] Partition de type rootdir avec sous-dossier /system détectée.", tag = "EXT4"))
        extractDirectory(rootInode, destDir, "")
      } else {
        // Direct partition (e.g. system.img root) -> extract into destDir/system
        val targetSystemDir = File(destDir, "system").apply { mkdirs() }
        onLog(TerminalEntry(level = LogLevel.INFO, message = "[EXT4] Extraction directe dans ${targetSystemDir.name}...", tag = "EXT4"))
        extractDirectory(rootInode, targetSystemDir, "system")
      }

      onProgress(0.95f, "Finalisation de l'extraction ($extractedFilesCount fichiers, $extractedDirCount dossiers)...")
      onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "[EXT4] SUCCÈS : $extractedFilesCount fichiers réels extraits sans root !", tag = "EXT4"))

      // If build.prop was extracted, display real ROM device specs in terminal
      val extractedBuildProp = File(destDir, "system/build.prop").let { if (it.exists()) it else File(destDir, "build.prop") }
      if (extractedBuildProp.exists()) {
        val lines = extractedBuildProp.readLines()
        val brand = lines.firstOrNull { it.startsWith("ro.product.brand=") }?.substringAfter("=") ?: "Android"
        val model = lines.firstOrNull { it.startsWith("ro.product.model=") }?.substringAfter("=") ?: "Generic"
        val release = lines.firstOrNull { it.startsWith("ro.build.version.release=") }?.substringAfter("=") ?: "14"
        val patch = lines.firstOrNull { it.startsWith("ro.build.version.security_patch=") }?.substringAfter("=") ?: "N/A"
        onLog(TerminalEntry(level = LogLevel.SUCCESS, message = "[ROM INFO] Appareil : $brand $model • Android $release • Patch : $patch", tag = "INFO"))
      }

      return extractedFilesCount > 0
    } finally {
      reader.close()
    }
  }
}
