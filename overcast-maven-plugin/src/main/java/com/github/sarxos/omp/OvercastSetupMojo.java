package com.github.sarxos.omp;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.jcabi.log.Logger;
import com.typesafe.config.Config;
import com.xebialabs.overcast.host.CloudHost;
import com.xebialabs.overcast.host.CloudHostFactory;


@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(name = "setup", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class OvercastSetupMojo extends OvercastAbstractMojo {

	protected CloudHost setup(String name) {
		Logger.debug(this, "Setting up instance " + name);
		CloudHost cloud = CloudHostFactory.getCloudHost(name);
		cloud.setup();
		return cloud;
	}

	@Override
	public void run() throws MojoExecutionException, MojoFailureException {

		Config config = getConfig();

		List<Mapping> mappings = new ArrayList<>();

		for (Instance instance : getInstances()) {

			String name = instance.getName();
			if (!config.hasPath(name)) {
				throw new MojoExecutionException("Overcast configuration file " + getConfFile().getAbsolutePath() + " does not define '" + name + "' instance");
			}

			CloudHost cloud = setup(name);
			Mapping mapping = map(instance, cloud);

			mappings.add(mapping);
		}

		store(mappings);
	}
}
