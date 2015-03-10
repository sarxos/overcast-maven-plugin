package com.github.sarxos.overcast;

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

	/**
	 * Open stream to the resource in classpath.
	 *
	 * @param name the resource name
	 * @return Input stream
	 */
	private static final InputStream stream(String name) {
		return OvercastHelper.class.getClassLoader().getResourceAsStream(name);
	}

	/**
	 * Read instance configuration map.
	 *
	 * @param name the instance name
	 * @return Configuration map
	 */
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

	/**
	 * Get host name exposed by the Overcast config for specific instance name.
	 *
	 * @param name the instance name
	 * @return Host name
	 */
	public static String getHostName(String name) {
		return mapping(name).get("host").toString();
	}

	/**
	 * Get port number exposed by the Overcast for specific instance name.
	 *
	 * @param name the instance name
	 * @param port the port that is mapped
	 * @return Port number
	 */
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