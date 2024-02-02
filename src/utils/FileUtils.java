package utils;

import java.io.File;
import java.util.Arrays;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileUtils {

	private static File previousDirectory;

	//Extension filters for logs
	private static ExtensionFilter eventlogExtensionFilter = new ExtensionFilter("Log file", Arrays.asList("*.xes", "*.mxml"));



	// Private constructor to avoid unnecessary instantiation of the class
	private FileUtils() {
	}
	
	
	public static File showLogOpenDialog(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		if (previousDirectory != null && previousDirectory.exists()) {
			fileChooser.setInitialDirectory(previousDirectory);
		}

		fileChooser.getExtensionFilters().add(eventlogExtensionFilter);
		File chosenFile = fileChooser.showOpenDialog(stage);

		if (chosenFile != null) { // If true then the user just closed the dialog without choosing a file
			previousDirectory = chosenFile.getParentFile();
		}

		return chosenFile;
	}
	
	

}
