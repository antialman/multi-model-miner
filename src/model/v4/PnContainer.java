package model.v4;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class PnContainer {
	
	private Petrinet petrinet;
	private Map<DiscoveredActivity, Transition> activityToTransitionMap = new HashMap<DiscoveredActivity, Transition>();
	private Set<DiscoveredConstraint> matchingConstraints = new HashSet<DiscoveredConstraint>();
	private DiscoveredActivity startActivity;
	
	public PnContainer(String pnName, DiscoveredActivity startActivity) {
		this.petrinet = new PetrinetImpl(pnName);
		this.startActivity = startActivity;
		Place p = petrinet.addPlace(String.valueOf("p" + petrinet.getPlaces().size()));
		Transition t = petrinet.addTransition(startActivity.getActivityName());
		activityToTransitionMap.put(startActivity, t);
		petrinet.addArc(p, t);
	}
	
	public Petrinet getPetrinet() {
		return petrinet;
	}
	
	public DiscoveredActivity getStartActivity() {
		return startActivity;
	}
	
	public Set<DiscoveredActivity> getActivities() {
		return activityToTransitionMap.keySet();
	}
	
	public Set<DiscoveredConstraint> getMatchingConstraints() {
		return matchingConstraints;
	}

	public void addSeqFlow(DiscoveredActivity prevSeqFlowActivity, DiscoveredActivity nextSeqFlowActivity, Set<DiscoveredConstraint> seqFlowConstraints) {
		Place p = petrinet.addPlace(String.valueOf("p" + petrinet.getPlaces().size()));
		Transition t = petrinet.addTransition(nextSeqFlowActivity.getActivityName());
		activityToTransitionMap.put(nextSeqFlowActivity, t);
		petrinet.addArc(activityToTransitionMap.get(prevSeqFlowActivity), p);
		petrinet.addArc(p, t);
		matchingConstraints.addAll(seqFlowConstraints);
	}
	

	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((petrinet == null) ? 0 : petrinet.getLabel().hashCode()); //Assuming net labels are unique and no net is placed in two containers
		return result;
	}
}
