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
import java.util.*

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

    override fun execute() {
        try {
            if (format) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            val dir = File(project!!.basedir, "src/main/resources/public")
            val outDir = File(project.basedir, "target/generated-resources/" + pathPrefix!!)
            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    throw MojoExecutionException("Unable to create output directory.")
                }
            }
            val map = HashMap<Any, MutableMap<Any, Any>>()
            combineJsonFiles(map, dir)
            log.info("Found i18n languages: " + map.keys)
            for (key in map.keys) {
                mapper.writeValue(File(outDir, "$key.json"), map[key])
            }
        } catch (e: Exception) {
            throw MojoExecutionException("Unable to combine json i18n files:", e)
        }

    }

    private fun combineJsonFiles(map: MutableMap<Any, MutableMap<Any, Any>>, dir: File) {
        for (file in Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory) {
                combineJsonFiles(map, file)
            }
            if (file.name.endsWith(".i18n.json")) {
                val current = mapper.readValue(file, Map::class.java)
                val name = file.name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                for (lang in current.keys) {
                    if (!map.containsKey(lang)) {
                        map[lang as Any] = HashMap()
                    }
                    val currentMap = map[lang]
                    currentMap!![name] = current[lang] as Any
                }
            }
        }
    }

}
