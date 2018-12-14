package de.pellepelster.ant.statistics;

import java.util.Random;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class RandomTask extends Task {

	private String min;

	private String max;
	private String property;
	private Random random = new Random(System.currentTimeMillis());

	protected int calculateRandom(int minInt, int maxInt) {
		return minInt + random.nextInt(maxInt - minInt + 1);
	}

	@Override
	public void execute() throws BuildException {
		if (min == null || min.equals("")) {
			throw new BuildException("Min property not specified");
		}

		if (max == null || max.equals("")) {
			throw new BuildException("Max property not specified");
		}

		int minInt = Integer.parseInt(min);
		int maxInt = Integer.parseInt(max);

		if (minInt > maxInt) {
			throw new BuildException("Min is bigger than max");
		}

		int randomInt = calculateRandom(minInt, maxInt);

		getProject().setNewProperty(property, String.valueOf(randomInt));
	}

	public String getMax() {
		return max;
	}

	public String getMin() {
		return min;
	}

	public String getProperty() {
		return property;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public void setProperty(String property) {
		this.property = property;
	}

}
