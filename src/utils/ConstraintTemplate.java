package utils;

import java.util.HashMap;
import java.util.Map;

public enum ConstraintTemplate {
	Absence("Absence", false, false, false),
	Absence2("Absence2", false, false, false), //Equal to AtMost1 in Minerful
	Absence3("Absence3", false, false, false), //Equal to AtMost2 in Minerful
	End("End", false, false, false),
	Exactly1("Exactly1", false, false, false),
	Exactly2("Exactly2", false, false, false),
	Existence("Existence", false, false, false), //Equal to AtLeast1 in Minerful
	Existence2("Existence2", false, false, false), //Equal to AtLeast2 in Minerful
	Existence3("Existence3", false, false, false), //Equal to AtLeast3 in Minerful
	Init("Init", false, false, false),
	Alternate_Precedence("Alternate Precedence", true, false, true),
	Alternate_Response("Alternate Response", true, false, false),
	Alternate_Succession("Alternate Succession", true, false, false),
	Chain_Precedence("Chain Precedence", true, false, true),
	Chain_Response("Chain Response", true, false, false),
	Chain_Succession("Chain Succession", true, false, false),
	Choice("Choice", true, false, false),
	CoExistence("Co-Existence", true, false, false),
	Exclusive_Choice("Exclusive Choice", true, false, false),
	Precedence("Precedence", true, false, true),
	Responded_Existence("Responded Existence", true, false, false),
	Response("Response", true, false, false),
	Succession("Succession", true, false, false),
	Not_Chain_Precedence("Not Chain Precedence", true, true, true),
	Not_Chain_Response("Not Chain Response", true, true, false),
	Not_Chain_Succession("Not Chain Succession", true, true, false),
	Not_CoExistence("Not Co-Existence", true, true, false),
	Not_Precedence("Not Precedence", true, true, true),
	Not_Responded_Existence("Not Responded Existence", true, true, false),
	Not_Response("Not Response", true, true, false),
	Not_Succession("Not Succession", true, true, false);

	private String templateName;
	private boolean isBinary;
	private boolean isNegative;
	private boolean reverseActivationTarget;

	private String displayText;

	//Reverse-lookup map for getting template object based on template name string
	private static final Map<String, ConstraintTemplate> templateNameToTemplate = new HashMap<String, ConstraintTemplate>();

	static {
		for (ConstraintTemplate constraintTemplate : ConstraintTemplate.values()) {
			templateNameToTemplate.put(constraintTemplate.getTemplateName(), constraintTemplate);
		}
	}

	private ConstraintTemplate(String templateName, boolean isBinary, boolean isNegative, boolean reverseActivationTarget) {
		this.templateName = templateName;
		this.isBinary = isBinary;
		this.isNegative = isNegative;
		this.reverseActivationTarget = reverseActivationTarget;

		if (isBinary) {
			this.displayText = templateName + (reverseActivationTarget ? "[B, A]" : "[A, B]");
		} else {
			this.displayText = templateName + "[A]";
		}
	}

	public String getTemplateName() {
		return templateName;
	}

	public boolean getIsBinary() {
		return isBinary;
	}

	public boolean getIsNegative() {
		return isNegative;
	}

	public boolean getReverseActivationTarget() {
		return reverseActivationTarget;
	}

	public String getDisplayText() {
		return displayText;
	}

	public static ConstraintTemplate getByTemplateName(String templateName) {
		return templateNameToTemplate.get(templateName);
	}
}
