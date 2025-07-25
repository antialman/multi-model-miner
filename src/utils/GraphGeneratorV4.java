package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.processmining.ltl2automaton.plugins.automaton.Automaton;
import org.processmining.ltl2automaton.plugins.automaton.State;
import org.processmining.ltl2automaton.plugins.automaton.Transition;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import model.v4.PnContainer;

public class GraphGeneratorV4 {
	// private constructor to avoid unnecessary instantiation of the class
	private GraphGeneratorV4() {
	}


	public static String createDeclareVisualizationString(List<DiscoveredActivity> filteredActivities, List<DiscoveredConstraint> filteredConstraints, boolean alternativeLayout, boolean standalone) {
		StringBuilder sb = new StringBuilder();
		
		if (standalone) {
			sb.append("digraph \"\" {");

			if (alternativeLayout)
				sb.append("rankdir = \"LR\"");

			sb.append("ranksep = \".6\"");
			sb.append("nodesep = \".5\"");
			sb.append("node [style=\"filled\", shape=box, fontsize=\"8\", fontname=\"Helvetica\"]");
			sb.append("edge [fontsize=\"8\", fontname=\"Helvetica\" arrowsize=\".8\"]");
		}
		
		

		Map<DiscoveredActivity, String> nodeNames = new HashMap<>();
		Map<String, Map<Integer, String>> activityToUnaryConstraints = new HashMap<>();

		for (DiscoveredActivity a : filteredActivities) {
			nodeNames.put(a, "node"+filteredActivities.indexOf(a));
			activityToUnaryConstraints.put(a.getActivityFullName(), new HashMap<>());
		}

		List<String> edges = new ArrayList<>();
		for (DiscoveredConstraint c : filteredConstraints) {
			String label = "";

			if (c.getTemplate().getIsBinary()) {
				
				label += c.getTemplate().toString();
				label = label.replace(" ", "\\\\n"); //Makes the visualization a bit more compact

				//Showing support is redundant as the approach uses 100% support threshold
				//label += "\\\\n";
				//float support = c.getConstraintSupport();
				//label += String.format("%.1f%%", support*100);

				edges.add(buildEdgeString(nodeNames.get(c.getActivationActivity()), nodeNames.get(c.getTargetActivity()), c.getTemplate(), label, null));

			} else {
				label = c.getTemplate().toString();
				int index = activityToUnaryConstraints.get(c.getActivationActivity().getActivityFullName()).size();
				//label += " - " + String.format("%.1f%%", c.getConstraintSupport()*100); //Showing support is redundant as the approach uses 100% support threshold

				activityToUnaryConstraints.get(c.getActivationActivity().getActivityFullName()).put(index, label);
			}
		}

		for (DiscoveredActivity discoveredActivity : filteredActivities)
			sb.append(buildNodeString(nodeNames.get(discoveredActivity), discoveredActivity.getActivityFullName(), activityToUnaryConstraints.get(discoveredActivity.getActivityFullName()), discoveredActivity.getActivitySupport(), alternativeLayout, standalone));

		for (String string : edges)
			sb.append(string);

		
		if (standalone) {
			sb.append("}");
		}

		return sb.toString();
	}

	private static String buildEdgeString(String nodeA, String nodeB, ConstraintTemplate template, String label, String constraintState) {
		String style = getStyleForTemplate(template, label, "", constraintState);

		if (template.getReverseActivationTarget())
			return nodeB + " -> " + nodeA + " " + style;
		else
			return nodeA + " -> " + nodeB + " " + style;
	}

