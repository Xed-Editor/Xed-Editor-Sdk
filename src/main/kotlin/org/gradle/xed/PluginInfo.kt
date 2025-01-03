package org.gradle.xed

import org.gradle.api.GradleException
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.HashMap
import java.util.jar.JarFile

open class PluginInfo {
    var pluginName: String? = null
    var versionCode: Int = -1
    var versionName: String? = null
    var packageName: String? = null
    var mainClass: String? = null
    var author: String? = null


    //optional
    val targetVersionCode = TargetVersionCode
    var settingsClass: String? = null
    var pluginWebsite: String? = null
    var outputDir: File? = null
}


fun PluginInfo.verify() {
    if (pluginName.isNullOrEmpty()) {
        throw GradleException("Error: 'pluginName' is invalid or not configured. Please set pluginName to a non-null and non-empty value.")
    }
    if (author.isNullOrEmpty()) {
        throw GradleException("Error: 'author' is invalid or not configured. Please set author to a non-null and non-empty value.")
    }
    if (versionCode <= 0) {
        throw GradleException("Error: 'versionCode' must be greater than 0.")
    }
    if (versionName.isNullOrEmpty()) {
        throw GradleException("Error: 'versionName' is invalid or not configured. Please set versionName to a non-null and non-empty value.")
    }
    if (packageName.isNullOrEmpty()) {
        throw GradleException("Error: 'packageName' is invalid or not configured. Please set packageName to a non-null and non-empty value.")
    }
    if (mainClass.isNullOrEmpty()) {
        throw GradleException("Error: 'mainClass' is invalid or not configured. Please set mainClass to a non-null and non-empty value.")
    }
}

//full class name eg com.example.Main
private fun Jar.checkClassExists(clazz: String): Boolean {
    val jarFile = archiveFile.get().asFile

    // Convert the class name to the appropriate file path format and append the ".class" extension
    val classFilePath = clazz.replace(".", "/") + ".class"

    return JarFile(jarFile).use { jar ->
        // Check if any entry in the jar matches the class file path
        jar.entries().asSequence().any { it.name == classFilePath }
    }
}

fun Jar.postJarVerify(pluginInfo: PluginInfo) {
    if (pluginInfo.mainClass.isNullOrEmpty().not() && checkClassExists(pluginInfo.mainClass!!).not()) {
        throw GradleException("Specified mainClass : ${pluginInfo.mainClass} doesn't exist in the JAR.")
    }
    if (pluginInfo.settingsClass.isNullOrEmpty().not()) {
        if (checkClassExists(pluginInfo.settingsClass!!).not()) {
            throw GradleException("Specified settingsClass : ${pluginInfo.settingsClass} doesn't exist in the JAR.")
        }
    }
}

fun processManifest(pluginInfo: PluginInfo): HashMap<String, String> {
    with(pluginInfo) {
        pluginInfo.verify()

        return hashMapOf(
            "name" to pluginName!!,
            "author" to author!!,
            "version" to versionName!!,
            "versionCode" to versionCode.toString(),
            "packageName" to packageName!!,
            "mainClass" to mainClass!!,
            "pluginWebsite" to pluginWebsite.orEmpty(),
            "settingsClass" to settingsClass.orEmpty()
        )
    }
}
