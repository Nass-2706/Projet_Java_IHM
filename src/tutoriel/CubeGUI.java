package tutoriel;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CubeGUI extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
		Parent content = FXMLLoader.load(getClass().getResource("gui2D.fxml"));
		primaryStage.setTitle("Cube 3D in GUI 2D");
		primaryStage.setScene(new Scene(content));
		primaryStage.show();
		
		} catch (IOException e) {
		e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}