package data;

import java.time.Duration;

import utils.ConstraintTemplate;

public class DiscoveredConstraint {

	private ConstraintTemplate template;
	private DiscoveredActivity activationActivity;
	private DiscoveredActivity targetActivity;
	private float constraintSupport;
	private Duration minTD, avgTD, maxTD;

	public DiscoveredConstraint(ConstraintTemplate template, DiscoveredActivity activationActivity, DiscoveredActivity targetActivity, float constraintSupport) {
		this.template = template;
		this.activationActivity = activationActivity;
		this.targetActivity = targetActivity;
		this.constraintSupport = constraintSupport;
	}

	public ConstraintTemplate getTemplate() {
		return template;
	}

	public DiscoveredActivity getActivationActivity() {
		return activationActivity;
	}

	public DiscoveredActivity getTargetActivity() {
		return targetActivity;
	}

	public float getConstraintSupport() {
		return constraintSupport;
	}
	
	public Duration getMinTD() {
		return minTD;
	}
	
	public void setMinTD(Duration minTD) {
		this.minTD = minTD;
	}

	public Duration getAvgTD() {
		return avgTD;
	}
	
	public void setAvgTD(Duration avgTD) {
		this.avgTD = avgTD;
	}
	
	public Duration getMaxTD() {
		return maxTD;
	}
	
	public void setMaxTD(Duration maxTD) {
		this.maxTD = maxTD;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activationActivity == null) ? 0 : activationActivity.hashCode());
		result = prime * result + ((targetActivity == null) ? 0 : targetActivity.hashCode());
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscoveredConstraint other = (DiscoveredConstraint) obj;
		if (activationActivity == null) {
			if (other.activationActivity != null)
				return false;
		} else if (!activationActivity.equals(other.activationActivity))
			return false;
		if (targetActivity == null) {
			if (other.targetActivity != null)
				return false;
		} else if (!targetActivity.equals(other.targetActivity))
			return false;
		if (template != other.template)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String output = "Constraint: \"" + template + "(" + activationActivity + ", " + targetActivity + ") - supp=" + constraintSupport;
		
		if (minTD != null && avgTD != null && maxTD != null)
			output += ", TDs [min=" + minTD + ", avg=" + avgTD + ", max=" + maxTD + "]";

		return output;
	}

}
