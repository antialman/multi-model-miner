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
				
				//Followers and preceders of the discovered activity
				for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
					if (discoveredConstraint.getActivationActivity() == discoveredActivity) {
						if (!discoveredConstraint.getTemplate().getReverseActivationTarget()) {
							activityRelations.addFollowerActivity(discoveredConstraint.getTargetActivity());
							activityRelations.addConstraintToFollower(discoveredConstraint);
						} else {
							activityRelations.addPrecederActivity(discoveredConstraint.getTargetActivity());
							activityRelations.addConstraintFromPreceder(discoveredConstraint);
						}
					} else if (discoveredConstraint.getTargetActivity() == discoveredActivity) {
						if (!discoveredConstraint.getTemplate().getReverseActivationTarget()) {
							activityRelations.addPrecederActivity(discoveredConstraint.getActivationActivity());
							activityRelations.addConstraintFromPreceder(discoveredConstraint);
						} else {
							activityRelations.addFollowerActivity(discoveredConstraint.getActivationActivity());
							activityRelations.addConstraintToFollower(discoveredConstraint);
						}
					}
				}
				
				//Constraints among followers/preceders of the discovered activity
				for (DiscoveredConstraint discoveredConstraint : declareDiscoveryResult.getConstraints()) {
					if (activityRelations.getAllFollowerActivities().contains(discoveredConstraint.getActivationActivity()) && activityRelations.getAllFollowerActivities().contains(discoveredConstraint.getTargetActivity())) {
						activityRelations.addConstraintAmongFollowers(discoveredConstraint);
					} //Technically, the same constraint should not appear among both followers and preceders, however there is a slim chance this will change when finalizing handling of loops
					if (activityRelations.getAllPrecederActivities().contains(discoveredConstraint.getActivationActivity()) && activityRelations.getAllPrecederActivities().contains(discoveredConstraint.getTargetActivity())) {
						activityRelations.addConstraintAmongPreceders(discoveredConstraint);
					}
				}
				
				//Potential closest follower activities based on constraints (i.e., which activities, if they occur, must occur the earliest among all the followers)
				for (DiscoveredActivity candidateActivity : activityRelations.getAllFollowerActivities()) {
					boolean potentiallyClosest = true;
					for (DiscoveredConstraint followerConstraint : activityRelations.getConstraintsAmongFollowers()) {
						//Exclusion of response here results in considering all activities that can be executed next as temporally closest
						//Inclusion of response here would result in considering only the activities for which an execution decision has to be made first as temporally closest
						//TODO: Incorporate both into the post processing task
						if (followerConstraint.getTemplate() == ConstraintTemplate.Succession || followerConstraint.getTemplate() == ConstraintTemplate.Alternate_Succession) {
							if (followerConstraint.getTargetActivity() == candidateActivity) {
								//If this activity is the target of a succession then it cannot be the earliest among all the followers
								potentiallyClosest = false;
								break;
							}
						} else if (followerConstraint.getTemplate() == ConstraintTemplate.Precedence || followerConstraint.getTemplate() == ConstraintTemplate.Alternate_Precedence) {
							if (followerConstraint.getActivationActivity() == candidateActivity) {
								//If this activity is the activation of a precedence then it cannot be the earliest among all the followers
								potentiallyClosest = false;
								break;
							}
						}
					}
					if (potentiallyClosest) {
						activityRelations.addPotentialClosestFollower(candidateActivity);
					}
				}
				
				//Potential closest preceder activities based on constraints (i.e., which activities, if they occur, must occur the latest among all the preceders)
				for (DiscoveredActivity candidateActivity : activityRelations.getAllPrecederActivities()) {
					boolean potentiallyClosest = true;
					for (DiscoveredConstraint precederConstraint : activityRelations.getConstraintsAmongPreceders()) {
						if (precederConstraint.getTemplate() == ConstraintTemplate.Succession || precederConstraint.getTemplate() == ConstraintTemplate.Alternate_Succession) {
							if (precederConstraint.getActivationActivity() == candidateActivity) {
								//If this activity is the activation of a succession then it cannot be the latest among the preceders
								potentiallyClosest = false;
								break;
							}
						} else if (precederConstraint.getTemplate() == ConstraintTemplate.Response || precederConstraint.getTemplate() == ConstraintTemplate.Alternate_Response) {
							if (precederConstraint.getActivationActivity() == candidateActivity) {
								//If this activity is the activation of a response then it cannot be the latest among the preceders
								potentiallyClosest = false;
								break;
							}
						}
					}
					if (potentiallyClosest) {
						activityRelations.addPotentialClosestPreceder(candidateActivity);
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
