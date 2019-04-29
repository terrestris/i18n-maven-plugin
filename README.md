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
