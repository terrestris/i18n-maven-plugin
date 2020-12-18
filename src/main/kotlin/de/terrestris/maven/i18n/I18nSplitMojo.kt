package de.terrestris.maven.i18n

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Execute
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

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
    private lateinit var project: MavenProject

    /**
     * The combined input file.
     */
    @Parameter(required = true, property = "i18n.file")
    private lateinit var file: String

    /**
     * The language string.
     */
    @Parameter(required = true, property = "i18n.language")
    private lateinit var language: String

    /**
     * Set to false to disable json pretty printing.
     */
    @Parameter(property = "i18n.format", defaultValue = "true")
    private val format: Boolean = false

    private val mapper = ObjectMapper()

    private val factory = JsonNodeFactory(false)

    override fun execute() {
        if (format) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
        }
        val dir = File(project.basedir, "src/main/resources/public")
        val file = File(this.file)
        val map = mapper.readTree(file) as ObjectNode
        splitJsonFile(map, dir)
    }

    private fun splitJsonFile(node: ObjectNode, dir: File) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                splitJsonFile(node, file)
            }
            if (file.name.endsWith(".i18n.json")) {
                val contents = mapper.readTree(file) as ObjectNode
                var current: ObjectNode? = contents.get(language) as ObjectNode?
                if (current == null) {
                    current = factory.objectNode()
                    contents.set(language, current)
                }
                val name = file.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.first()
                val newValues = node.get(name) as ObjectNode
                for (key in newValues.fieldNames()) {
                    current!!.set(key, newValues.get(key))
                }
                mapper.writeValue(file, contents)
            }
        }
    }

}
