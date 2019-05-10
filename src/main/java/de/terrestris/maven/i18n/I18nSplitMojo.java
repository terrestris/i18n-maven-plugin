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
import java.util.Map;
import java.util.Objects;

@Execute(goal = "split", phase = LifecyclePhase.NONE)
@Mojo(name = "split")
public class I18nSplitMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(required = true, property = "i18n.file")
    private String file;

    @Parameter(required = true, property = "i18n.language")
    private String language;

    @Parameter(property = "i18n.format", defaultValue = "true")
    private boolean format;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (format) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            }
            File dir = new File(project.getBasedir(), "src/main/resources/public");
            File file = new File(this.file);
            Map map = mapper.readValue(file, Map.class);
            splitJsonFile(map, dir);
        } catch (Throwable t) {
            throw new MojoExecutionException("Unable to combine json i18n files:", t);
        }
    }

    private void splitJsonFile(Map<Object, Map<Object, Object>> map, File dir) throws IOException {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                splitJsonFile(map, file);
            }
            if (file.getName().endsWith(".i18n.json")) {
                Map contents = mapper.readValue(file, Map.class);
                Map current = (Map) contents.get(language);
                String name = file.getName().split("\\.")[0];
                Map newValues = map.get(name);
                for (Object key : newValues.keySet()) {
                    current.put(key, newValues.get(key));
                }
                mapper.writeValue(file, contents);
            }
        }
    }

}
