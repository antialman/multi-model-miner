package task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.DeclareMinerNoHierarc;
import org.processmining.plugins.declareminer.DeclareMinerNoRed;
import org.processmining.plugins.declareminer.DeclareMinerNoTrans;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.util.Configuration;
import org.processmining.plugins.declareminer.util.UnifiedLogger;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import javafx.concurrent.Task;
import utils.TemplateUtils;
import utils.LogUtils;
import utils.ConstraintTemplate;
import utils.ConstraintUtils;
import utils.DeclarePruningType;

public class DiscoveryTaskDeclare extends Task<DiscoveryTaskResult> {

	private File logFile;
	private int minSupport;
	private boolean vacuityDetection;
	private boolean considerLifecycle = true;
	private DeclarePruningType pruningType;
	private List<ConstraintTemplate> selectedTemplates;
	
	private XLog xlog;

	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}

	public void setMinSupport(int minSupport) {
		this.minSupport = minSupport;
	}

	public void setVacuityDetection(boolean vacuityDetection) {
		this.vacuityDetection = vacuityDetection;
	}

	public void setConsiderLifecycle(boolean considerLifecycle) {
		this.considerLifecycle = considerLifecycle;
	}

	public void setPruningType(DeclarePruningType pruningType) {
		this.pruningType = pruningType;
	}

	public void setSelectedTemplates(List<ConstraintTemplate> selectedTemplates) {
		this.selectedTemplates = selectedTemplates;
	}

	@Override
	protected DiscoveryTaskResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering Declare model started at: " + taskStartTime);

			Configuration configuration = new Configuration();
			xlog = LogUtils.convertToXlog(logFile.getAbsolutePath());
			configuration.log = xlog;

			DeclareMinerInput input = new DeclareMinerInput();
			input.setMinSupport(minSupport);
			input.setAlpha(vacuityDetection ? 0 : 100);

			HashSet<DeclareTemplate> selectedDeclareTemplates = new HashSet<>();
			selectedTemplates.forEach(item -> selectedDeclareTemplates.add(TemplateUtils.getDeclareTemplate(item)));
			input.setSelectedDeclareTemplateSet(selectedDeclareTemplates);

			Set<DeclarePerspective> persp_set = Collections.singleton(DeclarePerspective.valueOf("Control_Flow"));
			input.setDeclarePerspectiveSet(persp_set);

			Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<>(); //This seems to be needed for some kind of internal mapping in Declare Miner (template name -> condec name)?
			for (ConstraintTemplate constraintTemplate : selectedTemplates) {
				DeclareTemplate declareTemplate = TemplateUtils.getDeclareTemplate(constraintTemplate);

				String templateNameString;
				if (declareTemplate == DeclareTemplate.Choice)
					templateNameString = "choice 1 of 2";
				else
					templateNameString = declareTemplate.name().replaceAll("_", " ").toLowerCase();

				templateNameStringDeclareTemplateMap.put(templateNameString, declareTemplate);
			}

			input.setDeclareTemplateConstraintTemplateMap(DeclareMiner.readConstraintTemplates(templateNameStringDeclareTemplateMap));
			input.setMapTemplateConfiguration(MapTemplateConfiguration.valueOf("DiscoverProvidedTemplatesAcrossAllActivitesInLog"));

			Set<AprioriKnowledgeBasedCriteria> apriori_set;
			if (considerLifecycle)
				apriori_set = new HashSet<>(Collections.singleton(AprioriKnowledgeBasedCriteria.valueOf("AllActivitiesWithEventTypes")));
			else
				apriori_set = new HashSet<>(Collections.singleton(AprioriKnowledgeBasedCriteria.valueOf("AllActivitiesIgnoringEventTypes")));

			input.setAprioriKnowledgeBasedCriteriaSet(apriori_set);
			input.setVerbose(false);
			input.setThreadNumber(4);

			configuration.input = input;

			UnifiedLogger.unified_log_path = "./output/all_results.log";
			UnifiedLogger.unified_memory_log_path = "./output/mem.log";
			configuration.setUnifiedLoggerPrunerType("replayers"); // will be obsolete after testing is done

			//Run model discovery
			DeclareMinerOutput declareMinerOutput;
			switch (pruningType) {
			case ALL_REDUCTIONS:
				declareMinerOutput = DeclareMiner.mineDeclareConstraints(null, configuration.log, configuration.input);
				break;
			case HIERARCHY_BASED:
				declareMinerOutput = DeclareMinerNoTrans.mineDeclareConstraints(null, configuration.log, configuration.input);
				break;
			case TRANSITIVE_CLOSURE:
				declareMinerOutput = DeclareMinerNoHierarc.mineDeclareConstraints(null, configuration.log, configuration.input);
				break;
			case NONE:
				declareMinerOutput = DeclareMinerNoRed.mineDeclareConstraints(null, configuration.log, configuration.input);
				break;
			default:
				throw new Exception("Unhandled pruning type: " + pruningType);
			}


			// Building a map for an easy identification in constraints discovery
			Map<String, DiscoveredActivity> activityMap = new HashMap<>();
			for (DiscoveredActivity act : ConstraintUtils.getAllActivitiesFromLog(xlog, considerLifecycle) ) {
				String identifier = act.getActivityFullName();
				activityMap.put(identifier, act);
			}

			// Discovered constraints
			List<DiscoveredConstraint> discoveredConstraints = new ArrayList<>();

			declareMinerOutput.getVisiblesupportRule().forEach((key, constraintSupport) -> {
				ConstraintTemplate template = TemplateUtils.getConstraintTemplate(declareMinerOutput.getTemplate().get(key));
				List<String> parametersList = declareMinerOutput.getVisibleConstraintParametersMap().get(key);

				DiscoveredActivity activationActivity = activityMap.get(parametersList.get(0));
				DiscoveredActivity targetActivity = null;
				
				if (template.getIsBinary()) {
					if (template.getReverseActivationTarget()) {
						activationActivity = activityMap.get(parametersList.get(1));
						targetActivity = activityMap.get(parametersList.get(0));
					} else {
						targetActivity = activityMap.get(parametersList.get(1));
					}
				}
				DiscoveredConstraint constraint = new DiscoveredConstraint(template, activationActivity, targetActivity, constraintSupport);

				discoveredConstraints.add(constraint);
			});
			
			//This should probably be changed, but not important at the moment
			List<DiscoveredActivity> discoveredActivities = activityMap.values().stream().collect(Collectors.toList());


			// Result object of the process discovery task
			DiscoveryTaskResult discoveryTaskResult = new DiscoveryTaskResult();
			discoveryTaskResult.setActivities(discoveredActivities);
			discoveryTaskResult.setConstraints(discoveredConstraints);


			System.out.println("Discovering Declare model finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));

			return discoveryTaskResult;


		} catch (Exception e) {
			System.err.println("Discovering Declare model failed: " + e.getMessage());
			System.err.println(e);
			throw e;
		}
	}

}
