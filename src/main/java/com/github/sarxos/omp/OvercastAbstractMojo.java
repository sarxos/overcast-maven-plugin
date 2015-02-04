package com.github.sarxos.omp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.impl.StaticLoggerBinder;

import com.jcabi.log.Logger;


public abstract class OvercastAbstractMojo extends AbstractMojo implements Runnable {

	private static final String PROPERTY_OVERCAST_RUNNING = "overcast.plugin.running";

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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());

		if (skip) {
			Logger.info(this, "Execution skipped because of 'skip' option");
			return;
		}

		run();
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}
}
