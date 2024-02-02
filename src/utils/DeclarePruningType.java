package utils;

public enum DeclarePruningType {
	ALL_REDUCTIONS("All reductions"),
	HIERARCHY_BASED("Hierarchy-based"),
	TRANSITIVE_CLOSURE("Transitive Closure"),
	NONE("None");

	private String displayText;

	private DeclarePruningType(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayText() {
		return displayText;
	}

}
