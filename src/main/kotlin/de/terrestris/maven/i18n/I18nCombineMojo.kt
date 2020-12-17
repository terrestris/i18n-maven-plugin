package de.terrestris.maven.i18n

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Execute
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

/**
 * Combine small json files into a complete translation file. See the
 * README at https://github.com/terrestris/i18n-maven-plugin/ for details.
 * It is best integrated into your pom in the generate-resources phase.
 */
@Execute(goal = "combine", phase = LifecyclePhase.GENERATE_RESOURCES)
@Mojo(name = "combine")
class I18nCombineMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    private lateinit var project: MavenProject

    @Parameter
    private lateinit var pathPrefix: String

    /**
     * Set to true to enable pretty printing for the output json.
     */
    @Parameter(property = "i18n.format", defaultValue = "false")
    private val format: Boolean = false

    private val mapper = ObjectMapper()

    private val factory = JsonNodeFactory(false)

    override fun execute() {
        try {
            if (format) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            val dir = File(project.basedir, "src/main/resources/public")
            val outDir = File(project.basedir, "target/generated-resources/$pathPrefix")
            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    throw MojoExecutionException("Unable to create output directory.")
                }
            }
            val node = factory.objectNode()
            combineJsonFiles(node, dir)
            log.info("Found i18n languages: " + node.fieldNames())
            for (key in node.fieldNames()) {
                mapper.writeValue(File(outDir, "$key.json"), node.get(key))
            }
        } catch (e: Exception) {
            throw MojoExecutionException("Unable to combine json i18n files:", e)
        }

    }

    private fun combineJsonFiles(node: ObjectNode, dir: File) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                combineJsonFiles(node, file)
            }
            if (file.name.endsWith(".i18n.json")) {
                val current = mapper.readTree(file) as ObjectNode
                val name = file.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                for (lang in current.fieldNames()) {
                    if (!node.has(lang)) {
                        node.set(lang, factory.objectNode())
                    }
                    val currentMap = node.get(lang) as ObjectNode
                    currentMap.set(name, current.get(lang))
                }
            }
        }
    }

}