	private static String getStyleForTemplate(ConstraintTemplate template, String label, String penwidth, String constraintState) {
		String color = "#000000";;
		if (constraintState != null) {
			switch(constraintState) {
			case "conflict":
				color = "#ff9900";
				break;
			case "sat":
				color = "#66ccff";
				break;
			case "viol":
				color = "#d44942";
				break;
			case "poss.viol":
				color = "#ffd700";
				break;
			case "poss.sat":
				color = "#79a888";
				break;
			}
		}

		switch(template) {
		case Responded_Existence:
			return "[dir=\"both\", edgetooltip=\"Responded Existence\", labeltooltip=\"Responded Existence\",arrowhead=\"none\",arrowtail=\"dot\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Response:
			return "[dir=\"both\", edgetooltip=\"Response\", labeltooltip=\"Response\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Alternate_Response:
			return "[edgetooltip=\"Alternate Response\", labeltooltip=\"Alternate Response\", dir=\"both\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+":"+color+"\","+penwidth+"]";
		case Chain_Response:
			return "[edgetooltip=\"Chain Response\", labeltooltip=\"Chain Response\", dir=\"both\", arrowhead=\"normal\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+":"+color+":"+color+"\","+penwidth+"]";
		case Precedence:
			return "[arrowhead=\"dotnormal\", edgetooltip=\"Precedence\", labeltooltip=\"Precedence\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Alternate_Precedence:
			return "[arrowhead=\"dotnormal\", edgetooltip=\"Alternate Precedence\", labeltooltip=\"Alternate Precedence\", label=\""+label+"\", color=\""+color+":"+color+"\","+penwidth+"]";
		case Chain_Precedence:
			return "[arrowhead=\"dotnormal\", edgetooltip=\"Chain Precedence\", labeltooltip=\"Chain Precedence\", label=\""+label+"\", color=\""+color+":"+color+":"+color+"\","+penwidth+"]";
		case Succession:
			return "[dir=\"both\", edgetooltip=\"Succession\", labeltooltip=\"Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Alternate_Succession:
			return "[dir=\"both\", edgetooltip=\"Alternate Succession\", labeltooltip=\"Alternate Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+":"+color+"\","+penwidth+"]";
		case Chain_Succession:
			return "[dir=\"both\", edgetooltip=\"Chain Succession\", labeltooltip=\"Chain Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+":"+color+":"+color+"\","+penwidth+"]";
		case CoExistence:
			return "[dir=\"both\", edgetooltip=\"CoExistence\", labeltooltip=\"CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Choice:
			return "[dir=\"both\", edgetooltip=\"Choice\", labeltooltip=\"Choice\", arrowhead=\"odiamond\", arrowtail=\"odiamond\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Exclusive_Choice:
			return "[dir=\"both\", edgetooltip=\"Exclusive Choice\", labeltooltip=\"Exclusive Choice\", arrowhead=\"diamond\", arrowtail=\"diamond\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Not_Chain_Precedence:
			return "[arrowhead=\"dotnormal\", edgetooltip=\"Not Chain Precedence\", labeltooltip=\"Not Chain Precedence\", style=\"dashed\", label=\""+label+"\", color=\""+color+":"+color+":"+color+"\","+penwidth+"]";
		case Not_Chain_Response:
			return "[dir=\"both\", edgetooltip=\"Not Chain Response\", labeltooltip=\"Not Chain Response\", arrowhead=\"normal\", arrowtail=\"dot\", style=\"dashed\", label=\""+label+"\", color=\""+color+":"+color+":"+color+"\","+penwidth+"]";
		case Not_Chain_Succession:
			return "[dir=\"both\", edgetooltip=\"Not Chain Succession\", labeltooltip=\"Not Chain Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", style=\"dashed\", label=\""+label+"\", color=\""+color+":"+color+":"+color+"\","+penwidth+"]";
		case Not_CoExistence:
			return "[dir=\"both\", edgetooltip=\"Not CoExistence\", labeltooltip=\"Not CoExistence\", arrowhead=\"dot\", arrowtail=\"dot\", style=\"dashed\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Not_Precedence:
			return "[arrowhead=\"dotnormal\", edgetooltip=\"Not Precedence\", labeltooltip=\"Not Precedence\", style=\"dashed\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Not_Responded_Existence:
			return "[dir=\"both\", arrowtail=\"dot\", arrowhead=\"none\", edgetooltip=\"Not Responded Existence\", labeltooltip=\"Not Responded Existence\", style=\"dashed\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Not_Response:
			return "[dir=\"both\", edgetooltip=\"Not Response\", labeltooltip=\"Not Response\", arrowhead=\"normal\", arrowtail=\"dot\", style=\"dashed\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		case Not_Succession:
			return "[dir=\"both\", edgetooltip=\"Not Succession\", labeltooltip=\"Not Succession\", arrowhead=\"dotnormal\", arrowtail=\"dot\", style=\"dashed\", label=\""+label+"\", color=\""+color+"\","+penwidth+"]";
		default:
			throw new NoSuchElementException("Style not defined for template " + template);
		}
	}

