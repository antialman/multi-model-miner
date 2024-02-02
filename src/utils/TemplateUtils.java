package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;

public class TemplateUtils {

	//Private constructor to avoid unnecessary instantiation of the class
	private TemplateUtils() {
	}

	public static List<ConstraintTemplate> DECLARE_TEMPLATES = getDeclareTemplates();
	public static List<ConstraintTemplate> MINERFUL_TEMPLATES = getMinerfulTemplates();
	public static List<ConstraintTemplate> MP_TEMPLATES = getMpTemplates();
	public static List<ConstraintTemplate> TIMED_TEMPLATES = getTimedTemplates();
	
	//Originally only used for DeclareModelUtils.createTmpXmlModel
	public static boolean isUnaryTemplate(String t) {
		return t.startsWith("Existence") || t.startsWith("Absence") || t.startsWith("Init") || t.startsWith("Exactly") || t.startsWith("End");
	}

	private static List<ConstraintTemplate> getDeclareTemplates() {
		List<ConstraintTemplate> declareTemplates = new ArrayList<ConstraintTemplate>();
		for (ConstraintTemplate constraintTemplate : ConstraintTemplate.values()) {
			if (constraintTemplate != ConstraintTemplate.End && constraintTemplate != ConstraintTemplate.Not_Response && 
					constraintTemplate != ConstraintTemplate.Not_Precedence && constraintTemplate != ConstraintTemplate.Not_Chain_Response && 
					constraintTemplate != ConstraintTemplate.Not_Chain_Precedence && constraintTemplate != ConstraintTemplate.Not_Responded_Existence) {
				declareTemplates.add(constraintTemplate);
			}
		}
		return declareTemplates;
	}

	private static List<ConstraintTemplate> getMinerfulTemplates() {
		List<ConstraintTemplate> minerfulTemplates = new ArrayList<ConstraintTemplate>();
		for (ConstraintTemplate constraintTemplate : ConstraintTemplate.values()) {
			if(constraintTemplate != ConstraintTemplate.End && //End would technically work with MINERful, but excluding it for now because it is not supported by most other algorithms
					constraintTemplate != ConstraintTemplate.Exactly1 && constraintTemplate != ConstraintTemplate.Exactly2 && //MINERful does not discover Exactly constraints, only AtMost and AtLeast
					constraintTemplate != ConstraintTemplate.Not_Chain_Precedence &&
					constraintTemplate != ConstraintTemplate.Not_Chain_Response && constraintTemplate != ConstraintTemplate.Not_Precedence &&
					constraintTemplate != ConstraintTemplate.Not_Response && constraintTemplate != ConstraintTemplate.Not_Responded_Existence &&
					constraintTemplate != ConstraintTemplate.Choice && constraintTemplate != ConstraintTemplate.Exclusive_Choice) {
				minerfulTemplates.add(constraintTemplate);
			}
		}
		return minerfulTemplates;
	}

	private static List<ConstraintTemplate> getMpTemplates() {
		List<ConstraintTemplate> mpDeclareTemplates = new ArrayList<ConstraintTemplate>();
		for (ConstraintTemplate constraintTemplate : ConstraintTemplate.values()) {
			if (constraintTemplate == ConstraintTemplate.Response || constraintTemplate == ConstraintTemplate.Precedence ||
					constraintTemplate == ConstraintTemplate.Chain_Response || constraintTemplate == ConstraintTemplate.Chain_Precedence ||
					constraintTemplate == ConstraintTemplate.Alternate_Response || constraintTemplate == ConstraintTemplate.Alternate_Precedence ||
					constraintTemplate == ConstraintTemplate.Responded_Existence) {
				mpDeclareTemplates.add(constraintTemplate);
			}
		}
		return mpDeclareTemplates;
	}
	
	private static List<ConstraintTemplate> getTimedTemplates() {
		List<ConstraintTemplate> mpDeclareTemplates = new ArrayList<ConstraintTemplate>();
		for (ConstraintTemplate constraintTemplate : ConstraintTemplate.values()) {
			if (constraintTemplate == ConstraintTemplate.Existence || constraintTemplate == ConstraintTemplate.Existence2 || constraintTemplate == ConstraintTemplate.Existence3 ||
					constraintTemplate == ConstraintTemplate.Response || constraintTemplate == ConstraintTemplate.Precedence || constraintTemplate == ConstraintTemplate.Succession ||
					constraintTemplate == ConstraintTemplate.Chain_Response || constraintTemplate == ConstraintTemplate.Chain_Precedence || constraintTemplate == ConstraintTemplate.Chain_Succession ||
					constraintTemplate == ConstraintTemplate.Alternate_Response || constraintTemplate == ConstraintTemplate.Alternate_Precedence || constraintTemplate == ConstraintTemplate.Alternate_Succession ||
					constraintTemplate == ConstraintTemplate.Responded_Existence || constraintTemplate == ConstraintTemplate.CoExistence) {
				mpDeclareTemplates.add(constraintTemplate);
			}
		}
		return mpDeclareTemplates;
	}

