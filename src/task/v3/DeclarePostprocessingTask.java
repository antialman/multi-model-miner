package task.v3;

import data.DiscoveredActivity;
import data.DiscoveredConstraint;
import data.v3.ActivityRelationsContainer;
import javafx.concurrent.Task;
import task.DeclareDiscoveryResult;
import utils.ConstraintTemplate;

public class DeclarePostprocessingTask extends Task<DeclarePostprocessingResult> {

	private DeclareDiscoveryResult declareDiscoveryResult;


	public void setDeclareDiscoveryResult(DeclareDiscoveryResult declareDiscoveryResult) {
		this.declareDiscoveryResult = declareDiscoveryResult;
	}


	@Override
	protected DeclarePostprocessingResult call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Declare post-processing started at: " + taskStartTime);

			DeclarePostprocessingResult declarePostprocessingResult = new DeclarePostprocessingResult();

			for (DiscoveredActivity discoveredActivity : declareDiscoveryResult.getActivities()) {
				ActivityRelationsContainer activityRelations = new ActivityRelationsContainer(discoveredActivity);
				declarePostprocessingResult.addActivityRelationsContainer(discoveredActivity, activityRelations);

				//Followers and predecessors of the discovered activity
				for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
					if (discoveredConstraint.getActivationActivity() == discoveredActivity) {
						if (!discoveredConstraint.getTemplate().getReverseActivationTarget()) {
							activityRelations.addFollowerActivity(discoveredConstraint.getTargetActivity());
							activityRelations.addConstraintToFollower(discoveredConstraint);
						} else {
							activityRelations.addPredecessorActivity(discoveredConstraint.getTargetActivity());
							activityRelations.addConstraintFromPredecessor(discoveredConstraint);
						}
					} else if (discoveredConstraint.getTargetActivity() == discoveredActivity) {
						if (!discoveredConstraint.getTemplate().getReverseActivationTarget()) {
							activityRelations.addPredecessorActivity(discoveredConstraint.getActivationActivity());
							activityRelations.addConstraintFromPredecessor(discoveredConstraint);
						} else {
							activityRelations.addFollowerActivity(discoveredConstraint.getActivationActivity());
							activityRelations.addConstraintToFollower(discoveredConstraint);
						}
					}
				}

