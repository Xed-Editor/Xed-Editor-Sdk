package org.gradle.xed

import java.io.FileOutputStream
import java.util.*

fun createPropertiesFile(map: HashMap<String, String>, filePath: String) {
    val properties = Properties()
    for ((key, value) in map) {
        properties[key] = value
    }
    FileOutputStream(filePath).use { outputStream ->
        properties.store(outputStream, "")
    }
}