package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;

import data.DiscoveredConstraint;

public class TransitiveClosureUtils {

	//Private constructor to avoid unnecessary instantiation of the class
	private TransitiveClosureUtils() {
	}
	
	
	
	
	public static List<DiscoveredConstraint> getTransitiveClosureSuccessionConstraints(List<DiscoveredConstraint> sucConstraints) {
		
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
			//This check shouldn't actually be needed since I plan on calling this method only with Succession constraints, but keeping it just in case 
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
		
		
		return sucConstraints;
	}
	
	
	
	
	public static List<DiscoveredConstraint> getTransitiveClosureResponseConstraints(List<DiscoveredConstraint> resConstraints) {

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
		
		
		return resConstraints;

	}
	
	
	
	
	public static List<DiscoveredConstraint> getTransitiveClosurePrecedenceConstraints(List<DiscoveredConstraint> preConstraints) {

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
		
		
		return preConstraints;


	}

}
