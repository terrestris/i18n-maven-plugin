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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
      if (!outDir.exists()) {
        if (!outDir.mkdirs()) {
          throw new MojoExecutionException("Unable to create output directory.");
        }
      }
      Map<Object, Map<Object, Object>> map = new HashMap<>();
      combineJsonFiles(map, dir);
      LOG.info("Found i18n languages: " + map.keySet());
      for (Object key : map.keySet()) {
        mapper.writeValue(new File(outDir, key + ".json"), map.get(key));
      }
    } catch (Throwable t) {
      throw new MojoExecutionException("Unable to combine json i18n files:", t);
    }
  }

  private void combineJsonFiles(Map<Object, Map<Object, Object>> map, File dir) throws IOException {
    for (File file : Objects.requireNonNull(dir.listFiles())) {
      if (file.isDirectory()) {
        combineJsonFiles(map, file);
      }
      if (file.getName().endsWith(".i18n.json")) {
        Map current = mapper.readValue(file, Map.class);
        String name = file.getName().split("\\.")[0];
        for (Object lang : current.keySet()) {
          if (!map.containsKey(lang)) {
            map.put(lang, new HashMap<>());
          }
          Map<Object, Object> currentMap = map.get(lang);
          currentMap.put(name, current.get(lang));
        }
      }
    }
  }

}
