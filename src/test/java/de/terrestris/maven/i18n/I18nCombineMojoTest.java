package de.terrestris.maven.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nCombineMojoTest {

  private static final String PREFIX = "public/i18n";

  private final ObjectMapper mapper = new ObjectMapper();

  @TempDir
  Path basedir;

  @Test
  void combinesFilesIntoOneFilePerLanguage() throws Exception {
    write("src/main/resources/public/src/App.i18n.json",
      "{\"de\":{\"title\":\"Titel\"},\"en\":{\"title\":\"Title\"}}");
    write("src/main/resources/public/src/components/Footer/Footer.i18n.json",
      "{\"de\":{\"imprint\":\"Impressum\"},\"en\":{\"imprint\":\"Imprint\"}}");
    write("src/main/resources/public/src/ignored.json", "{\"de\":{\"x\":\"y\"}}");

    run();

    JsonNode de = read("de.json");
    assertEquals("Titel", de.get("App").get("title").asText());
    assertEquals("Impressum", de.get("Footer").get("imprint").asText());
    assertEquals(2, de.size());

    JsonNode en = read("en.json");
    assertEquals("Title", en.get("App").get("title").asText());
    assertEquals("Imprint", en.get("Footer").get("imprint").asText());
  }

  @Test
  void skipsGenerationWithoutPublicDirectory() throws Exception {
    run();

    assertFalse(outputFile("de.json").exists());
  }

  @Test
  void formatsOutputWhenRequested() throws Exception {
    write("src/main/resources/public/App.i18n.json", "{\"de\":{\"title\":\"Titel\"}}");

    run(true);

    assertTrue(Files.readString(outputFile("de.json").toPath(), StandardCharsets.UTF_8).contains("\n"));
  }

  private void run() throws Exception {
    run(false);
  }

  private void run(boolean format) throws Exception {
    MavenProject project = new MavenProject();
    project.setFile(basedir.resolve("pom.xml").toFile());

    I18nCombineMojo mojo = new I18nCombineMojo();
    set(mojo, "project", project);
    set(mojo, "pathPrefix", PREFIX);
    set(mojo, "format", format);
    mojo.execute();
  }

  private void write(String path, String content) throws Exception {
    Path file = basedir.resolve(path);
    Files.createDirectories(file.getParent());
    Files.writeString(file, content, StandardCharsets.UTF_8);
  }

  private File outputFile(String name) {
    return basedir.resolve("target/generated-resources/" + PREFIX).resolve(name).toFile();
  }

  private JsonNode read(String name) throws Exception {
    return mapper.readTree(outputFile(name));
  }

  private static void set(Object target, String field, Object value) throws Exception {
    java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
    f.setAccessible(true);
    f.set(target, value);
  }
}
