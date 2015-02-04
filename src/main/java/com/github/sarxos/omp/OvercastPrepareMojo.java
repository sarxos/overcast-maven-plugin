package com.github.sarxos.omp;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(name = "prepare", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class OvercastPrepareMojo {

}
