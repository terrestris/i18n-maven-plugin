# i18n-maven-plugin

Maven plugin to work with i18n json files. Currently includes a `combine` goal
that combines i18n files like this:

* `src/main/resources/public` and subdirectories are searched
* files named `SomeComponent.i18n.json` are processed

i18n files should look like this:

```json
{
  "de": {
    "someKey": "Beschreibung"
  },
  "en": {
    "someKey": "Some description"
  }
}
```

After processing, a `de.json` and a `en.json` file will be generated (example `de.json`):

```json
{
  "SomeComponent": {
    "someKey": "Beschreibung"
  }
}
```

# Usage

Configure the plugin in your `pom.xml` like this:

```xml
            <plugin>
                <groupId>de.terrestris</groupId>
                <artifactId>i18n-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>generate-i18n</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>combine</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

# Other mojos

See the plugin help for details:

```
mvn i18n:help -Ddetail=true
```

# Deployment

Artifacts are deployed manually to the terrestris Reposilite instance (`https://maven.terrestris.de/releases` and `https://maven.terrestris.de/snapshots`).

Add a server entry with a Reposilite token that has write access to your `~/.m2/settings.xml` for each of the ids `reposilite-repository-releases` and `reposilite-repository-snapshots`:

```xml
<server>
  <id>reposilite-repository-releases</id>
  <username>YOUR_USER</username>
  <password>YOUR_TOKEN</password>
</server>
<server>
  <id>reposilite-repository-snapshots</id>
  <username>YOUR_USER</username>
  <password>YOUR_TOKEN</password>
</server>
```

## Release

Use the Maven release plugin. It sets the release version, tags it, deploys
the artifacts (`deploy` goal) and bumps to the next snapshot version:

```sh
mvn release:prepare
mvn release:perform
```
