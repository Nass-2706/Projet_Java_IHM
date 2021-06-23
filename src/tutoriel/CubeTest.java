package tutoriel;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

public class CubeTest extends Application {

    @Override
    public void start(Stage primaryStage) {

        //Create a Pane et graph scene root for the 3D content
        Group root3D = new Group();
        Pane pane3D = new Pane(root3D);

        //Create cube shape
        Box cube = new Box(1, 1, 1);
        Box cube2 = new Box(1,1,1);
        Box cube3 = new Box(1,1,1);
        cube2.setTranslateY(-1.5);
        cube3.setTranslateZ(2);
        cube3.setTranslateX(1.5);
        
        
        //Add an animation timer
        int rotationSpeed = 100;
    	final long startNanoTime = System.nanoTime();
    	new AnimationTimer() {
			public void handle(long currentNanoTime) {
				double t = (currentNanoTime - startNanoTime)/1000000000.0;
				cube3.setRotationAxis(new Point3D(0, 1, 0));
		        cube3.setRotate(rotationSpeed * t);
			}
    		
    	}.start();
        

        //Create Material
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.BLUE);
        blueMaterial.setSpecularColor(Color.BLUE);
        
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.GREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.RED);
        redMaterial.setSpecularColor(Color.RED);
        
        //Set it to the cube
        cube.setMaterial(blueMaterial);
        cube2.setMaterial(greenMaterial);
        cube3.setMaterial(redMaterial);

        //Add the cube to this node
        root3D.getChildren().add(cube);
        root3D.getChildren().add(cube2);
        root3D.getChildren().add(cube3);
        
        //Add a camera group
        PerspectiveCamera camera = new PerspectiveCamera(true);

        // Add point light
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-100);
        light.setTranslateY(-90);
        light.setTranslateZ(-120);
        light.getScope().addAll(root3D);
        root3D.getChildren().add(light);

        // Create scene
        Scene scene = new Scene(pane3D, 600, 600, true);
        scene.setCamera(camera);
        scene.setFill(Color.GREY);
        
        //Build camera manager
        new CameraManager(camera, pane3D, root3D);
        

        //Add the scene to the stage and show it
        primaryStage.setTitle("Cubes Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}