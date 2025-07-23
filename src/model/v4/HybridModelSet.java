package model.v4;

import java.util.HashSet;
import java.util.Set;

import data.DiscoveredConstraint;

public class HybridModelSet {

	//Procedural components
	private Set<PnContainer> pnContainers = new HashSet<PnContainer>();
	//Constraints not embedded in the procedural components
	private Set<DiscoveredConstraint> remainingConstraints = new HashSet<DiscoveredConstraint>();
	
	public HybridModelSet(Set<PnContainer> initialPnContainers, Set<DiscoveredConstraint> initialRemainingConstraints) {
		this.pnContainers = initialPnContainers;
		this.remainingConstraints = initialRemainingConstraints;
	}
	
	public Set<PnContainer> getPnContainers() {
		return pnContainers;
	}
	
	public Set<DiscoveredConstraint> getRemainingConstraints() {
		return remainingConstraints;
	}
	
}
