package de.terrestris.maven.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Add missing keys to the translation json files. Usage example:<br>
 *<br>
 * mvn i18n:fill -Di18n.sourceLanguage=de -Di18n.targetLanguage=en<br>
 *<br>
 * This will add all missing keys in english with a default value
 * of en:GermanTranslation as value. This can also be used to add
 * a new translation language.
 */
@Execute(goal = "fill", phase = LifecyclePhase.NONE)
@Mojo(name = "fill")
public class I18nFillMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  /**
   * Set to false to disable json pretty printing.
   */
  @Parameter(property = "i18n.format", defaultValue = "true")
  private boolean format;

  /**
   * The source language parameter, e.g. de.
   */
  @Parameter(property = "i18n.sourceLanguage", required = true)
  private String sourceLanguage;

  /**
   * The target language parameter, e.g. en.
   */
  @Parameter(property = "i18n.targetLanguage", required = true)
  private String targetLanguage;

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void execute() throws MojoExecutionException {
    try {
      if (format) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
      }
      File dir = new File(project.getBasedir(), "src/main/resources/public");
      fillMissingValues(dir);
    } catch (Throwable t) {
      throw new MojoExecutionException("Unable to combine json i18n files:", t);
    }
  }

  private void fillMissingValues(File dir) throws IOException {
    for (File file : Objects.requireNonNull(dir.listFiles())) {
      if (file.isDirectory()) {
        fillMissingValues(file);
      }
      if (file.getName().endsWith(".i18n.json")) {
        Map contents = mapper.readValue(file, Map.class);
        Map source = (Map) contents.get(sourceLanguage);
        Map target = (Map) contents.get(targetLanguage);
        if (target == null) {
          target = new HashMap();
          contents.put(targetLanguage, target);
        }
        for (Object key : source.keySet()) {
          if (!target.containsKey(key)) {
            target.put(key, targetLanguage + ":" + source.get(key));
          }
        }
        mapper.writeValue(file, contents);
      }
    }
  }

}
