package org.gradle.xed

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.nio.file.Files

const val TargetVersionCode = 38
private val keepLibs = mutableListOf<String>()


class Main : Plugin<Project> {
    private lateinit var pluginInfo: PluginInfo

    override fun apply(project: Project) {
        with(project) {
            pluginInfo = extensions.create("pluginInfo", PluginInfo::class.java)
            project.extensions.create("keepDependencies", KeepDependenciesExtension::class.java)

            dependencies.add("compileOnly", files(File(layout.projectDirectory.asFile,"sdk/xed-editor-sdk.jar")))

            tasks.named("jar", Jar::class.java).configure { jarTask ->
                val tmpDir = File(layout.buildDirectory.asFile.get(), "xtmp").also {
                    if (it.exists().not()) {
                        it.mkdirs()
                    }else{
                        it.deleteRecursively()
                        it.mkdirs()
                    }
                }

                tmpDir.listFiles()?.forEach { it.deleteRecursively() }
                with(jarTask) {
                    destinationDirectory.set(tmpDir)
                    val prop = File(tmpDir, "manifest.properties")
                    createPropertiesFile(processManifest(pluginInfo), prop.absolutePath)

                    val res = File(layout.projectDirectory.asFile, "src/main/resources")
                    if (res.exists() and res.listFiles().isNullOrEmpty().not()) {
                        copyDirectory(res, File(tmpDir, "assets"))
                    }

                    archiveFileName.set("${pluginInfo.pluginName}.jar")
                    doLast {
                        jarTask.postJarVerify(pluginInfo)

                        val runtimeJars = configurations.getByName("runtimeClasspath")
                            .resolvedConfiguration
                            .resolvedArtifacts
                            .filter { it.file.name.endsWith(".jar") }
                            .map { it.file }

                        var ignored = false
                        runtimeJars.forEach { runtimeJar ->
                            if (runtimeJar.absolutePath.contains("org.jetbrains") || runtimeJar.absolutePath.contains("kotlin")) {
                                if (keepLibs.contains(runtimeJar.nameWithoutExtension).not()) {
                                    if (runtimeJar.nameWithoutExtension.contains("kotlin-stdlib").not() && runtimeJar.nameWithoutExtension.contains("annotations-").not()){
                                        ignored = true
                                        val yellowColor = "\u001B[33m"
                                        val resetColor = "\u001B[0m"

                                        println("${yellowColor}Warning: Ignoring Library: ${runtimeJar.nameWithoutExtension}${resetColor}")
                                    }
                                } else {
                                    println("Info : Keeping ${runtimeJar.nameWithoutExtension}")
                                    Files.copy(runtimeJar.toPath(),File(tmpDir,runtimeJar.name).toPath())
                                }
                            }else{
                                Files.copy(runtimeJar.toPath(),File(tmpDir,runtimeJar.name).toPath())
                            }
                        }


                        if (ignored) {
                            println(getKeepLibMesage(runtimeJars.first().nameWithoutExtension))
                        }

                        Packaging(tmpDir,pluginInfo.outputDir ?: File(layout.projectDirectory.asFile,"output"),pluginInfo.pluginName!!,prop)
                    }
                }

            }

        }
    }


}

open class KeepDependenciesExtension {
    fun keep(notation: String) {
        keepLibs.add(notation)
    }
}
