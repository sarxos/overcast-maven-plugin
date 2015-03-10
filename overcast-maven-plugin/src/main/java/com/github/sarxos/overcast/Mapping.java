package com.github.sarxos.overcast;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.xebialabs.overcast.host.CloudHost;


public class Mapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	private final String host;

	private final Map<Integer, Integer> ports;

	private transient final CloudHost cloud;

	public Mapping(Instance instance, CloudHost cloud) {

		String host = cloud.getHostName();
		if (host == null) {
			host = "localhost";
		}

		Map<Integer, Integer> ports = new HashMap<>();
		for (Integer port : instance.getPorts()) {
			ports.put(port, cloud.getPort(port));
		}

		this.name = instance.getName();
		this.host = host;
		this.ports = ports;
		this.cloud = cloud;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public Map<Integer, Integer> getPorts() {
		return ports;
	}

	public CloudHost getCloud() {
		return cloud;
	}
}
