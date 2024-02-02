package data;

import java.util.Set;

public class DiscoveredActivity {

	private String activityName;
	private String activityTransition;
	private float activitySupport;
	private Set<String> payloadAttributes;
	
	public DiscoveredActivity(String activityName, float activitySupport) {
		this.activityName = activityName;
		this.activityTransition = null;
		this.activitySupport = activitySupport;
	}
	
	public DiscoveredActivity(String activityName, String activityTransition, float activitySupport) {
		this.activityName = activityName;
		this.activityTransition = activityTransition;
		this.activitySupport = activitySupport;
	}

	public String getActivityFullName() {
		return activityTransition==null ? activityName : activityName + "-" + activityTransition;
	}
	
	public String getActivityName() {
		return activityName;
	}
	
	public String getActivityTransition() {
		return activityTransition;
	}

	public float getActivitySupport() {
		return activitySupport;
	}

	public Set<String> getPayloadAttributes() {
		return payloadAttributes;
	}

	public void setPayloadAttributes(Set<String> payloadAttributes) {
		this.payloadAttributes = payloadAttributes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activityName == null) ? 0 : activityName.hashCode());
		result = prime * result + ((activityTransition == null) ? 0 : activityTransition.hashCode());
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
		DiscoveredActivity other = (DiscoveredActivity) obj;
		if (activityName == null) {
			if (other.activityName != null)
				return false;
		} else if (!activityName.equals(other.activityName))
			return false;
		if (activityTransition == null) {
			if (other.activityTransition != null)
				return false;
		} else if (!activityTransition.equals(other.activityTransition))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Activity: \"" + getActivityFullName() + "\" (supp=" + activitySupport + ")";
	}
}
