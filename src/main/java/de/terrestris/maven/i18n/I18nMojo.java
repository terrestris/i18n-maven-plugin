package de.terrestris.maven.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
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

@Execute(goal = "combine", phase = LifecyclePhase.GENERATE_RESOURCES)
@Mojo(name = "combine")
public class I18nMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void execute() throws MojoExecutionException {
        try {
            Log log = getLog();
            File dir = new File(project.getBasedir(), "src/main/resources/public");
            File outDir = new File(project.getBasedir(), "target/generated-resources/");
            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    throw new MojoExecutionException("Unable to create output directory.");
                }
            }
            Map<Object, Map<Object, Object>> map = new HashMap<>();
            combineJsonFiles(map, dir);
            log.info("Found i18n languages: " + map.keySet());
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
