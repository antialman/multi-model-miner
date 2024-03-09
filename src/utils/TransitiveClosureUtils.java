package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import data.DiscoveredConstraint;

public class TransitiveClosureUtils {

	//Private constructor to avoid unnecessary instantiation of the class
	private TransitiveClosureUtils() {
	}



	public static List<DiscoveredConstraint> getPrunedConstraints(List<DiscoveredConstraint> discoveredConstraints) {

		//For reusing Declare miner code as-is
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();

		for (int i = 0; i < discoveredConstraints.size(); i++) {
			//Keys are offset by -1 compared to original implementation, but that should not matter
			if (!discoveredConstraints.get(i).getTemplate().getIsBinary()) {
				constraintParametersMap.put(i, Arrays.asList(discoveredConstraints.get(i).getActivationActivity().getActivityName()));
			} else if (discoveredConstraints.get(i).getTemplate().getReverseActivationTarget()) {
				constraintParametersMap.put(i, Arrays.asList(discoveredConstraints.get(i).getTargetActivity().getActivityName(),discoveredConstraints.get(i).getActivationActivity().getActivityName()));
			} else {
				constraintParametersMap.put(i, Arrays.asList(discoveredConstraints.get(i).getActivationActivity().getActivityName(),discoveredConstraints.get(i).getTargetActivity().getActivityName()));
			}
			constraintTemplateMap.put(i, TemplateUtils.getDeclareTemplate(discoveredConstraints.get(i).getTemplate()));
		}



		Vector<Integer> transitiveClosureSuccessionConstraints = new Vector<Integer>(); 
		Vector<Integer> transitiveClosureCoexistenceConstraints = new Vector<Integer>();
		Vector<Integer> transitiveClosureResponseConstraints = new Vector<Integer>();
		Vector<Integer> transitiveClosurePrecedenceConstraints = new Vector<Integer>();
		Vector<Integer> transitiveClosureNotCoexistenceConstraints = new Vector<Integer>();

		populateTransitiveClosureSuccessionConstraints(transitiveClosureSuccessionConstraints, constraintParametersMap, constraintTemplateMap);
		populateTransitiveClosureCoexistenceConstraints(transitiveClosureCoexistenceConstraints, transitiveClosureSuccessionConstraints, constraintParametersMap, constraintTemplateMap);
		populateTransitiveClosureResponseConstraints(transitiveClosureResponseConstraints, transitiveClosureSuccessionConstraints, constraintParametersMap, constraintTemplateMap);
		populateTransitiveClosurePrecedenceConstraints(transitiveClosurePrecedenceConstraints, transitiveClosureSuccessionConstraints, constraintParametersMap, constraintTemplateMap);
		populateTransitiveClosureNotCoexistenceConstraints(transitiveClosureNotCoexistenceConstraints, transitiveClosureSuccessionConstraints, transitiveClosureCoexistenceConstraints, constraintParametersMap, constraintTemplateMap);


		List<DiscoveredConstraint> prunedConstraints = new ArrayList<DiscoveredConstraint>();
		List<DiscoveredConstraint> prunedSuccessions = new ArrayList<DiscoveredConstraint>();
		for (int i = 0; i < discoveredConstraints.size(); i++) {
			if (!transitiveClosureSuccessionConstraints.contains(i)
					&& !transitiveClosureCoexistenceConstraints.contains(i)
					&& !transitiveClosureResponseConstraints.contains(i)
					&& !transitiveClosurePrecedenceConstraints.contains(i)
					&& !transitiveClosureNotCoexistenceConstraints.contains(i)) {
				prunedConstraints.add(discoveredConstraints.get(i));
				if (discoveredConstraints.get(i).getTemplate() == ConstraintTemplate.Succession) {
					prunedSuccessions.add(discoveredConstraints.get(i)); //For basic hierarchy-based pruning
				}
			}
		}
		
		
		//Basic hierarchy-based pruning (not the most efficient)
		for(Iterator<DiscoveredConstraint> it_d = prunedConstraints.iterator(); it_d.hasNext();) {
			DiscoveredConstraint discoveredConstraint = it_d.next();
			if (discoveredConstraint.getTemplate() == ConstraintTemplate.Response || discoveredConstraint.getTemplate() == ConstraintTemplate.CoExistence) {
				for(Iterator<DiscoveredConstraint> it_s = prunedSuccessions.iterator(); it_s.hasNext();) {
					DiscoveredConstraint successionConstraint = it_s.next();
					if (discoveredConstraint.getActivationActivity() == successionConstraint.getActivationActivity() && discoveredConstraint.getTargetActivity() == successionConstraint.getTargetActivity()) {
						it_d.remove();
						break;
					}
				}
			} else if (discoveredConstraint.getTemplate() == ConstraintTemplate.Precedence) {
				for(Iterator<DiscoveredConstraint> it_s = prunedSuccessions.iterator(); it_s.hasNext();) {
					DiscoveredConstraint successionConstraint = it_s.next();
					if (discoveredConstraint.getTargetActivity() == successionConstraint.getActivationActivity() && discoveredConstraint.getActivationActivity() == successionConstraint.getTargetActivity()) {
						it_d.remove();
						break;
					}
				}
			}
		}
		return prunedConstraints;
	}




