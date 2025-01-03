package org.gradle.xed

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

fun copyDirectory(sourceDir: File, targetDir: File) {
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        throw IllegalArgumentException("Source directory does not exist or is not a directory: ${sourceDir.absolutePath}")
    }

    if (!targetDir.exists()) {
        targetDir.mkdirs() // Create target directory if it doesn't exist
    }

    sourceDir.listFiles()?.forEach { file ->
        val targetFile = File(targetDir, file.name)
        if (file.isDirectory) {
            // Recursively copy subdirectories
            copyDirectory(file, targetFile)
        } else {
            // Copy files
            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}