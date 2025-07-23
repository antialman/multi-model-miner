package task.v4;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import model.v4.HybridModelSet;
import model.v4.PnContainer;
import task.DeclareDiscoveryResult;
import utils.ConstraintTemplate;

public class SeqFlowTask extends Task<HybridModelSet> {
	//Creates all sequence flows of of length>2 that exist between gateways
	//Intended to be the first step of processing the discovered constraints
	
	private DeclareDiscoveryResult declareDiscoveryResult;

	public void setDeclareDiscoveryResult(DeclareDiscoveryResult declareDiscoveryResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
	}
	
	
	@Override
	protected HybridModelSet call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println(this.getClass().getSimpleName() + " started at: " + taskStartTime);
			
			
			//Each key has an incoming Alternate Succession from the activities in the corresponding list
			Map<DiscoveredActivity, Set<DiscoveredActivity>> altSucInMap = new HashMap<DiscoveredActivity, Set<DiscoveredActivity>>();
			//Each key has an outgoing Alternate Succession to the activities in the corresponding list 
			Map<DiscoveredActivity, Set<DiscoveredActivity>> altSucOutMap = new HashMap<DiscoveredActivity, Set<DiscoveredActivity>>();
			
			//For tracking the Alternate Succession constraints that have not been added to any pnContainer
			Set<DiscoveredConstraint> remainingSeqFlowConstraints = new HashSet<DiscoveredConstraint>();
			//Constraints not relevant for sequence flows
			Set<DiscoveredConstraint> otherConstraints = new HashSet<DiscoveredConstraint>();
			
			//Filling the above data structures
			declareDiscoveryResult.getActivities().forEach(activity -> {
				altSucInMap.put(activity, new HashSet<DiscoveredActivity>());
				altSucOutMap.put(activity, new HashSet<DiscoveredActivity>());
			});
			for (DiscoveredConstraint constraint : declareDiscoveryResult.getConstraints()) {
				if (constraint.getTemplate() == ConstraintTemplate.Alternate_Succession) {
					remainingSeqFlowConstraints.add(constraint);
					altSucOutMap.get(constraint.getActivationActivity()).add(constraint.getTargetActivity());
					altSucInMap.get(constraint.getTargetActivity()).add(constraint.getActivationActivity());
				} else if (constraint.getTemplate() == ConstraintTemplate.Not_Chain_Succession) {
					remainingSeqFlowConstraints.add(constraint);
				} else {
					otherConstraints.add(constraint);
				}
			}
			
			
			//Creating sequence flow nets
			Set<PnContainer> seqFlowPnContainers = new HashSet<PnContainer>();
			Set<DiscoveredActivity> processedSeqFlowActivities = new HashSet<DiscoveredActivity>();
			
			DiscoveredActivity prevSeqFlowActivity = findStartActivity(altSucInMap, processedSeqFlowActivities);
			while (prevSeqFlowActivity != null) { //Loop over all activities that can be used to start a sequence flow net  
				PnContainer pnContainer = new PnContainer("seqFlow_"+seqFlowPnContainers.size() , prevSeqFlowActivity);
				processedSeqFlowActivities.add(prevSeqFlowActivity);
				
				DiscoveredActivity nextSeqFlowActivity = findNextSeqFlowActivity(prevSeqFlowActivity, pnContainer, altSucInMap, altSucOutMap);
				while (nextSeqFlowActivity != null) { //Loop over all activities that can be added to the current sequence flow net 
					
					Set<DiscoveredConstraint> matchingSeqFlowConstraints = findMatchingSeqFlowConstraints(pnContainer, prevSeqFlowActivity, nextSeqFlowActivity, remainingSeqFlowConstraints);
					pnContainer.addSeqFlow(prevSeqFlowActivity, nextSeqFlowActivity, matchingSeqFlowConstraints);
					processedSeqFlowActivities.add(nextSeqFlowActivity);
					remainingSeqFlowConstraints.removeAll(matchingSeqFlowConstraints);
					
					//System.out.println("Added " + prevSeqFlowActivity + " -> " + nextSeqFlowActivity);
					//matchingSeqFlowConstraints.forEach(c -> System.out.println("\t" + c));
					//System.out.println();
					
					prevSeqFlowActivity = nextSeqFlowActivity;
					nextSeqFlowActivity = findNextSeqFlowActivity(nextSeqFlowActivity, pnContainer, altSucInMap, altSucOutMap);
				}
				
				if (pnContainer.getActivities().size()>1) { //Discarding sequence flow nets that would contain only a single activity
					seqFlowPnContainers.add(pnContainer);
				}
				
				prevSeqFlowActivity = findStartActivity(altSucInMap, processedSeqFlowActivities);
			}
			
			
			//For checking the results
			for (PnContainer seqFlowPnContainer : seqFlowPnContainers) {
				System.out.println(seqFlowPnContainer.getPetrinet().getTransitions());
				seqFlowPnContainer.getMatchingConstraints().forEach(c -> System.out.println("\t" + c));
			}
			
			
			Set<DiscoveredConstraint> allRemainingConstraints = new HashSet<DiscoveredConstraint>(otherConstraints);
			allRemainingConstraints.addAll(remainingSeqFlowConstraints);
			HybridModelSet seqFlowModelSet = new HybridModelSet(seqFlowPnContainers, allRemainingConstraints);
			
