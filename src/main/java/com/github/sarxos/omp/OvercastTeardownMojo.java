package com.github.sarxos.omp;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.jcabi.log.Logger;
import com.typesafe.config.Config;
import com.xebialabs.overcast.host.CloudHost;


@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(name = "teardown", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class OvercastTeardownMojo extends OvercastAbstractMojo {

	private CloudHost teardown(String name) throws MojoExecutionException {
		Logger.info(this, "Tearing down instance " + name);
		CloudHost cloud = find(name);
		cloud.teardown();
		return cloud;
	}

	@Override
	public void run() throws MojoExecutionException, MojoFailureException {

		Config config = getConfig();

		for (Instance instance : getInstances()) {

			String name = instance.getName();
			if (!config.hasPath(name)) {
				throw new MojoExecutionException("Overcast configuration file " + getConfFile().getAbsolutePath() + " does not define '" + name + "' instance");
			}

			teardown(name);
		}
	}
}