	//Used for discovery; creates a standard graphviz node strings
	private static String buildNodeString(String nodeId, String activityName, Map<Integer, String> ls, double supp, boolean horizontal, boolean standalone) {
		String color = "";
		String ss = "";
		if(supp != -1) {
			color = getColorFrom(supp,ls.size());
			ss = String.format("%.1f%%", supp*100);
		}
		if(supp == -1) {
			double portion = 1.0 / (ls.size()+1);
			color = "#0000ff";
			String fc= "#ffffff";
			color = "fillcolor=\""+color+";"+portion+":#000000\" gradientangle=90 fontcolor=\""+fc+"\"";
		}
		if(ls.isEmpty()) {
			if (standalone) {
				return nodeId + " [label=" + "\"" + activityName + "\\\\n" + ss +"\"" + color +" tooltip=\""+activityName+"\"]";
			} else {
				return nodeId + " [label=" + "\"" + activityName +"\"" + color +" tooltip=\""+activityName+"\" style=\"filled\" shape=box]";
			}
		}
		else {
			String unaryRep = "\"";
			if (!horizontal) {
				unaryRep += "{";
			}
			for(String u : ls.values()) {
				unaryRep += u + "|";
			}
			unaryRep += activityName + "\\\\n" + ss;
			if (!horizontal) {
				unaryRep += "}";
			}
			unaryRep += "\"";
			//System.out.println(n + " [shape=\"record\" label="+ unaryRep +" "+color+"]");
			
			return nodeId + " [shape=\"record\" label="+ unaryRep +" "+color+" tooltip=\""+activityName+"\"]"; //Not planning to show unary constraints in standalone mode
			
		}
	}

	private static String getColorFrom(double supp,int size) {
		double res = 51 + 26 * (1-supp) * 27.46;
		double portion = 1.5 / (size+1.5);
		String color = "";
		if(res > 255) {
			long remaining = Math.round((res - 255) / 2);
			color = "#"+getHexValue(remaining)+getHexValue(remaining)+"ff";
		}
		else {
			color = "#0000"+getHexValue(Math.round(res));
		}
		String fc = "#e6e600";
		return "fillcolor=\""+color+";"+portion+":#808080\" gradientangle=90 fontcolor=\""+fc+"\"";
	}

	private static String getHexValue(long value) {
		long b1 = value / 16;
		long b2 = value % 16;
		String s = "";
		if(b1 == 0) s = "0";
		if(b1 == 1) s = "1";
		if(b1 == 2) s = "2";
		if(b1 == 3) s = "3";
		if(b1 == 4) s = "4";
		if(b1 == 5) s = "5";
		if(b1 == 6) s = "6";
		if(b1 == 7) s = "7";
		if(b1 == 8) s = "8";
		if(b1 == 9) s = "9";
		if(b1 == 10) s = "a";
		if(b1 == 11) s = "b";
		if(b1 == 12) s = "c";
		if(b1 == 13) s = "d";
		if(b1 == 14) s = "e";
		if(b1 == 15) s = "f";

		if(b2 == 0) s += "0";
		if(b2 == 1) s += "1";
		if(b2 == 2) s += "2";
		if(b2 == 3) s += "3";
		if(b2 == 4) s += "4";
		if(b2 == 5) s += "5";
		if(b2 == 6) s += "6";
		if(b2 == 7) s += "7";
		if(b2 == 8) s += "8";
		if(b2 == 9) s += "9";
		if(b2 == 10) s += "a";
		if(b2 == 11) s += "b";
		if(b2 == 12) s += "c";
		if(b2 == 13) s += "d";
		if(b2 == 14) s += "e";
		if(b2 == 15) s += "f";

		return s;
	}


