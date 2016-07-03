package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Scanner;
import java.util.prefs.Preferences;
import app.view.rootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
/**
 * Simple Text Editor with few editing options like adding and removing lines numbers, 
 * removing empty lines, removing lines contained chars other than a-z only.
 * Program may be useful for cleaning messy files or so.
 * You can save your progress to .txt file and you can read from any .txt too.
 * Application also remembers last open/save file directory, and will inform if you want to close 
 * or open file without saving progress.
 * 
 * @author Pablitto77
*/
public class Main extends Application {

	private Stage mainWindow = new Stage();
	private BorderPane rootLayout = new BorderPane();
	private static final String FILE_PATH = "filePath";
	private boolean mainIsChanged;
	rootLayoutController controller;

	@Override
	public void start(Stage primaryStage) {
		try {
			mainWindow = primaryStage;
			mainWindow.setTitle("Simple Text Editor");
			mainWindow.getIcons().add(
					new Image("/app/app.sources/images/icon.png"));
			initRootLayout();
			setFilePath(null);

		} catch (Exception e) {
			showErrorDialog(e.toString());
		}
	}

	public void initRootLayout() {
		String path = "/app/view/rootLayout.fxml";
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(this.getClass().getResource(path));
		try {
			rootLayout = (BorderPane) loader.load();
			Scene scene = new Scene(rootLayout);
			mainWindow.setScene(scene);
			mainWindow.setResizable(true);
			mainWindow.show();

			controller = loader.getController();
			controller.setMainApp(this);

			/**
			 * Save Confirmation dialog appears if text has been changed and file has
			 * not been saved
			 */
			mainWindow.setOnCloseRequest(e -> {
				mainIsChanged = controller.getIsChanged();
				if (mainIsChanged == true) {
					boolean saveOrNot = showChangesConfirmation();
					if (saveOrNot == true) {
						e.consume();
						showSaveChooser();
					}
					mainWindow.close();
					System.exit(0);
				} else {
					mainWindow.close();
					System.exit(0);
				}
			});
		} catch (Exception e) {
			showErrorDialog(e.toString());
		}
	}

	public Stage getPrimaryStage() {
		return mainWindow;
	}

	public void setPrimaryStage(Stage stage) {
		mainWindow = stage;
	}

	/**
	 * Method remembers last opened/saved  file's path  
	 * 
	 * @param file - last opened/saved file, reset remembering if param is null 
	 */

	public void setFilePath(File file) {
		Preferences pref = Preferences.userNodeForPackage(this.getClass());
		if (file != null) {
			pref.put(FILE_PATH, file.getPath());

			// Update the stage title.
			mainWindow.setTitle("File name - " + file.getName());
		} else {
			pref.remove(FILE_PATH);
			// Update the stage title.
			mainWindow.setTitle("File");
		}
	}
	
	/**
	 * Method is called to ensure if file was previously used (save/open)
	 * 
	 * @return File if path has been remembered, if not return null 
	 */

	public File getFilePath() {
		Preferences pref = Preferences.userNodeForPackage(this.getClass());
		String path = pref.get(FILE_PATH, null);
		if (path != null) {
			return new File(path);
		} else
			return null;
	}
	/**
	 * Saves string to file with Windows-1250 encoding
	 * @param content -String to save
	 * @param file - File to save to
	 */

	public void saveToFile(String content, File file) {
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			BufferedWriter buffWritter = new BufferedWriter(
					new OutputStreamWriter(outStream, "Windows-1250"));
			Scanner scanner = new Scanner(content);
			setFilePath(file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				buffWritter.write(line);
				buffWritter.write(System.lineSeparator());
			}
			buffWritter.close();
			scanner.close();
		} catch (IOException e) {
			showErrorDialog(e.toString());
		}
	}
	/**
	 * Reads File line by line
	 * 
	 * @return String read file
	 */
	public String readFromFile(File file) {
		String line = "";
		StringBuilder builder = new StringBuilder();
		try {
			FileInputStream reader = new FileInputStream(file);
			BufferedReader buffReader = new BufferedReader(
					new InputStreamReader(reader, "Windows-1250"));

			while ((line = buffReader.readLine()) != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
			}
			line = builder.toString();
			buffReader.close();
			setFilePath(file);
			return line;
		} catch (UnsupportedEncodingException e1) {
			showErrorDialog(e1.toString());
		} catch (IOException e) {
			showErrorDialog(e.toString());
		}
		setFilePath(file);
		return line;
	}

	/**
	 * last opened/saved folder directory
	 * 
	 * @return String directory or C:/ if NullPointer has been occurred
	 */
	public String getDirectory() {
		File filea = getFilePath();
		String newPath = "";
		try {
			String oldPath = filea.getAbsolutePath();
			String[] arr = oldPath.split("\\\\");
			for (int i = 0; i < arr.length - 1; i++) {
				newPath += arr[i] + "/";
			}
		} catch (NullPointerException exc) {
			return "C:/";
		}
		return newPath;
	}

	public Stage getPrimamryStage() {
		return mainWindow;
	}

	public boolean showChangesConfirmation() {

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText("Please confirm");
		alert.setContentText("Do you want to save changes ?");
		alert.initOwner(mainWindow);

		Optional<ButtonType> buttonTaker = alert.showAndWait();
		if (buttonTaker.get() == ButtonType.OK)
			return true;
		else
			return false;
	}

	public void showErrorDialog(String error) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Some error has been occured");
		alert.setContentText(error);
		alert.showAndWait();

	}

	public void showSaveChooser() {
		FileChooser chooser = new FileChooser();
		FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
				"Txt files ", "*.txt");
		chooser.getExtensionFilters().add(filter);
		chooser.setInitialDirectory(new File(getDirectory()));
		File fileX = chooser.showSaveDialog(mainWindow);

		String content = controller.getText();

		if (fileX != null) {
			// Make sure it has the correct extension
			if (!fileX.getPath().endsWith(".txt")) {
				fileX = new File(fileX.getPath() + ".txt");
			}
			saveToFile(content, fileX);
		}
	}
	public static void main(String[] args) {
		launch(args);
	}
}
