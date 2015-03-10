package com.github.sarxos.overcast;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.sarxos.overcast.OvercastSetupMojo;
import com.typesafe.config.Config;


public class OvercastMojoTest {

	private static final File TEST_CONF = new File("src/test/resources/test.conf");

	@Test
	public void test_configNotEmpty() {

		Config config = new OvercastSetupMojo()
			.setConfFile(TEST_CONF)
			.getConfig();

		Assertions
			.assertThat(config.isResolved())
			.isTrue();

		Assertions
			.assertThat(config.isEmpty())
			.isFalse();
	}

	@Test
	public void test_configValues() {

		Config config = new OvercastSetupMojo()
			.setConfFile(TEST_CONF)
			.getConfig();

		Assertions
			.assertThat(config.getInt("test.x"))
			.isNotNull()
			.isEqualTo(1);

		Assertions
			.assertThat(config.getInt("test.y"))
			.isNotNull()
			.isEqualTo(2);

		Assertions
			.assertThat(config.getString("test.z"))
			.isNotNull()
			.isEqualTo("abc");

	}
}
