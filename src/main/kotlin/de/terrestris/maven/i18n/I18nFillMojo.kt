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
 * Add missing keys to the translation json files. Usage example:<br></br>
 * <br></br>
 * mvn i18n:fill -Di18n.sourceLanguage=de -Di18n.targetLanguage=en<br></br>
 * <br></br>
 * This will add all missing keys in english with a default value
 * of en:GermanTranslation as value. This can also be used to add
 * a new translation language.
 */
@Execute(goal = "fill", phase = LifecyclePhase.NONE)
@Mojo(name = "fill")
class I18nFillMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    private lateinit var project: MavenProject

    /**
     * Set to false to disable json pretty printing.
     */
    @Parameter(property = "i18n.format", defaultValue = "true")
    private val format: Boolean = false

    /**
     * The source language parameter, e.g. de.
     */
    @Parameter(property = "i18n.sourceLanguage", required = true)
    private lateinit var sourceLanguage: String

    /**
     * The target language parameter, e.g. en.
     */
    @Parameter(property = "i18n.targetLanguage", required = true)
    private lateinit var targetLanguage: String

    private val mapper = ObjectMapper()

    override fun execute() {
        try {
            if (format) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            val dir = File(project.basedir, "src/main/resources/public")
            fillMissingValues(dir)
        } catch (t: Throwable) {
            throw MojoExecutionException("Unable to combine json i18n files:", t)
        }

    }

    private fun fillMissingValues(dir: File) {
        for (file in Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory) {
                fillMissingValues(file)
            }
            if (file.name.endsWith(".i18n.json")) {
                val contents = mapper.readValue(file, Map::class.java) as MutableMap<String, Any>
                val source = contents[sourceLanguage] as MutableMap<String, Any>?
                var target = contents[targetLanguage] as MutableMap<String, Any>?
                if (target == null) {
                    target = HashMap()
                    contents[targetLanguage] = target
                }
                for (key in source!!.keys) {
                    if (!target.containsKey(key)) {
                        target[key ] = """$targetLanguage:${source[key]}""" as Any
                    }
                }
                mapper.writeValue(file, contents)
            }
        }
    }

}
