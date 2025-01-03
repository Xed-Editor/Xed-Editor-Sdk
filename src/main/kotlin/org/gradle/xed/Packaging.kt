package org.gradle.xed

import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.compile.CompilationFailedException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path

class Packaging (inputDir:File,outputDir:File,name:String){
    init {
        if (inputDir.exists().not()){
            throw GradleException("Packaging : Input Directory doesn't exist")
        }
        if (outputDir.exists().not()){
            outputDir.mkdirs()
        }

        fun create(inputFile: String, outputFile: String) {
            val keyBytes = arrayOf<Byte>(88, 101, 100, 45, 69, 100, 105, 116, 111, 114)
            val keyLength = keyBytes.size

            FileInputStream(inputFile).use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var keyIndex = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        for (i in 0 until bytesRead) {
                            buffer[i] = (buffer[i].toInt() xor keyBytes[keyIndex % keyLength].toInt()).toByte()
                            keyIndex++
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
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

        runCatching {
            inputDir.listFiles()!!.forEach { file ->
                if (file.name.endsWith("jar")){
                    convertJarToDex(file.toPath(),code.toPath())
                    file.delete()
                }
            }

            val zip = File(inputDir.parent,"tempBuild")
            val xp = File(outputDir,"$name.xp")

            with(xp){
                if (exists()){
                    delete()
                    createNewFile()
                }else{
                    createNewFile()
                }
            }

            with(zip){
                if (exists()){
                    delete()
                    createNewFile()
                }else{
                    createNewFile()
                }
            }


            createZipFile(inputDir, zip)
            create(zip.absolutePath,xp.absolutePath)
            zip.delete()
            inputDir.deleteRecursively()
        }.onFailure {
            throw GradleException(it.message+"\n"+it.stackTraceToString(),it.cause)
        }
    }
}