	//Translates ConstraintTemplate object used in RuM into DeclareTemplate used by some methods
	public static DeclareTemplate getDeclareTemplate(ConstraintTemplate constraintTemplate) {
		return DeclareTemplate.valueOf(constraintTemplate.name());
	}

	//Translates DeclareTemplate used by some methods into ConstraintTemplate object used in RuM
	public static ConstraintTemplate getConstraintTemplate(DeclareTemplate declareTemplate) {
		return ConstraintTemplate.valueOf(declareTemplate.name());
	}

	public static ConstraintTemplate getConstraintTemplateFromMinerful(String minerfulTemplateName) {
		if (minerfulTemplateName.equals("AtMost1"))
			return ConstraintTemplate.Absence2;
		else if (minerfulTemplateName.equals("AtMost2"))
			return ConstraintTemplate.Absence3;
		
		else if (minerfulTemplateName.equals("AtLeast1"))
			return ConstraintTemplate.Existence;
		else if (minerfulTemplateName.equals("AtLeast2"))
			return ConstraintTemplate.Existence2;
		else if (minerfulTemplateName.equals("AtLeast3"))
			return ConstraintTemplate.Existence3;
		
		else if (minerfulTemplateName.equals("Exactly1"))
			return ConstraintTemplate.Exactly1;
		else if (minerfulTemplateName.equals("Exactly2"))
			return ConstraintTemplate.Exactly2;
		
		else if (minerfulTemplateName.equals("CoExistence"))
			return ConstraintTemplate.CoExistence;
		else if (minerfulTemplateName.equals("NotCoExistence"))
			return ConstraintTemplate.Not_CoExistence;
		
		else {
			minerfulTemplateName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(minerfulTemplateName), '_');
			return ConstraintTemplate.valueOf(minerfulTemplateName);
		}
	}

	public static String getMpTemplateName(ConstraintTemplate constraintTemplate) {
		return constraintTemplate.name().toLowerCase().replace("_", " ");
	}
	
	public static Set<ConstraintTemplate> getDeclareDiscoverySupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		for (ConstraintTemplate c : ConstraintTemplate.values())
			if (c != ConstraintTemplate.End)
				supported.add(c);
		
		return supported;
	}
	
	public static Set<ConstraintTemplate> getMinerfulDiscoverySupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		for (ConstraintTemplate c : ConstraintTemplate.values())
			if (c != ConstraintTemplate.Exactly1 && c != ConstraintTemplate.Exactly2
					&& c != ConstraintTemplate.Choice && c != ConstraintTemplate.Exclusive_Choice)
				supported.add(c);
		
		return supported;
	}
	
	public static Set<ConstraintTemplate> getAnalyzerConformanceSupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		for (ConstraintTemplate c : ConstraintTemplate.values())
			if (c != ConstraintTemplate.End)
				supported.add(c);
		
		return supported;
	}
	
	public static Set<ConstraintTemplate> getReplayerConformanceSupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		for (ConstraintTemplate c : ConstraintTemplate.values())
			if (c != ConstraintTemplate.End && c != ConstraintTemplate.Choice
					&& c != ConstraintTemplate.Not_Response && c != ConstraintTemplate.Not_Precedence
					&& c != ConstraintTemplate.Not_Chain_Response && c != ConstraintTemplate.Not_Chain_Precedence
					&& c != ConstraintTemplate.Not_Responded_Existence)
				supported.add(c);
		
		return supported;
	}
	
	public static Set<ConstraintTemplate> getAlloyGenerationSupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		supported.addAll( Arrays.asList(ConstraintTemplate.values()) );
		
		// Note that Alloy generation doesn't support natively the following templates:
		// Not Succession, Not Chain Succession and Not Co-Existence
		// but it derives them from the supported ones
		return supported;
	}
	
	public static Set<ConstraintTemplate> getMinerfulGenerationSupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		for (ConstraintTemplate c : ConstraintTemplate.values())
			if (c != ConstraintTemplate.Exclusive_Choice)
				supported.add(c);
		
		return supported;
	}
	
	public static Set<ConstraintTemplate> getAlloyMonitoringSupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		supported.addAll( Arrays.asList(ConstraintTemplate.values()) );
		
		// Note that Alloy monitoring doesn't support natively the following templates:
		// Not Succession, Not Chain Succession, Not Co-Existence
		// but it derives them from the supported ones
		return supported;
	}
	
	public static Set<ConstraintTemplate> getMobuconMonitoringSupportedConstraints() {
		Set<ConstraintTemplate> supported = new HashSet<>();
		
		supported.addAll( Arrays.asList(ConstraintTemplate.values()) );
		
		return supported;
	}
}
