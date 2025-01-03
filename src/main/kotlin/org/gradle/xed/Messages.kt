package org.gradle.xed


fun getKeepLibMesage(name:String):String{
    return """
    Some libraries will not be packaged with the plugin to prevent code duplication at runtime.
    If your plugin crashes at runtime, add the library name to the `keepDependencies` block. For example:

    keepDependencies {
        // Forcefully keep a library
        // Note : only do this if the plugin is crashing, forcefully keeping a library may cause performance issues
        keep("${name}")
    }
    """.trimIndent()
}

