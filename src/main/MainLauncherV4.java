package main;

public class MainLauncherV4 {
	public static void main(String[] args) {
		// required Helper Class that avoids Missing Components exception when starting runnable Jar
		// Alternative would be to add this to VM options: --module-path /path/to/JavaFX/lib --add-modules=javafx.controls
		
		// This would be the place for starting command line version based on arguments
		
		
		MainGuiV4.main(args);
	}
}
