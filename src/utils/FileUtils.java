package utils;

import java.io.File;
import java.util.Arrays;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileUtils {

	private static File previousDirectory;
	private static File previousDeclFile;

	//Extension filters for files
	private static ExtensionFilter eventlogExtensionFilter = new ExtensionFilter("Log file", Arrays.asList("*.xes", "*.mxml"));
	private static ExtensionFilter declExtensionFilter = new ExtensionFilter("Declare model", Arrays.asList("*.decl"));



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
	
	
	public static File showDeclSaveDialog(Stage stage) {
		FileChooser fileChooser = new FileChooser();
		if (previousDeclFile != null) {
            if (previousDeclFile.getParentFile().exists()) {
                fileChooser.setInitialDirectory(previousDeclFile.getParentFile());
            }
            fileChooser.setInitialFileName(previousDeclFile.getName());
        } else if (previousDirectory != null && previousDirectory.exists()) {
            fileChooser.setInitialDirectory(previousDirectory);
        }
		
		fileChooser.getExtensionFilters().add(declExtensionFilter);
		File chosenFile = fileChooser.showSaveDialog(stage);
		
		if (chosenFile != null) {
			previousDirectory = chosenFile.getParentFile();
			previousDeclFile = chosenFile;
		}
		
		return chosenFile;
	}
	

}
