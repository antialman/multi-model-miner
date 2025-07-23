package data.v4;

import javafx.beans.property.SimpleBooleanProperty;
import model.v4.PnContainer;

public class ModelSelector {
	private PnContainer pnContainer;
	private SimpleBooleanProperty isSelectedProperty = new SimpleBooleanProperty();
	
	public ModelSelector(PnContainer pnContainer, boolean isSelected) {
		this.pnContainer = pnContainer;
		this.isSelectedProperty.set(isSelected);
	}
	
	public PnContainer getPnContainer() {
		return pnContainer;
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
		result = prime * result + ((pnContainer == null) ? 0 : pnContainer.hashCode());
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
		ModelSelector other = (ModelSelector) obj;
		if (!pnContainer.equals(other.pnContainer))
			return false;
		return true;
	}
}