	public static String createAutomatonVisualizationString(List<DiscoveredActivity> activities, List<DiscoveredConstraint> constraints, boolean alternativeLayout, BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		//Adopted from org.processmining.ltl2automaton.plugins.automaton.DOTExporter, but significantly modified
		Automaton aut = AutomatonUtils.createAutomaton(activities, constraints, activityToEncodingsMap);
		StringBuilder sb = new StringBuilder();
		sb.append("digraph \"\" {");
		
		if (aut==null) {
			sb.append("}");
			return sb.toString();
		} else {
			sb.append(" init [shape=none, label=\"\"];");
			if (!alternativeLayout)
				sb.append(" rankdir = \"LR\"");

			for (final State s : aut) {
				if (!isNonAcceptingTrap(s)) {
					sb.append(" s");
					sb.append(s.getId());
					if (s.isAccepting()) {
						sb.append("[shape=doublecircle]");
					} else {
						sb.append("[shape=circle]");
					}
					sb.append(';');
				}
			}

			for (final State s : aut) {
				if (isNonAcceptingTrap(s)) {
					continue; //Skipping the fail-state
				}
				Map<State, List<String>> outStateToLabels = new HashMap<State, List<String>>();
				for (final Transition t : s.getOutput()) {
					if (isNonAcceptingTrap(t.getTarget())) {
						continue; //Skipping the edges that lead to the fail-state
					}
					//Replacing encodings with activity names and merging same labeled transitions (not the most efficient code)
					if (!outStateToLabels.containsKey(t.getTarget())) {
						outStateToLabels.put(t.getTarget(), new ArrayList<String>());
					}

					String transitionLabel = t.toString();
					if (t.isNegative()) { //Only one outgoing negative transitions per state
						List<String> negLabels = Arrays.asList(transitionLabel.split("&"));
						if (negLabels.size() == activities.size()) { //Not adding the edge if all activities were negated
							outStateToLabels.remove(t.getTarget());
							continue;
						}
						for (DiscoveredActivity activity : activities) {
							if (!negLabels.contains("!"+activityToEncodingsMap.get(activity))) {
								outStateToLabels.get(t.getTarget()).add(activity.getActivityName());
							}
						}
					} else if (t.isPositive()) { //Only one activity name per positive transition
						outStateToLabels.get(t.getTarget()).add(activityToEncodingsMap.inverseBidiMap().get(transitionLabel).getActivityName());
					} else { //The "any" transition remains as-is
						outStateToLabels.get(t.getTarget()).add(transitionLabel);
					}

				}

				//Adding the edge to dot string
				for (State outState : outStateToLabels.keySet()) {
					sb.append(" s");
					sb.append(s.getId());
					sb.append(" -> s");
					sb.append(outState.getId());
					sb.append("[label=\"");
					sb.append(String.join("\\n", outStateToLabels.get(outState)).replaceAll("\"", "\\\""));
					sb.append("\"];");
				}
			}

			if (aut.getInit() != null) {
				sb.append(" init -> s");
				sb.append(aut.getInit().getId());
				sb.append(';');
			}

			sb.append("}");

			return sb.toString();
		}

		
	}

	public static boolean isNonAcceptingTrap(State s) {
		if (!s.isAccepting() && s.getOutputSize()==1) {
			Transition t = s.getOutput().iterator().next();
			if (t.isAll() && t.getTarget()==s) {
				return true;
			}
		}
		return false;
	}


	
	public static String createHybridVisualizationString(Set<DiscoveredConstraint> remainingConstraints, Set<PnContainer> pnContainers, boolean constrainPn, BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		StringBuilder sb = new StringBuilder("digraph \"\" {");
		sb.append("rankdir = \"LR\"");
		sb.append("ranksep = \".4\"");
		sb.append("nodesep = \".3\"");
		sb.append("node [fontsize=\"8\", fontname=\"Helvetica\"]");
		sb.append("edge [fontsize=\"8\", fontname=\"Helvetica\" arrowsize=\".8\"]");
		
		
		if (!constrainPn) {
			Set<DiscoveredActivity> constraintActivities = new HashSet<DiscoveredActivity>();
			remainingConstraints.forEach(constraint -> {
				constraintActivities.add(constraint.getActivationActivity());
				if (constraint.getTemplate().getIsBinary()) {
					constraintActivities.add(constraint.getTargetActivity());
				}
			});
			
			sb.append(createDeclareVisualizationString(new ArrayList<DiscoveredActivity>(constraintActivities), new ArrayList<DiscoveredConstraint>(remainingConstraints), false, false));
			
			for (PnContainer pnContainer : pnContainers) {
				sb.append(createPnVisualizationString(pnContainer.getPetrinet()));
			}
		}
		
		sb.append("}");
		
		
		return sb.toString();
	}


	private static Object createPnVisualizationString(Petrinet petrinet) {
		StringBuilder sb = new StringBuilder();
		
		petrinet.getPlaces().forEach(place -> {
			sb.append(petrinet.getLabel() + place.getLabel() + "[label=\"" + place.getLabel() + "\",fontcolor=\"#777777\",shape=circle,fixedsize=true,height=.3,width=.3]");
			petrinet.getOutEdges(place).forEach(edge -> sb.append(" " + petrinet.getLabel() + edge.getSource().getLabel() + " -> " + petrinet.getLabel() + edge.getTarget().getLabel() + " "));
		});
		
		petrinet.getTransitions().forEach(transition -> {
			if (transition.isInvisible()) {
				sb.append(petrinet.getLabel() + transition.getLabel() + " [label=\"\"style=\"filled\",fillcolor=\"#000000\",shape=rect,height=0.3,width=.3]");
			} else {
				sb.append(petrinet.getLabel() + transition.getLabel() + " [label=\"" + transition.getLabel() + "\",shape=rect,height=0.3,width=.3]");
			}
			petrinet.getOutEdges(transition).forEach(edge -> sb.append(" " + petrinet.getLabel() + edge.getSource().getLabel() + " -> " + petrinet.getLabel() + edge.getTarget().getLabel() + " "));
		});
		
		
		
		
		return sb.toString();
	}
	
	
	
}
