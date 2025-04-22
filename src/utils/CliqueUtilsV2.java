package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.DiscoveredActivity;
import data.v1v2.ActivityRelationsContainer;

public class CliqueUtilsV2 { //V1 was implemented as part of creating the initial fragments

	//Private constructor to avoid unnecessary instantiation of the class
	private CliqueUtilsV2() {
	}
	
	public static Set<Set<DiscoveredActivity>> findCoExCliques(Set<DiscoveredActivity> candidateActivities, Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap) {
		Set<DiscoveredActivity> candidateSet = new HashSet<DiscoveredActivity>(candidateActivities); //Copying the set so that modifications would be local to this method (not necessary at the time of writing this comment)
		
		Set<Set<DiscoveredActivity>> coexCliques = new HashSet<Set<DiscoveredActivity>>();
		Iterator<DiscoveredActivity> candidateIt = candidateSet.iterator();
		
		while (candidateIt.hasNext()) {
			DiscoveredActivity currCandidate = candidateIt.next();
			Set<DiscoveredActivity> currClique = new HashSet<DiscoveredActivity>();
			currClique.add(currCandidate);
			
			for (DiscoveredActivity coexActivity : activityToRelationsMap.get(currCandidate).getCoexActivities()) {
				if (candidateSet.contains(coexActivity)) {
					currClique.add(coexActivity);
				}
			}
			
			coexCliques.add(currClique);
			candidateSet.removeAll(currClique);
		}
		return coexCliques;
	}
	


	//Result is placed into List<Set<DiscoveredActivity>> notcoCliques, which is assumed to be empty on initial method call
	public static void findNotCoCliques(List<DiscoveredActivity> potentialClique, List<DiscoveredActivity> candidates, List<DiscoveredActivity> alreadyFound, Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap, List<Set<DiscoveredActivity>> notcoCliques) {
		//Based on the following Bron�Kerbosch algorithm implementation: https://github.com/liziliao/Bron-Kerbosch/blob/master/Bron-Kerbosch.java

		List<DiscoveredActivity> candidatesArray = new ArrayList<DiscoveredActivity>(candidates);
		if (!end(candidates, alreadyFound, activityToRelationsMap)) {
			for (DiscoveredActivity candidate : candidatesArray) {
				List<DiscoveredActivity> newCandidates  = new ArrayList<DiscoveredActivity>();
				List<DiscoveredActivity> newAlreadyFound  = new ArrayList<DiscoveredActivity>();

				potentialClique.add(candidate);
				candidates.remove(candidate);

				for (DiscoveredActivity newCandidate : candidates) {
					if (isNotcoNeighbor(candidate, newCandidate, activityToRelationsMap)) {
						newCandidates.add(newCandidate);
					}
				}

				for (DiscoveredActivity newFound : alreadyFound) {
					if (isNotcoNeighbor(candidate, newFound, activityToRelationsMap)) {
						newAlreadyFound.add(newFound);
					}
				}

				if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
					notcoCliques.add(new HashSet<DiscoveredActivity>(potentialClique));
				} else {
					findNotCoCliques(potentialClique, newCandidates, newAlreadyFound, activityToRelationsMap, notcoCliques);
				}

				alreadyFound.add(candidate);
				potentialClique.remove(candidate);
			}
		}
	}

	private static boolean end(List<DiscoveredActivity> candidates, List<DiscoveredActivity> alreadyFound, Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap) {
		boolean end = false;
		int edgecounter;

		for (DiscoveredActivity found : alreadyFound) {
			edgecounter = 0;
			for (DiscoveredActivity candidate : candidates) {
				if (isNotcoNeighbor(found, candidate, activityToRelationsMap)) {
					edgecounter++;
				}
			}
			if (edgecounter == candidates.size()) {
				end = true;
			}
		}
		return end;
	}

	private static boolean isNotcoNeighbor(DiscoveredActivity activityA, DiscoveredActivity activityB, Map<DiscoveredActivity, ActivityRelationsContainer> activityToRelationsMap) {
		return activityToRelationsMap.get(activityA).getNotCoexActivities().contains(activityB);
	}
}
