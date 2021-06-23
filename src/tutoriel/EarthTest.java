package tutoriel;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class EarthTest extends Application {
	
    @Override
    public void start(Stage primaryStage) {
    	
    	try {
    		Parent content = FXMLLoader.load(getClass().getResource("projetIHM-2.fxml"));
    		primaryStage.setTitle("OBIS 3D");
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
