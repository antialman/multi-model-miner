package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import model.PlaceNode;
import model.TransitionNode;
import task.InitialFragments;

public class GraphGenerator {

	// private constructor to avoid unnecessary instantiation of the class
	private GraphGenerator() {
	}


	public static String createDeclareVisualizationString(List<DiscoveredActivity> filteredActivities, List<DiscoveredConstraint> filteredConstraints, boolean showConstraints, boolean alternativeLayout) {
		StringBuilder sb = new StringBuilder("digraph \"\" {");

		if (alternativeLayout)
			sb.append("rankdir = \"LR\"");

		sb.append("ranksep = \".6\"");
		sb.append("nodesep = \".5\"");
		sb.append("node [style=\"filled\", shape=box, fontsize=\"8\", fontname=\"Helvetica\"]");
		sb.append("edge [fontsize=\"8\", fontname=\"Helvetica\" arrowsize=\".8\"]");

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
				if (showConstraints)
					label += c.getTemplate().toString();


				label += "\\\\n";
				float support = c.getConstraintSupport();
				label += String.format("%.1f%%", support*100);

				edges.add(buildEdgeString(nodeNames.get(c.getActivationActivity()), nodeNames.get(c.getTargetActivity()), c.getTemplate(), label, null));

			} else {
				label = c.getTemplate().toString();
				int index = activityToUnaryConstraints.get(c.getActivationActivity().getActivityFullName()).size();
				label += " - " + String.format("%.1f%%", c.getConstraintSupport()*100);

				activityToUnaryConstraints.get(c.getActivationActivity().getActivityFullName()).put(index, label);
			}
		}

		for (DiscoveredActivity discoveredActivity : filteredActivities)
			sb.append(buildNodeString(nodeNames.get(discoveredActivity), discoveredActivity.getActivityFullName(), activityToUnaryConstraints.get(discoveredActivity.getActivityFullName()), discoveredActivity.getActivitySupport(), alternativeLayout));

		for (String string : edges)
			sb.append(string);

		sb.append("}");

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
	private static String buildNodeString(String nodeId, String activityName, Map<Integer, String> ls, double supp, boolean horizontal) {
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
			return nodeId + " [label=" + "\"" + activityName + "\\\\n" + ss +"\"" + color +" tooltip=\""+activityName+"\"]";
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
			return nodeId + " [shape=\"record\" label="+ unaryRep +" "+color+" tooltip=\""+activityName+"\"]";
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


	public static String createFragmentsVisualizationString(InitialFragments initialFragments) {
		StringBuilder sb = new StringBuilder("digraph \"\" {");
		sb.append("rankdir = \"LR\"");
		sb.append("ranksep = \".4\"");
		sb.append("nodesep = \".3\"");
		sb.append("node [fontsize=\"8\", fontname=\"Helvetica\"]");
		sb.append("edge [fontsize=\"8\", fontname=\"Helvetica\" arrowsize=\".8\"]");
		
		int ellipsisCount = 0;
		
		Set<Integer> createdTransitions = new HashSet<Integer>(); //TODO: Would be better to first create all of the nodes and then the arcs between them
		
		for (TransitionNode mainTransitionNode : initialFragments.getFragmentMainTransitions()) {
			sb.append(buildTransitionString(mainTransitionNode));
			
			for (PlaceNode outPlaceNode : mainTransitionNode.getOutgoingPlaces()) {
				sb.append(buildPlaceString(outPlaceNode));
				sb.append(" node" + mainTransitionNode.getNodeId() + " -> node" + outPlaceNode.getNodeId());
				for (TransitionNode outTransitionNode : outPlaceNode.getOutgoingTransitions()) { //Recursion not needed here because initial fragments will not go further
					if (!createdTransitions.contains(outTransitionNode.getNodeId())) {
						sb.append(buildTransitionString(outTransitionNode));
						sb.append(buildEllipsisNodeString(ellipsisCount));
						sb.append("node" + outTransitionNode.getNodeId() + " -> ellipsis" + ellipsisCount++);
						createdTransitions.add(outTransitionNode.getNodeId());
					}
					sb.append(" node" + outPlaceNode.getNodeId() + " -> node" + outTransitionNode.getNodeId());
				}
			}
			
			for (PlaceNode inPlaceNode : mainTransitionNode.getIncomingPlaces()) {
				sb.append(buildPlaceString(inPlaceNode));
				sb.append(" node" + inPlaceNode.getNodeId() + " -> node" + mainTransitionNode.getNodeId());
				for (TransitionNode inTransitionNode : inPlaceNode.getIncomingTransitions()) { //Recursion not needed here because initial fragments will not go further
					if (!createdTransitions.contains(inTransitionNode.getNodeId())) {
						sb.append(buildTransitionString(inTransitionNode));
						sb.append(buildEllipsisNodeString(ellipsisCount));
						sb.append("ellipsis" + ellipsisCount++ + " -> node" + inTransitionNode.getNodeId());
						createdTransitions.add(inTransitionNode.getNodeId());
					}
					sb.append(" node" + inTransitionNode.getNodeId() + " -> node" + inPlaceNode.getNodeId());
				}
			}
		}
		
		
		sb.append("}");

		return sb.toString();
	}
	
	private static String buildTransitionString(TransitionNode transitionNode) {
		
		if (transitionNode.isSilent()) {
			return " node" + transitionNode.getNodeId() + " [label=\"\"style=\"filled\",fillcolor=\"#000000\",shape=rect,height=0.3,width=.3]";
		} else {
			return " node" + transitionNode.getNodeId() + " [label=\"" + transitionNode.getTransitionLabel() + "\",shape=rect,height=0.3,width=.3]";
		}
	}
	
	private static String buildPlaceString(PlaceNode placeNode) {
		return " node" + placeNode.getNodeId() + "[shape=circle,fixedsize=true,label=\"\", height=.3,width=.3]";
	}
	
	private static String buildEllipsisNodeString(int ellipsisId) {
		return " ellipsis" + ellipsisId + "[shape=none,fontsize=\"14\",label=\"...\",height=0.3,width=.3]";
	}
}