	//Code taken from Declare miner, changes limited to method name and parameters
	public static void populateTransitiveClosureSuccessionConstraints(Vector<Integer> transitiveClosureSuccessionConstraints, HashMap<Integer, List<String>> constraintParametersMap,  HashMap<Integer, DeclareTemplate> constraintTemplateMap) {

		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession))){

				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//			HashSet<String> bluesA = null;
				//			HashSet<String> redsB = null;
				//			HashSet<String> bluesB = null;
				//			HashSet<String> redsA = null;
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				//			d.addEdge(constraintParametersMap.get(cd.getId()).get(0), constraintParametersMap.get(cd.getId()).get(1));
				//		}

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				if(!d.containsEdge(a, b)&&constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)){
					transitiveClosureSuccessionConstraints.add(id);
				}
			}
		}

	}


	//Code taken from Declare miner, changes limited to method name and parameters
	public static void populateTransitiveClosureCoexistenceConstraints(Vector<Integer> transitiveClosureCoexistenceConstraints, Vector<Integer> transitiveClosureSuccessionConstraints, HashMap<Integer, List<String>> constraintParametersMap,  HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		String a = null;
		String b = null;
		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)&& !transitiveClosureCoexistenceConstraints.contains(id)) || constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id)){
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
				a = constraintParametersMap.get(id).get(1);
				b = constraintParametersMap.get(id).get(0);

				descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		//Vector<String> added = new Vector<String>();
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)){
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);
				if(!d.containsEdge(a, b) &&constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)){
					transitiveClosureCoexistenceConstraints.add(id);
				}
			}
		}
		//	transitiveClosureCoexistenceConstraints = new Vector<Integer>();
	}


	//Code taken from Declare miner, changes limited to method name and parameters
	public static void populateTransitiveClosureResponseConstraints(Vector<Integer> transitiveClosureResponseConstraints, Vector<Integer> transitiveClosureSuccessionConstraints, HashMap<Integer, List<String>> constraintParametersMap,  HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id))){

				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//			HashSet<String> bluesA = null;
				//			HashSet<String> redsB = null;
				//			HashSet<String> bluesB = null;
				//			HashSet<String> redsA = null;
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				//			d.addEdge(constraintParametersMap.get(cd.getId()).get(0), constraintParametersMap.get(cd.getId()).get(1));
				//		}

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Response)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				if(!d.containsEdge(a, b) &&constraintTemplateMap.get(id).equals(DeclareTemplate.Response)){
					transitiveClosureResponseConstraints.add(id);
				}
			}
		}

	}


	//Code taken from Declare miner, changes limited to method name and parameters
	public static void populateTransitiveClosurePrecedenceConstraints(Vector<Integer> transitiveClosurePrecedenceConstraints, Vector<Integer> transitiveClosureSuccessionConstraints, HashMap<Integer, List<String>> constraintParametersMap,  HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))  || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id))){

				String a = constraintParametersMap.get(id).get(1);
				String b = constraintParametersMap.get(id).get(0);
				//			HashSet<String> bluesA = null;
				//			HashSet<String> redsB = null;
				//			HashSet<String> bluesB = null;
				//			HashSet<String> redsA = null;
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				//			d.addEdge(constraintParametersMap.get(cd.getId()).get(0), constraintParametersMap.get(cd.getId()).get(1));
				//		}

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)){
				String a = constraintParametersMap.get(id).get(1);
				String b = constraintParametersMap.get(id).get(0);
				if(!d.containsEdge(a, b) &&constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)){
					transitiveClosurePrecedenceConstraints.add(id);
				}
			}
		}

	}


	//Code taken from Declare miner, changes limited to method name and parameters
	public static void populateTransitiveClosureNotCoexistenceConstraints(Vector<Integer> transitiveClosureNotCoexistenceConstraints, Vector<Integer> transitiveClosureSuccessionConstraints, Vector<Integer> transitiveClosureCoexistenceConstraints, HashMap<Integer, List<String>> constraintParametersMap,  HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		//	HashMap<String,ArrayList<String>> negativeConnections = new HashMap<String, ArrayList<String>>();

		DefaultDirectedGraph<String, DefaultEdge> coexistenceDiagram = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		DefaultDirectedGraph<String, DefaultEdge> notCoexistenceDiagram = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};

		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence))){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//	System.out.println(d.toString());
				notCoexistenceDiagram.addVertex(a);
				notCoexistenceDiagram.addVertex(b);
				//		if(DijkstraShortestPath.findPathBetween(notCoexistenceDiagram, a, b)==null){
				DefaultEdge de =ef.createEdge(a, b);
				notCoexistenceDiagram.addEdge(a,b,de);

				DefaultEdge ed =ef.createEdge(b, a);
				notCoexistenceDiagram.addEdge(b,a,ed);

				//				d.addEdge(a, b);
				//		}			
			}


		}


		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || 
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) || 
					(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)&& !transitiveClosureCoexistenceConstraints.contains(id)) || 
					constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//	System.out.println(d.toString());
				coexistenceDiagram.addVertex(a);
				coexistenceDiagram.addVertex(b);
				//	if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, a, b)==null){
				DefaultEdge de =ef.createEdge(a, b);
				coexistenceDiagram.addEdge(a,b,de);

				DefaultEdge ed =ef.createEdge(b, a);
				coexistenceDiagram.addEdge(b,a,ed);

				//				d.addEdge(a, b);
				//	}			
			}


		}
		HashMap<String, ArrayList<String>> coexistencePaths = new HashMap<String, ArrayList<String>>();
		ArrayList<ArrayList<String>> alreadyRemoved = new ArrayList<ArrayList<String>>();
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				coexistenceDiagram.addVertex(a);

				ArrayList<String> coexistencePathsFromA = coexistencePaths.get(a);
				if(!coexistencePaths.containsKey(a)){
					coexistencePathsFromA = new ArrayList<String>();
					coexistencePathsFromA.add(a);
					for(String node : coexistenceDiagram.vertexSet()){
						if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, a, node)!=null){
							coexistencePathsFromA.add(node);
						}
					}
					coexistencePaths.put(a, coexistencePathsFromA);
				}
				coexistenceDiagram.addVertex(b);
				ArrayList<String> coexistencePathsFromB = coexistencePaths.get(b);
				if(!coexistencePaths.containsKey(b)){
					coexistencePathsFromB = new ArrayList<String>();
					coexistencePathsFromB.add(b);
					for(String node : coexistenceDiagram.vertexSet()){
						if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, b, node)!=null){
							coexistencePathsFromB.add(node);
						}
					}
					coexistencePaths.put(b, coexistencePathsFromB);
				}
			}
		}

		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				ArrayList<String> currentEdge = new ArrayList<String>();
				currentEdge.add(a);
				currentEdge.add(b);
				boolean removed = false;
				if(!alreadyRemoved.contains(currentEdge)){
					if(coexistencePaths.get(a)!=null){
						for(String reachableNodeFromA : coexistencePaths.get(a)){
							if(coexistencePaths.get(b)!=null){
								for(String reachableNodeFromB : coexistencePaths.get(b)){
									ArrayList<String> reachedEdge = new ArrayList<String>();
									reachedEdge.add(a);
									reachedEdge.add(b);
									ArrayList<String> invreachedEdge = new ArrayList<String>();
									invreachedEdge.add(b);
									invreachedEdge.add(a);
									ArrayList<String>  currentNegEdge = new ArrayList<String>();
									currentNegEdge.add(reachableNodeFromA);
									currentNegEdge.add(reachableNodeFromB);
									if(!reachableNodeFromA.equals(a) || !reachableNodeFromB.equals(b)){
										if(notCoexistenceDiagram.containsEdge(reachableNodeFromA, reachableNodeFromB) && !alreadyRemoved.contains(currentNegEdge)){
											transitiveClosureNotCoexistenceConstraints.add(id);
											alreadyRemoved.add(reachedEdge);
											alreadyRemoved.add(invreachedEdge);
											removed = true;
											break;		
										}
									}
								}
								if(removed){
									break;
								}

							}
						}
					}
				}
			}
		}
	}










	/* 
	 * Methods used for V1
	 */



	public static void pruneSuccessionConstraints(List<DiscoveredConstraint> sucConstraints) {

		//For reusing Declare miner code as-is
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		Vector<Integer> transitiveClosureSuccessionConstraints = new Vector<Integer>();

		for (int i = 0; i < sucConstraints.size(); i++) {
			//Keys are offset by -1 compared to original implementation, but that should not matter
			constraintParametersMap.put(i, Arrays.asList(sucConstraints.get(i).getActivationActivity().getActivityName(),sucConstraints.get(i).getTargetActivity().getActivityName()));
			constraintTemplateMap.put(i, TemplateUtils.getDeclareTemplate(sucConstraints.get(i).getTemplate()));
		}

		/*
		 * Code from Declare miner START
		 */

		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for(Integer id : constraintParametersMap.keySet()){
			//This check should only be needed if  
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession))){

				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				if(!d.containsEdge(a, b)&&constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)){
					transitiveClosureSuccessionConstraints.add(id);
				}
			}
		}

		/*
		 * Code from Declare miner END
		 */


		for (int i = sucConstraints.size()-1; i >= 0; i--) {
			if (transitiveClosureSuccessionConstraints.contains(i)) {
				sucConstraints.remove(i);
			}
		}
	}




	public static void pruneResponseConstraints(List<DiscoveredConstraint> resConstraints) {

		//For reusing Declare miner code as-is
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		Vector<Integer> transitiveClosureResponseConstraints = new Vector<Integer>();

		for (int i = 0; i < resConstraints.size(); i++) {
			//Keys are offset by -1 compared to original implementation, but that should not matter
			constraintParametersMap.put(i, Arrays.asList(resConstraints.get(i).getActivationActivity().getActivityName(),resConstraints.get(i).getTargetActivity().getActivityName()));
			constraintTemplateMap.put(i, TemplateUtils.getDeclareTemplate(resConstraints.get(i).getTemplate()));
		}

		/*
		 * Code from Declare miner START
		 */

		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for(Integer id : constraintParametersMap.keySet()){
			//if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id))){
			//Removed "&& !transitiveClosureSuccessionConstraints.contains(id)" from original check since I plan to call this method independently from all Successions
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Response)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession))){

				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//			HashSet<String> bluesA = null;
				//			HashSet<String> redsB = null;
				//			HashSet<String> bluesB = null;
				//			HashSet<String> redsA = null;
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				//			d.addEdge(constraintParametersMap.get(cd.getId()).get(0), constraintParametersMap.get(cd.getId()).get(1));
				//		}

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Response)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				if(!d.containsEdge(a, b) &&constraintTemplateMap.get(id).equals(DeclareTemplate.Response)){
					transitiveClosureResponseConstraints.add(id);
				}
			}
		}

		/*
		 * Code from Declare miner END
		 */


		for (int i = resConstraints.size()-1; i >= 0; i--) {
			if (transitiveClosureResponseConstraints.contains(i)) {
				resConstraints.remove(i);
			}
		}
	}




	public static void prunePrecedenceConstraints(List<DiscoveredConstraint> preConstraints) {

		//For reusing Declare miner code as-is
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		Vector<Integer> transitiveClosurePrecedenceConstraints = new Vector<Integer>();

		for (int i = 0; i < preConstraints.size(); i++) {
			//Keys are offset by -1 compared to original implementation, but that should not matter
			constraintParametersMap.put(i, Arrays.asList(preConstraints.get(i).getTargetActivity().getActivityName(),preConstraints.get(i).getActivationActivity().getActivityName()));
			constraintTemplateMap.put(i, TemplateUtils.getDeclareTemplate(preConstraints.get(i).getTemplate()));
		}

		/*
		 * Code from Declare miner START
		 */

		HashMap<String,HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String,HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		//List<List<String>> simplePaths  =  new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for(Integer id : constraintParametersMap.keySet()){
			//if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))  || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id))){
			//Removed " && !transitiveClosureSuccessionConstraints.contains(id)" from original check since I plan to call this method independently from all Successions
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))  || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)) || (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession))){

				String a = constraintParametersMap.get(id).get(1);
				String b = constraintParametersMap.get(id).get(0);
				//			HashSet<String> bluesA = null;
				//			HashSet<String> redsB = null;
				//			HashSet<String> bluesB = null;
				//			HashSet<String> redsA = null;
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				//			d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				//			d.addEdge(constraintParametersMap.get(cd.getId()).get(0), constraintParametersMap.get(cd.getId()).get(1));
				//		}

				HashSet<String> descendantsA = descendantsMap.get(a);
				if(descendantsA==null){
					descendantsA = new HashSet<String>();
				}
				if(descendantsMap.get(b)!=null){
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				}else{
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a,descendantsA);


				if(ancestorsMap.get(a)!=null){
					for(String ancestor : ancestorsMap.get(a)){
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if(descendantsMap.get(b)!=null){
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						}else{
							descendants.add(b);
						}
						descendantsMap.put(ancestor,descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if(ancestorsB==null){
					ancestorsB = new HashSet<String>();
				}

				if(ancestorsMap.get(a)!=null){
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				}else{
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b,ancestorsB);


				if(descendantsMap.get(b)!=null){
					for(String descendant : descendantsMap.get(b)){
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if(ancestorsMap.get(a)!=null){
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						}else{
							ancestors.add(a);
						}
						ancestorsMap.put(descendant,ancestors);
					}
				}

				//	System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if(DijkstraShortestPath.findPathBetween(d, a, b)==null){
					DefaultEdge de =ef.createEdge(a, b);
					d.addEdge(a,b,de);
					HashSet<String> reds = new HashSet<String>();
					//				boolean isolated = true;
					if(ancestorsMap.get(a)!=null){
						reds.addAll(ancestorsMap.get(a));
						//					isolated = false;
					}else{
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if(descendantsMap.get(b)!=null){
						blues.addAll(descendantsMap.get(b));
					}else{
						//					if(!isolated){
						//						blues.add(b);
						//					}
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for(String red : reds){
						for(String blue : blues){
							if(!red.equals(a) || !blue.equals(b)){
								if(d.containsEdge(red, blue)){
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if(redA!=null && bBlue!=null){
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for(DefaultEdge edge : redA){
											if(!nodes.contains(edge.getSource())&&!nodes.contains(edge.getTarget())){
												nodes.add((String)edge.getSource());
											}else{
												shortest = false;
												break;
											}
										}

										if(shortest){
											d.removeEdge(red, blue);		
										}
									}
								}
							}
						}
					}
					//				d.addEdge(a, b);
				}
			}
		}
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)){
				String a = constraintParametersMap.get(id).get(1);
				String b = constraintParametersMap.get(id).get(0);
				if(!d.containsEdge(a, b) &&constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)){
					transitiveClosurePrecedenceConstraints.add(id);
				}
			}
		}

		/*
		 * Code from Declare miner END
		 */


		for (int i = preConstraints.size()-1; i >= 0; i--) {
			if (transitiveClosurePrecedenceConstraints.contains(i)) {
				preConstraints.remove(i);
			}
		}
	}


	//Shouldn't really be needed since Not Co-Existence constraints are not transitive, meaning that a Not Co-Existence constraint could never be removed because of any set of other Not Co-Existence constraints
	public static List<DiscoveredConstraint> getTransitiveClosureNotCoexistenceConstraints(List<DiscoveredConstraint> notcoConstraints) {

		//For reusing Declare miner code as-is
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		Vector<Integer> transitiveClosureNotCoexistenceConstraints = new Vector<Integer>();

		for (int i = 0; i < notcoConstraints.size(); i++) {
			//Keys are offset by -1 compared to original implementation, but that should not matter
			constraintParametersMap.put(i, Arrays.asList(notcoConstraints.get(i).getActivationActivity().getActivityName(),notcoConstraints.get(i).getTargetActivity().getActivityName()));
			constraintTemplateMap.put(i, TemplateUtils.getDeclareTemplate(notcoConstraints.get(i).getTemplate()));
		}

		/*
		 * Code from Declare miner START
		 */

		//	HashMap<String,ArrayList<String>> negativeConnections = new HashMap<String, ArrayList<String>>();

		DefaultDirectedGraph<String, DefaultEdge> coexistenceDiagram = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		DefaultDirectedGraph<String, DefaultEdge> notCoexistenceDiagram = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};

		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence))){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//	System.out.println(d.toString());
				notCoexistenceDiagram.addVertex(a);
				notCoexistenceDiagram.addVertex(b);
				//		if(DijkstraShortestPath.findPathBetween(notCoexistenceDiagram, a, b)==null){
				DefaultEdge de =ef.createEdge(a, b);
				notCoexistenceDiagram.addEdge(a,b,de);

				DefaultEdge ed =ef.createEdge(b, a);
				notCoexistenceDiagram.addEdge(b,a,ed);

				//				d.addEdge(a, b);
				//		}			
			}


		}


		for(Integer id : constraintParametersMap.keySet()){
			if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || 
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) || 
					(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)) || 
					constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)){
				//if((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) || 
				//	(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) || 
				//	(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)&& !transitiveClosureCoexistenceConstraints.contains(id)) || 
				//	constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) && !transitiveClosureSuccessionConstraints.contains(id)){
				//Removed " && !transitiveClosureCoexistenceConstraints.contains(id) and !transitiveClosureSuccessionConstraints.contains(id)" from original check since I plan to call this method independently from all Successions
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				//	System.out.println(d.toString());
				coexistenceDiagram.addVertex(a);
				coexistenceDiagram.addVertex(b);
				//	if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, a, b)==null){
				DefaultEdge de =ef.createEdge(a, b);
				coexistenceDiagram.addEdge(a,b,de);

				DefaultEdge ed =ef.createEdge(b, a);
				coexistenceDiagram.addEdge(b,a,ed);

				//				d.addEdge(a, b);
				//	}			
			}


		}
		HashMap<String, ArrayList<String>> coexistencePaths = new HashMap<String, ArrayList<String>>();
		ArrayList<ArrayList<String>> alreadyRemoved = new ArrayList<ArrayList<String>>();
		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				coexistenceDiagram.addVertex(a);

				ArrayList<String> coexistencePathsFromA = coexistencePaths.get(a);
				if(!coexistencePaths.containsKey(a)){
					coexistencePathsFromA = new ArrayList<String>();
					coexistencePathsFromA.add(a);
					for(String node : coexistenceDiagram.vertexSet()){
						if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, a, node)!=null){
							coexistencePathsFromA.add(node);
						}
					}
					coexistencePaths.put(a, coexistencePathsFromA);
				}
				coexistenceDiagram.addVertex(b);
				ArrayList<String> coexistencePathsFromB = coexistencePaths.get(b);
				if(!coexistencePaths.containsKey(b)){
					coexistencePathsFromB = new ArrayList<String>();
					coexistencePathsFromB.add(b);
					for(String node : coexistenceDiagram.vertexSet()){
						if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, b, node)!=null){
							coexistencePathsFromB.add(node);
						}
					}
					coexistencePaths.put(b, coexistencePathsFromB);
				}
			}
		}

		for(Integer id : constraintParametersMap.keySet()){
			if(constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence)){
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				ArrayList<String> currentEdge = new ArrayList<String>();
				currentEdge.add(a);
				currentEdge.add(b);
				boolean removed = false;
				if(!alreadyRemoved.contains(currentEdge)){
					if(coexistencePaths.get(a)!=null){
						for(String reachableNodeFromA : coexistencePaths.get(a)){
							if(coexistencePaths.get(b)!=null){
								for(String reachableNodeFromB : coexistencePaths.get(b)){
									ArrayList<String> reachedEdge = new ArrayList<String>();
									reachedEdge.add(a);
									reachedEdge.add(b);
									ArrayList<String> invreachedEdge = new ArrayList<String>();
									invreachedEdge.add(b);
									invreachedEdge.add(a);
									ArrayList<String>  currentNegEdge = new ArrayList<String>();
									currentNegEdge.add(reachableNodeFromA);
									currentNegEdge.add(reachableNodeFromB);
									if(!reachableNodeFromA.equals(a) || !reachableNodeFromB.equals(b)){
										if(notCoexistenceDiagram.containsEdge(reachableNodeFromA, reachableNodeFromB) && !alreadyRemoved.contains(currentNegEdge)){
											transitiveClosureNotCoexistenceConstraints.add(id);
											alreadyRemoved.add(reachedEdge);
											alreadyRemoved.add(invreachedEdge);
											removed = true;
											break;		
										}
									}
								}
								if(removed){
									break;
								}

							}
						}
					}
				}
			}
		}



		/*
		 * Code from Declare miner END
		 */


		for (int i = notcoConstraints.size()-1; i >= 0; i--) {
			if (transitiveClosureNotCoexistenceConstraints.contains(i)) {
				notcoConstraints.remove(i);
			}
		}
		return notcoConstraints;

	}

}
