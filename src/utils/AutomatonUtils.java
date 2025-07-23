package utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections15.BidiMap;
import org.processmining.ltl2automaton.plugins.automaton.Automaton;
import org.processmining.ltl2automaton.plugins.automaton.DeterministicAutomaton;
import org.processmining.ltl2automaton.plugins.formula.DefaultParser;
import org.processmining.ltl2automaton.plugins.formula.Formula;
import org.processmining.ltl2automaton.plugins.formula.conjunction.ConjunctionFactory;
import org.processmining.ltl2automaton.plugins.formula.conjunction.ConjunctionTreeLeaf;
import org.processmining.ltl2automaton.plugins.formula.conjunction.ConjunctionTreeNode;
import org.processmining.ltl2automaton.plugins.formula.conjunction.DefaultTreeFactory;
import org.processmining.ltl2automaton.plugins.formula.conjunction.GroupedTreeConjunction;
import org.processmining.ltl2automaton.plugins.formula.conjunction.TreeFactory;
import org.processmining.ltl2automaton.plugins.ltl.SyntaxParserException;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;

public class AutomatonUtils {

	private static TreeFactory<ConjunctionTreeNode, ConjunctionTreeLeaf> treeFactory = DefaultTreeFactory.getInstance();
	private static ConjunctionFactory<? extends GroupedTreeConjunction> conjunctionFactory = GroupedTreeConjunction.getFactory(treeFactory);

	private AutomatonUtils() {
		//Private constructor to avoid unnecessary instantiation of the class
	}


	public static Automaton createAutomaton(List<DiscoveredActivity> activities, List<DiscoveredConstraint> constraints, BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		List<String> formulas = new ArrayList<String>();
		for (DiscoveredConstraint discoveredConstraint : constraints) {
			formulas.add(getConstraintLtlFormula(discoveredConstraint, activityToEncodingsMap));
		}

		if (!formulas.isEmpty()) {
			
			//Enforcing the alphabet, otherwise ltl2automata will allow satisfying negative chain constraints with activities that are not in the alphabet, which may result in many extra states  
			List<String> encodedActivities = new ArrayList<String>();
			activities.forEach(act -> {encodedActivities.add(activityToEncodingsMap.get(act));});
			String activitiesFormula = "([](" + String.join(" \\/ ", encodedActivities) + "))";
			formulas.add(activitiesFormula);
			
			
			String automatonFormula = "((" + String.join(") /\\ (", formulas) + "))";
			
			try {
				return createAutomatonForLtlFormula(automatonFormula);
			} catch (SyntaxParserException e) {
				System.err.println("Malformed LTL: " + e.getMessage());
				return null;
			}	
		} else {
			return null;
		}
	}

	//Creates an automaton for LTL formula
	public static DeterministicAutomaton createAutomatonForLtlFormula(String ltlFormula) throws SyntaxParserException {
		Formula parsedFormula = new DefaultParser(ltlFormula).parse();
		//System.out.println("Parsed formula: " + parsedFormula);
		GroupedTreeConjunction conjunction = conjunctionFactory.instance(parsedFormula);

		return conjunction.getAutomaton().op.determinize().op.complete();
	}

	public static String getConstraintLtlFormula(DiscoveredConstraint discoveredConstraint, BidiMap<DiscoveredActivity, String> activityToEncodingsMap) {
		String ltlFormula = AutomatonUtils.getGenericLtlFormula(discoveredConstraint.getTemplate());

		System.out.println(discoveredConstraint); //For debugging a mysterious nullpointer exception that I have not managed to reliably reproduce

		//Replacing activity placeholders in the generic formula with activity encodings based on the model
		if (discoveredConstraint.getTemplate().getReverseActivationTarget()) {
			ltlFormula = ltlFormula.replace("\"B\"", activityToEncodingsMap.get(discoveredConstraint.getActivationActivity()));
			ltlFormula = ltlFormula.replace("\"A\"", activityToEncodingsMap.get(discoveredConstraint.getTargetActivity()));
		} else {
			ltlFormula = ltlFormula.replace("\"A\"", activityToEncodingsMap.get(discoveredConstraint.getActivationActivity()));
			if (discoveredConstraint.getTemplate().getIsBinary()) {
				ltlFormula = ltlFormula.replace("\"B\"", activityToEncodingsMap.get(discoveredConstraint.getTargetActivity()));
			}
		}
		return ltlFormula;	
	}

