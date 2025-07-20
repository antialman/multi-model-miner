package data.v4;

import data.DiscoveredActivity;
import javafx.beans.property.SimpleBooleanProperty;

public class ActivitySelector {
	private DiscoveredActivity discoveredActivity;
	private SimpleBooleanProperty isSelectedProperty = new SimpleBooleanProperty();
	
	public ActivitySelector(DiscoveredActivity discoveredActivity, boolean isSelected) {
		this.discoveredActivity = discoveredActivity;
		this.isSelectedProperty.set(isSelected);
	}
	
	public DiscoveredActivity getDiscoveredActivity() {
		return discoveredActivity;
	}
	
	public SimpleBooleanProperty isSelectedProperty() {
		return this.isSelectedProperty;
	}
	public boolean getIsSelected() {
		return isSelectedProperty.get();
	}
	public void setIsSelected(boolean isSelected) {
		this.isSelectedProperty.set(isSelected);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((discoveredActivity == null) ? 0 : discoveredActivity.hashCode());
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
		ActivitySelector other = (ActivitySelector) obj;
		if (!discoveredActivity.equals(other.discoveredActivity))
			return false;
		return true;
	}
}