			System.out.println(this.getClass().getSimpleName() + " finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));

			return seqFlowModelSet;
		} catch (Exception e) {
			System.err.println(this.getClass().getSimpleName() + " failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}


	//Finds all constraints that would match the given sequence flow extension
	private Set<DiscoveredConstraint> findMatchingSeqFlowConstraints(PnContainer pnContainer, DiscoveredActivity prevSeqFlowActivity, DiscoveredActivity nextSeqFlowActivity, Set<DiscoveredConstraint> remainingSeqFlowConstraints) {
		Set<DiscoveredConstraint> matchingSeqFlowConstraints = new HashSet<DiscoveredConstraint>();
		
		for (DiscoveredConstraint constraint : remainingSeqFlowConstraints) {
			if (constraint.getTargetActivity() == nextSeqFlowActivity && pnContainer.getActivities().contains(constraint.getActivationActivity())) {
				if (constraint.getTemplate() == ConstraintTemplate.Alternate_Succession) { //Extension of a sequence must have incoming Alternate Succession from all activities in that sequence  
					matchingSeqFlowConstraints.add(constraint);
				} else if (constraint.getTemplate() == ConstraintTemplate.Not_Chain_Succession && constraint.getActivationActivity() != prevSeqFlowActivity) {  //Extension of a sequence must have incoming Not Chain Succession from all activities preceding the last activity in that sequence
					matchingSeqFlowConstraints.add(constraint);
				}
			}
		}
		
		return matchingSeqFlowConstraints;
	}



	//Finds the first available activity for starting a sequence flow
	private DiscoveredActivity findStartActivity(Map<DiscoveredActivity, Set<DiscoveredActivity>> altSucInMap, Set<DiscoveredActivity> processedSeqFlowActivities) {
		for (DiscoveredActivity candidateActivity : altSucInMap.keySet()) {
			if ((altSucInMap.get(candidateActivity).isEmpty() || processedSeqFlowActivities.containsAll(altSucInMap.get(candidateActivity))) && !processedSeqFlowActivities.contains(candidateActivity)) {
				return candidateActivity;
			}
		}
		return null;
	}
	
	//Finds the next activity in a sequence flow (stops at AND-split and AND-join)
	private DiscoveredActivity findNextSeqFlowActivity(DiscoveredActivity prevActivity, PnContainer pnContainer, Map<DiscoveredActivity, Set<DiscoveredActivity>> altSucInMap, Map<DiscoveredActivity, Set<DiscoveredActivity>> altSucOutMap) {
		Set<DiscoveredActivity> candidateActivities =  new HashSet<DiscoveredActivity>(altSucOutMap.get(prevActivity));
		
		//Sequence can only be extended with activities that have incoming Alternate Succession constraints from all activities already in the sequence
		Iterator<DiscoveredActivity> candidateIterator = candidateActivities.iterator();
		while (candidateIterator.hasNext()) {
			DiscoveredActivity candidateActivity = (DiscoveredActivity) candidateIterator.next();
			if (!altSucInMap.get(candidateActivity).containsAll(pnContainer.getActivities())) {
				candidateIterator.remove();
			}
		}
		//Return null if no candidates remain
		if (candidateActivities.isEmpty()) {
			return null;
		}
		
		
		//Sequence can only be extended with activities that do not have incoming Alternate Succession constraints form other candidates
		Set<DiscoveredActivity> invalidCandidates =  new HashSet<DiscoveredActivity>();
		for (DiscoveredActivity candidateActivity1 : candidateActivities) {
			for (DiscoveredActivity candidateActivity2 : candidateActivities) {
				if (altSucInMap.get(candidateActivity1).contains(candidateActivity2)) {
					invalidCandidates.add(candidateActivity1);
				}
			}
		}
		candidateActivities.removeAll(invalidCandidates);
		//Return null if no candidates or more than 2 candidates remain. Latter means  there is an AND-split, which will be handled later.
		if (candidateActivities.isEmpty() || candidateActivities.size()>1) {
			return null;
		}
		
		
		//Sequence can only be extended with activities 
		DiscoveredActivity candidateActivity = candidateActivities.iterator().next();
		Set<DiscoveredActivity> altSucInActivities = altSucInMap.get(candidateActivity);
		DiscoveredActivity closestAltSucInActivity = null;
		for (DiscoveredActivity altInSucActivity1 : altSucInActivities) {
			boolean isLast = true;
			for (DiscoveredActivity altInSucActivity2 : altSucInActivities) {
				if (altSucOutMap.get(altInSucActivity1).contains(altInSucActivity2)) {
					isLast = false;
				}
			}
			if (isLast) {
				if (closestAltSucInActivity == null) {
					closestAltSucInActivity = altInSucActivity1; //This should also match the last activity currently in the pnContainer
				} else {
					return null;
				}
			} 
		}
		
		
		//Should reach here only if there is a valid candidate for extending the sequence in the given pnContainer
		return candidateActivity;
	}

}
