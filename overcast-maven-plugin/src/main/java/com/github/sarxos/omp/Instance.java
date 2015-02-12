package com.github.sarxos.omp;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.jcabi.aspects.Loggable;


@ToString
@EqualsAndHashCode(of = "name")
@Loggable(Loggable.INFO)
public class Instance {

	private String name;

	private List<Integer> ports;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getPorts() {
		return ports;
	}

	public void setPorts(List<Integer> ports) {
		this.ports = ports;
	}
}
