package org.gradle.xed

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun createZipFile(sourceDir: File, outputZip: File, exclude: File? = null) {
    ZipOutputStream(outputZip.outputStream().buffered()).use { zipOut ->
        sourceDir.walkTopDown().filter { it != exclude && it.isFile }.forEach { file ->
            val zipEntry = ZipEntry(sourceDir.toPath().relativize(file.toPath()).toString())
            zipOut.putNextEntry(zipEntry)
            file.inputStream().use { input -> input.copyTo(zipOut) }
            zipOut.closeEntry()
        }
    }
}