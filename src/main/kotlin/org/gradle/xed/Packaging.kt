package org.gradle.xed

import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.compile.CompilationFailedException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.*

class Packaging (inputDir:File,outputDir:File,name:String,proprtiesFile:File){
    init {
        if (inputDir.exists().not()){
            throw GradleException("Packaging : Input Directory doesn't exist")
        }
        if (outputDir.exists().not()){
            outputDir.mkdirs()
        }

        val code = File(inputDir,"code").also { if (it.exists().not()){it.mkdirs()} }

        fun convertJarToDex(jarPath: Path, outputDir: Path) {
            try {
                val command = D8Command.builder()
                    .addProgramFiles(jarPath)
                    .setOutput(outputDir, com.android.tools.r8.OutputMode.DexIndexed)
                    .build()
                D8.run(command)
                File(outputDir.toFile(),"classes.dex").renameTo(File(outputDir.toFile(),jarPath.toFile().name.removeSuffix(".jar")+".dex"))
            } catch (e: CompilationFailedException) {
                throw GradleException("D8 conversion failed for $jarPath: ${e.message}", e)
            }
        }


        val properties = Properties()

        if (proprtiesFile.exists()) {
            FileInputStream(proprtiesFile).use { inputStream ->
                properties.load(inputStream)
            }
        }

        val libs = mutableListOf<String>()

        runCatching {
            inputDir.listFiles()!!.forEach { file ->
                if (file.name.endsWith("jar")){
                    convertJarToDex(file.toPath(),code.toPath())
                    libs.add(file.name)
                    file.delete()
                }
            }

            val serializedArray = libs.joinToString(",")
            properties.setProperty("code", serializedArray)

            FileOutputStream(proprtiesFile).use { outputStream ->
                properties.store(outputStream, "code")
            }

            val zip = File(outputDir,"$name.plugin")
            with(zip){
                if (exists()){
                    delete()
                    createNewFile()
                }else{
                    createNewFile()
                }
            }

            createZipFile(inputDir, zip)
            inputDir.deleteRecursively()
        }.onFailure {
            throw GradleException(it.message+"\n"+it.stackTraceToString(),it.cause)
        }
    }
}