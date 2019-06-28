package de.terrestris.maven.i18n

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Execute
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.Objects

/**
 * Overwrite values in the i18n json files. Uses a combined file
 * like the one generated from the combine goal to overwrite
 * the values in the source files. Usage example:<br></br>
 * <br></br>
 * mvn i18n:split -Di18n.file=de.json -Di18n.language<br></br>
 * <br></br>
 * Can also be used to add a new translation.
 */
@Execute(goal = "split", phase = LifecyclePhase.NONE)
@Mojo(name = "split")
class I18nSplitMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    private val project: MavenProject? = null

    /**
     * The combined input file.
     */
    @Parameter(required = true, property = "i18n.file")
    private val file: String? = null

    /**
     * The language string.
     */
    @Parameter(required = true, property = "i18n.language")
    private val language: String? = null

    /**
     * Set to false to disable json pretty printing.
     */
    @Parameter(property = "i18n.format", defaultValue = "true")
    private val format: Boolean = false

    private val mapper = ObjectMapper()

    @Throws(MojoExecutionException::class)
    override fun execute() {
        try {
            if (format) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            val dir = File(project!!.basedir, "src/main/resources/public")
            val file = File(this.file!!)
            val map = mapper.readValue(file, Map::class.java) as Map<String, Any>
            splitJsonFile(map, dir)
        } catch (t: Throwable) {
            throw MojoExecutionException("Unable to combine json i18n files:", t)
        }

    }

    @Throws(IOException::class)
    private fun splitJsonFile(map: Map<String, Any>, dir: File) {
        for (file in Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory) {
                splitJsonFile(map, file)
            }
            if (file.name.endsWith(".i18n.json")) {
                val contents = mapper.readValue(file, MutableMap::class.java) as MutableMap<String, Any>
                var current = contents.get(language) as MutableMap<String, Any>?
                if (current == null) {
                    current = HashMap()
                    contents[language as String] = current
                }
                val name = file.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val newValues = map[name] as MutableMap<String, String>
                for (key in newValues.keys) {
                    current[key] = newValues[key] as String
                }
                mapper.writeValue(file, contents)
            }
        }
    }

}
