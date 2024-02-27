package task;

import java.util.Set;

import javafx.concurrent.Task;
import model.TransitionNode;
import utils.ModelUtils;

public class FirstMergeTask extends Task<Set<TransitionNode>> {
	
	private Set<TransitionNode> fragmentMainTransitions;

	public FirstMergeTask(Set<TransitionNode> fragmentMainTransitions) {
		this.fragmentMainTransitions = fragmentMainTransitions;
	}
	
	@Override
	protected Set<TransitionNode> call() throws Exception {
		try {
			long taskStartTime = System.currentTimeMillis();
			System.out.println("Discovering constraint subsets started at: " + taskStartTime);
			
			//It should be ok to modify the input fragments directly, but creating new ones for now just to be safe and in case I need the input fragments
			Set<TransitionNode> firstMergeMainTransitions = ModelUtils.copyInitialFragments(fragmentMainTransitions);
			
			
			
			
			
			
			
			
			//Extending each copy of the initial fragments, only one "extension pass" for now, just to see how it works
			for (TransitionNode mainTransition : firstMergeMainTransitions) {
				//TODO
			}
			
			
			
			return firstMergeMainTransitions;
			
		} catch (Exception e) {
			System.err.println("Finding constraint subsets failed: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
}