	//Returns a generic LTL formula for a given Declare template
	public static String getGenericLtlFormula(ConstraintTemplate declareTemplate) {
		String formula = "";
		switch (declareTemplate) {
		case Absence:
			formula = "!( <> ( \"A\" ) )";
			break;
		case Absence2:
			formula = "! ( <> ( ( \"A\" /\\ X(<>(\"A\")) ) ) )";
			break;
		case Absence3:
			formula = "! ( <> ( ( \"A\" /\\  X ( <> ((\"A\" /\\  X ( <> ( \"A\" ) )) ) ) ) ))";
			break;
		case Alternate_Precedence:
			formula = "(((( !(\"B\") U \"A\") \\/ []( !(\"B\"))) /\\ []((\"B\" ->( (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) \\/ X((( !(\"B\") U \"A\") \\/ []( !(\"B\")))))))) /\\ (  ! (\"B\" ) \\/ (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) ))";
			break;
		case Alternate_Response:
			formula = "( []( ( \"A\" -> X(( (! ( \"A\" )) U \"B\" ) )) ) )";
			break;
		case Alternate_Succession:
			formula = "( []((\"A\" -> X(( !(\"A\") U \"B\")))) /\\ (((( !(\"B\") U \"A\") \\/ []( !(\"B\"))) /\\ []((\"B\" ->( (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) \\/ X((( !(\"B\") U \"A\") \\/ []( !(\"B\")))))))) /\\ (  ! (\"B\" ) \\/ (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) )))";
			break;
		case Chain_Precedence:
			formula = "[]( ( X( \"B\" ) -> \"A\") )/\\ (  ! (\"B\" ) \\/ (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) )";
			break;
		case Chain_Response:
			formula = "[] ( ( \"A\" -> X( \"B\" ) ) )";
			break;
		case Chain_Succession:
			formula = "([]( ( \"A\" -> X( \"B\" ) ) )) /\\ ([]( ( X( \"B\" ) ->  \"A\") ) /\\ (  ! (\"B\" ) \\/ (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) ))";
			break;
		case Choice:
			formula = "(  <> ( \"A\" ) \\/ <>( \"B\" )  )";
			break;
		case CoExistence:
			formula = "( ( <>(\"A\") -> <>( \"B\" ) ) /\\ ( <>(\"B\") -> <>( \"A\" ) )  )";
			break;
		case End:
			//formula = "( []((\"A\") -> ( !(X(\"A\" /\\ (!(\"A\")))) )";
			//formula = "(\"A\") && !X (true)";
			//formula = "<>( (\"A\") && !X (true))";
			//formula = "( <>((\"A\") && ( ! (X(\"A\" /\\  (!(\"A\")))))) )";
			//formula = " ( <>((\"A\") && ( (X(\"A\" /\\  (!(\"A\")))))) )";

			formula = "( <> ( \"A\" && !X( \"A\" U ( !\"A\" ) ) ) )";

			break;
		case Exactly1:
			formula = "(  <> (\"A\") /\\ ! ( <> ( ( \"A\" /\\ X(<>(\"A\")) ) ) ) )";
			break;
		case Exactly2:
			formula = "( <> (\"A\" /\\ (\"A\" -> (X(<>(\"A\"))))) /\\  ! ( <>( \"A\" /\\ (\"A\" -> X( <>( \"A\" /\\ (\"A\" -> X ( <> ( \"A\" ) ))) ) ) ) ) )";
			break;
		case Exclusive_Choice:
			formula = "(  ( <>( \"A\" ) \\/ <>( \"B\" )  )  /\\ !( (  <>( \"A\" ) /\\ <>( \"B\" ) ) ) )";
			break;
		case Existence:
			formula = "( <> ( \"A\" ) )";
			break;
		case Existence2:
			formula = "<> ( ( \"A\" /\\ X(<>(\"A\")) ) )";
			break;
		case Existence3:
			formula = "<>( \"A\" /\\ X(  <>( \"A\" /\\ X( <> \"A\" )) ))";
			break;
		case Init:
			formula = "( \"A\" )";
			break;
		case Not_Chain_Precedence:
			formula = "[] ( \"A\" -> !( X ( \"B\" ) ) )";
			break;
		case Not_Chain_Response:
			formula = "[] ( \"A\" -> !( X ( \"B\" ) ) )";
			break;
		case Not_Chain_Succession:
			formula = "[]( ( \"A\" -> !(X( \"B\" ) ) ))";
			break;
		case Not_CoExistence:
			formula = "(<>( \"A\" )) -> (!(<>( \"B\" )))";
			break;
		case Not_Precedence:
			formula = "[] ( \"A\" -> !( <> ( \"B\" ) ) )";
			break;
		case Not_Responded_Existence:
			formula = "(<>( \"A\" )) -> (!(<>( \"B\" )))";
			break;
		case Not_Response:
			formula = "[] ( \"A\" -> !( <> ( \"B\" ) ) )";
			break;
		case Not_Succession:
			formula = "[]( ( \"A\" -> !(<>( \"B\" ) ) ))";
			break;
		case Precedence:
			formula = "( ! (\"B\" ) U \"A\" ) \\/ ([](!(\"B\"))) /\\ (  ! (\"B\" ) \\/ (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) )";
			break;
		case Responded_Existence:
			formula = "(( ( <>( \"A\" ) -> (<>( \"B\" ) )) ))";
			break;
		case Response:
			formula = "( []( ( \"A\" -> <>( \"B\" ) ) ))";
			break;
		case Succession:
			formula = "(( []( ( \"A\" -> <>( \"B\" ) ) ))) /\\ (( ! (\"B\" ) U \"A\" ) \\/ ([](!(\"B\"))) /\\ (  ! (\"B\" ) \\/ (!(X(\"A\")) /\\ !(X(!(\"A\"))) ) )   )";
			break;
		default:
			break;
		}
		return formula;
	}
}
