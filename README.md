# overcast-maven-plugin

This is Maven plugin that helps setup integration tests which depends on the [Overcast](https://github.com/xebialabs/overcast) setup. If you do not know what the Overcast is, or you do not know how it works, I suggest to read the following blog post by the [Paul van der Ende](http://blog.xebia.com/author/pvanderende):

[Fast and Easy integration testing with Docker and Overcast](http://blog.xebia.com/2014/10/13/fast-and-easy-integration-testing-with-docker-and-overcast)

## Rationalle

It's widely proven statement that doing integration tests with Overcast is **fast and easy**. I can guarantee that is indeed true, but only to some extent because when project grow needing more and more dependencies, you may find it very difficult to wire everything together because these additional dependencies may conflict with the ones used by the Overcast (e.g. HTTP Client, AWS SDK, Jackson, etc). I found it very difficult to resolve conflicts between dependencies used in both, my project, and the Overcast. These conflicts caused all integration tests to fail instantly. Thus I came up with the idea of excluding whole Overcast-related stuff to the Maven build lifecycle so it does not conflict with the runtime. All that is left in project code is a very simple helper class that has no dependencies and uses pure JRE classes.

## How To Use

Put your ```overcast.conf``` file in the project root directory (this path cannot be changed for now) so the project layout looks like the following:

```plain
src
 \_ main
     \_ java
    test
     \_ java
pom.xml
overcast.conf
```

The example ```overcast.conf``` may look like the following (the below example defines only one [docker](https://www.docker.com/) instance):

```plain
mysql {
	dockerHost="unix:///var/run/docker.sock"
	dockerImage="mysql:5.6"
	exposeAllPorts=true
	remove=true
	env=["MYSQL_ROOT_PASSWORD=foo","MYSQL_USER=root","MYSQL_DATABASE=bar"]
	command=["mysqld"]
}
```

The plugin configuration for the above:

```xml
<plugin>
	<groupId>com.github.sarxos</groupId>
	<artifactId>overcast-maven-plugin</artifactId>
	<version>0.1.4</version>
	<configuration>
		<conf>${project.basedir}/overcast.conf</conf>
		<instances>
			<instance>
				<name>mysql</name>
				<ports>
					<port>3306</port>
				</ports>
			</instance>
		</instances>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>setup</goal>
				<goal>teardown</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

In your integration test you can now use ```OvercastHelper``` (see source code in next paragraph) to read instances ports and hosts:

```java
@BeforeClass
public static void prepare() {
	host = OvercastHelper.getHostName("mysql");
	port = OvercastHelper.getPort("mysql", 3306);
}
```

The last thing you need to do is to execute integration tests:

```plain
$ mvn clean verify
```

## How It Works

The plugin takes all instances from configuration in ```POM``` and spawn them by using settings from Overcast. You must specify what ports you will be using, example:

```xml
<instances>
	<instance>
		<name>mysql</name>
		<ports>
			<port>3306</port>
		</ports>
	</instance>
</instances>
```

Plugin will setup these instances in the ```pre-integration-test``` phase and tear them down in ```post-integration-test``` phase. The instances metadata such as port formwarding and target host name are stored in classpath in the ```overcast.ser``` file. This file can be read with the following helper class (I plan to put it in Maven Central as well, but for now one have to copy and pase it into own code base):

```java
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;


/**
 * Helper class that will read metadata stored by the overcast-maven-plugin.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class OvercastHelper {

	private static final InputStream stream(String name) {
		return OvercastHelper.class.getClassLoader().getResourceAsStream(name);
	}

	@SuppressWarnings("unchecked")
	private static final Map<?, ?> mapping(String name) {

		List<Map<?, ?>> mappings = null;

		try (ObjectInputStream ois = new ObjectInputStream(stream("overcast.ser"))) {
			mappings = (List<Map<?, ?>>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		for (Map<?, ?> mapping : mappings) {
			if (name.equals(mapping.get("name"))) {
				return mapping;
			}
		}

		throw new NoSuchElementException("No mapping with name " + name + " has been found");
	}

	public static String getHostName(String name) {
		return mapping(name).get("host").toString();
	}

	public static int getPort(String name, int port) {

		Map<?, ?> mapping = mapping(name);
		Map<?, ?> ports = (Map<?, ?>) mapping.get("ports");
		String pkey = Integer.toString(port);

		for (Entry<?, ?> entry : ports.entrySet()) {
			String key = entry.getKey().toString();
			if (key.equals(pkey)) {
				return Integer.valueOf(entry.getValue().toString());
			}
		}

		throw new NoSuchElementException("Port " + port + " is not forwarded");
	}
}
```

## License

Copyright (C) 2015 Bartosz Firyn (https://github.com/sarxos)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

