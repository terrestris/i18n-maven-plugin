package de.terrestris.maven.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterators;
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
 * Combine small json files into a complete translation file. See the
 * README on <a href="https://github.com/terrestris/i18n-maven-plugin/">Github</a> for details.
 * It is best integrated into your pom in the generate-resources phase.
 */
@Execute(goal = "combine", phase = LifecyclePhase.GENERATE_RESOURCES)
@Mojo(name = "combine")
public class I18nCombineMojo extends AbstractMojo {

  private static final Logger LOG = LoggerFactory.getLogger(I18nCombineMojo.class);

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter
  private String pathPrefix;

  /**
   * Set to true to enable pretty printing for the output json.
   */
  @Parameter(property = "i18n.format", defaultValue = "false")
  private boolean format;

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void execute() throws MojoExecutionException {
    try {
      if (format) {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
      }
      File dir = new File(project.getBasedir(), "src/main/resources/public");
      File outDir = new File(project.getBasedir(), "target/generated-resources/" + pathPrefix);
      if (!outDir.exists() && !outDir.mkdirs()) {
        throw new MojoExecutionException("Unable to create output directory.");
      }
      if (!dir.exists()) {
        LOG.info("No public directory found, skipping i18n generation.");
        return;
      }
      ObjectNode root = mapper.createObjectNode();
      combineJsonFiles(root, dir);
      if (LOG.isInfoEnabled()) {
        LOG.info("Found i18n languages: {}", Iterators.toString(root.fieldNames()));
      }
      for (Iterator<Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
        Entry<String, JsonNode> field = it.next();
        mapper.writeValue(new File(outDir, field.getKey() + ".json"), field.getValue());
      }
    } catch (Exception t) {
      throw new MojoExecutionException("Unable to combine json i18n files:", t);
    }
  }

  private void combineJsonFiles(ObjectNode root, File dir) throws IOException {
    for (File file : Objects.requireNonNull(dir.listFiles())) {
      if (file.isDirectory()) {
        combineJsonFiles(root, file);
      }
      if (file.getName().endsWith(".i18n.json")) {
        JsonNode current = mapper.readTree(file);
        String name = file.getName().split("\\.")[0];
        for (Iterator<Entry<String, JsonNode>> it = current.fields(); it.hasNext(); ) {
          Entry<String, JsonNode> field = it.next();
          String lang = field.getKey();
          if (!root.has(lang)) {
            root.set(lang, mapper.createObjectNode());
          }
          ObjectNode currentMap = (ObjectNode) root.get(lang);
          currentMap.set(name, field.getValue().get(lang));
        }
      }
    }
  }

}
