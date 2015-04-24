package com.github.sarxos.overcast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.impl.StaticLoggerBinder;

import com.jcabi.log.Logger;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.xebialabs.overcast.OvercastProperties;
import com.xebialabs.overcast.PropertiesLoader;
import com.xebialabs.overcast.host.CloudHost;


@ToString
@EqualsAndHashCode(callSuper = false)
public abstract class OvercastAbstractMojo extends AbstractMojo {

	private static final String CLOUDS_PROPERTY = "overcast.maven.plugin.cloud.mapping";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * The Maven project.
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private transient MavenProject project;

	/**
	 * Shall we skip execution?
	 */
	@Parameter(defaultValue = "false", required = false)
	private transient boolean skip;

	/**
	 * Overcast configuration file.
	 */
	@Parameter(defaultValue = "${project.basedir}/src/test/overcast/overcast.conf", required = true)
	private transient File conf;

	/**
	 * Helper file target.
	 */
	@Parameter(defaultValue = "${project.build.directory}/test-classes/overcast.ser", required = true)
	private transient File target;

	/**
	 * Instances to be run (names must be the same as in the overcast configuration file).
	 */
	@Parameter(readonly = true, required = true)
	private List<Instance> instances;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		StaticLoggerBinder
			.getSingleton()
			.setMavenLog(this.getLog());

		if (isSkip()) {
			Logger.info(this, "Execution skipped because of 'skip' option");
			return;
		}

		if (!conf.exists()) {
			throw new MojoExecutionException("Overcast configuration file does not exist: " + FilenameUtils.normalize(conf.getAbsolutePath()));
		}

		Properties properties = System.getProperties();

		String key = PropertiesLoader.OVERCAST_CONF_FILE_PROPERTY;
		String value = properties.getProperty(key);
		String path = conf.getAbsolutePath();

		Logger.debug(this, "Setting system property " + key + " to " + path);

		properties.setProperty(key, path);
		OvercastProperties.reloadOvercastProperties();

		Logger.debug(this, "Run MOJO " + getClass());

		try {
			run();
		} finally {
			if (value != null) {
				properties.setProperty(key, value);
			} else {
				properties.remove(key);
			}
		}
	}

	public abstract void run() throws MojoExecutionException, MojoFailureException;

	public boolean isSkip() {
		return skip;
	}

	public MavenProject getProject() {
		return project;
	}

	public Config getConfig() {
		return ConfigFactory.systemProperties()
			.withFallback(loadOvercastConfigFromFile(conf))
			.withFallback(loadOvercastConfigFromFile(new File("overcast.conf")))
			.resolve();
	}

	protected OvercastAbstractMojo setConfFile(File conf) {
		this.conf = conf;
		return this;
	}

	protected File getConfFile() {
		return conf;
	}

	private Config loadOvercastConfigFromFile(File file) {

		if (file == null) {
			Logger.warn(this, "File is null");
			return ConfigFactory.empty();
		}

		if (!file.exists()) {
			Logger.warn(this, "File " + file + " not found");
			return ConfigFactory.empty();
		}

		Logger.info(this, "Loading from file" + file.getAbsolutePath());
		return ConfigFactory.parseFile(file);
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public File getTarget() {
		return target;
	}

	@SuppressWarnings("unchecked")
	protected void store(List<Mapping> mappings) throws MojoExecutionException {

		Logger.debug(this, "Store cloud mappings: " + mappings);

		List<Map<?, ?>> trivialities = null;
		try {
			trivialities = MAPPER.readValue(MAPPER.writeValueAsString(mappings), ArrayList.class);
		} catch (IOException e) {
			throw new MojoExecutionException("Cannot reduce objects types by JSON reverse conversion", e);
		}

		File target = getTarget();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(target))) {
			oos.writeObject(trivialities);
		} catch (IOException e) {
			throw new MojoExecutionException("Cannot write object to file " + target, e);
		}

		setCloudMappings(mappings);
	}

	protected CloudHost find(String name) throws MojoExecutionException {

		Logger.debug(this, "Find mapping " + name);

		List<Mapping> mappings = getCloudMappings();
		if (mappings == null) {
			throw new MojoExecutionException("Cannot find cloud mappings, seems like setup goal has not ben executed");
		}

		for (Mapping mapping : mappings) {
			if (mapping.getName().equals(name)) {
				return mapping.getCloud();
			}
		}

		throw new MojoExecutionException("Cloud mapping for name '" + name + "' has not been found");
	}

	protected Mapping map(Instance instance, CloudHost cloud) {
		return new Mapping(instance, cloud);
	}

	private void setCloudMappings(List<Mapping> mappings) {
		getProject()
			.getProperties()
			.put(CLOUDS_PROPERTY, mappings);
	}

	@SuppressWarnings("unchecked")
	private List<Mapping> getCloudMappings() {
		return (List<Mapping>) getProject()
			.getProperties()
			.get(CLOUDS_PROPERTY);
	}
}
