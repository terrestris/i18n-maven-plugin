package de.terrestris.maven.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Add missing keys to the translation json files. Usage example:<br>
 * <br>
 * mvn i18n:fill -Di18n.sourceLanguage=de -Di18n.targetLanguage=en<br>
 * <br>
 * This will add all missing keys in english with a default value
 * of en:GermanTranslation as value. This can also be used to add
 * a new translation language.
 */
@Execute(goal = "fill", phase = LifecyclePhase.NONE)
@Mojo(name = "fill")
public class I18nFillMojo extends AbstractMojo {

  private static final Logger LOG = LoggerFactory.getLogger(I18nFillMojo.class);

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
      if (!dir.exists()) {
        LOG.info("Skip filling i18n entries, public directory does not exist.");
        return;
      }
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
        ObjectNode contents = (ObjectNode) mapper.readTree(file);
        JsonNode source = contents.get(sourceLanguage);
        ObjectNode target = (ObjectNode) contents.get(targetLanguage);
        if (target == null) {
          target = mapper.createObjectNode();
          contents.set(targetLanguage, target);
        }
        for (Iterator<Entry<String, JsonNode>> it = source.fields(); it.hasNext(); ) {
          Entry<String, JsonNode> field = it.next();
          if (!target.has(field.getKey())) {
            target.put(field.getKey(), targetLanguage + ":" + field.getValue().asText());
          }
        }
        mapper.writeValue(file, contents);
      }
    }
  }

}