				//Constraints among followers/predecessors of the discovered activity
				for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
					if (activityRelations.getAllFollowerActivities().contains(discoveredConstraint.getActivationActivity()) && activityRelations.getAllFollowerActivities().contains(discoveredConstraint.getTargetActivity())) {
						activityRelations.addConstraintAmongFollowers(discoveredConstraint);
					} //Technically, the same constraint should not appear among both followers and predecessors, however there is a slim chance this will change when finalizing handling of loops
					if (activityRelations.getAllPredecessorActivities().contains(discoveredConstraint.getActivationActivity()) && activityRelations.getAllPredecessorActivities().contains(discoveredConstraint.getTargetActivity())) {
						activityRelations.addConstraintAmongPredecessors(discoveredConstraint);
					}
				}

				//Potential closest follower activities based on constraints (i.e., which activities, if they occur, must occur the earliest among all the followers)
				for (DiscoveredActivity candidateActivity : activityRelations.getAllFollowerActivities()) {
					boolean closestExecution = true;
					boolean closestDecision = true;
					for (DiscoveredConstraint followerConstraint : activityRelations.getConstraintsAmongFollowers()) {
						if (followerConstraint.getTemplate() == ConstraintTemplate.Succession || followerConstraint.getTemplate() == ConstraintTemplate.Alternate_Succession) {
							if (followerConstraint.getTargetActivity() == candidateActivity) {
								//If this activity is the target of a succession then it cannot be the earliest among all the followers
								closestExecution = false;
								closestDecision = false;
								break;
							}
						} else if (followerConstraint.getTemplate() == ConstraintTemplate.Precedence || followerConstraint.getTemplate() == ConstraintTemplate.Alternate_Precedence) {
							if (followerConstraint.getActivationActivity() == candidateActivity) {
								//If this activity is the activation of a precedence then it cannot be the earliest among all the followers
								closestExecution = false;
								closestDecision = false;
								break;
							}
						} else if (followerConstraint.getTemplate() == ConstraintTemplate.Response || followerConstraint.getTemplate() == ConstraintTemplate.Alternate_Response) {
							if (followerConstraint.getTargetActivity() == candidateActivity) {
								//If this activity is the target of a response then it can be the earliest among the followers
								//...but executing it the earliest among the followers requires first deciding to not execute the activation of that response (in the same loop iteration)
								closestDecision = false; //Cannot break iteration here because there might also be a Succession or Precedence among other constraints 
							}
						}
					}

					//					if (closestDecision) {
					//						for (DiscoveredConstraint discoveredConstraint : activityRelations.getConstraintsFromPredecessors()) {
					//							//If the discoveredActivity activates (Alternate) Succession/Response then the candidateActivity is mandatory
					//							if (!(discoveredConstraint.getActivationActivity() == discoveredActivity && (discoveredConstraint.getTemplate() == ConstraintTemplate.Succession || discoveredConstraint.getTemplate() == ConstraintTemplate.Alternate_Succession || discoveredConstraint.getTemplate() == ConstraintTemplate.Response || discoveredConstraint.getTemplate() == ConstraintTemplate.Alternate_Response))) {
					//								closestDecision = false;
					//							}
					//						}
					//					}

					if (closestExecution) {
						activityRelations.addPotentialNextActivity(candidateActivity);
					}
					if (closestDecision) {
						activityRelations.addPotentialNextDecision(candidateActivity);
					}
				}

				//Potential closest predecessor activities based on constraints (i.e., which activities, if they occur, must occur the latest among all the predecessors)
				for (DiscoveredActivity candidateActivity : activityRelations.getAllPredecessorActivities()) {
					boolean closestExecution = true;
					boolean closestDecision = true;
					for (DiscoveredConstraint predecessorConstraint : activityRelations.getConstraintsAmongPredecessors()) {
						if (predecessorConstraint.getTemplate() == ConstraintTemplate.Succession || predecessorConstraint.getTemplate() == ConstraintTemplate.Alternate_Succession) {
							if (predecessorConstraint.getActivationActivity() == candidateActivity) {
								//If this activity is the activation of a succession or response then it cannot be the latest among the predecessors
								closestExecution = false;
								closestDecision = false;
								break;
							}
						}
						else if (predecessorConstraint.getTemplate() == ConstraintTemplate.Response || predecessorConstraint.getTemplate() == ConstraintTemplate.Alternate_Response) {
							if (predecessorConstraint.getActivationActivity() == candidateActivity) {
								//If this activity is the activation of a succession or response then it cannot be the latest among the predecessors
								closestExecution = false;
								closestDecision = false;
								break;
							}
						} else if (predecessorConstraint.getTemplate() == ConstraintTemplate.Precedence || predecessorConstraint.getTemplate() == ConstraintTemplate.Alternate_Precedence) {
							if (predecessorConstraint.getTargetActivity() == candidateActivity) {
								//If this activity is the target of a precedence then it can be the latest among the predecessors
								//...but executing it the latest among predecessors requires deciding to skip the activation of that precedence afterwards (in the same loop iteration)
								closestDecision = false; //Cannot break iteration here because there might also be a Succession or Response among other constraints
							}
						}
					}
					if (closestExecution) {
						activityRelations.addPotentialPrevActivity(candidateActivity);
					}
					if (closestDecision) {
						activityRelations.addPotentialPrevDecision(candidateActivity);
					}
				}
			}


			System.out.println("Declare post-processing finished at: " + taskStartTime + " - total time: " + (System.currentTimeMillis() - taskStartTime));

			return declarePostprocessingResult;
		} catch (Exception e) {
			System.err.println("Declare post-processing failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
