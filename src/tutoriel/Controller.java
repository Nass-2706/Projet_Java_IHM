package tutoriel;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import application.Espece;
import application.Signalement;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Controller implements Initializable{
	
	@FXML
	private Pane pane3D;
	
	@FXML
	private Label Infos;
	
	@FXML
	private DatePicker debut;
	
	@FXML
	private DatePicker fin;
	
	@FXML
	private ListView<String> ResultatRecherche;
	
	@FXML
	private TextField Recherche;
	
	@FXML
	private TextField geohash;
	
	@FXML
	private TextField pas;
	
	@FXML
	private TextField precision;
	
	@FXML
	private Label Liste;
	
	
	private static final float TEXTURE_LAT_OFFSET = -0.2f;
    private static final float TEXTURE_LON_OFFSET = 2.8f;
    
	@Override
	public void initialize(URL location, ResourceBundle ressources) {
		//Create a Pane et graph scene root for the 3D content
        Group root3D = new Group();
       
        // Load geometry
        ObjModelImporter objImporter = new ObjModelImporter();
        try {
        	URL modelUrl = this.getClass().getResource("Earth/copy/earth.obj");
        	objImporter.read(modelUrl);
        } catch (ImportException e) {
        	//handle exception
        	System.out.println(e.getMessage());
        }
        MeshView[] meshViews = objImporter.getImport();
        Group earth = new Group(meshViews);
        root3D.getChildren().add(earth);

        // Add a camera group
        PerspectiveCamera camera = new PerspectiveCamera(true);
        new CameraManager(camera, pane3D, root3D);

        // Add point light
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-180);
        light.setTranslateY(-90);
        light.setTranslateZ(-120);
        light.getScope().addAll(root3D);
        root3D.getChildren().add(light);

        // Add ambient light
        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        ambientLight.getScope().addAll(root3D);
        root3D.getChildren().add(ambientLight);

        // Create scene
        SubScene subscene = new SubScene(root3D,700,700 ,true, SceneAntialiasing.BALANCED);
        subscene.setCamera(camera);
        pane3D.getChildren().addAll(subscene);
        
        pane3D.addEventHandler(MouseEvent.ANY, event -> {
        	if (event.getEventType() == MouseEvent.MOUSE_PRESSED && event.isControlDown()) {
        		PickResult pickResult = event.getPickResult();
        		
        		Point3D point = pickResult.getIntersectedPoint();
        		
        		Point2D nouveauPoint = SpaceCoordToGeoCoord(point);
        		
        		Location loc = new Location("selectedGeoHash", nouveauPoint.getX(), nouveauPoint.getY());
        		
        		String b = Character.toString(GeoHashHelper.getGeohash(loc).charAt(0)) + GeoHashHelper.getGeohash(loc).charAt(1) + GeoHashHelper.getGeohash(loc).charAt(2);
        		OccurenceGeometry(b);
        		System.out.println(b);
        	}
         });
        
        //ici on récupere ce qui est écrit dans la barre de recherche quand il appuie sur entrer
        Recherche.setOnAction(new EventHandler<ActionEvent>() {
        	  public void handle(ActionEvent event) {
        		  ClearChild(earth);
        		  try {
        			  	Espece e = Taxon(Recherche.getText());
        			  	OccurenceGlobale(e, e.scientificName);
              			Liste.setText("Scientific name : " + e.scientificName + "\n" + "acceptedNameUsageID : " + e.acceptedNameUsageID + "\n" + "id : " + e.id + "\n" + "rank : " +
        	                e.rank + "\n" + "kingdom : " + e.kingdom + "\n" + "phylum : " + e.phylum + "\n" + "Nombre de signalements : " + e.signalements.size());
              			double lat1;
              			double lon1;
              			Point3D point1 = new Point3D(0, 0,0);
              			for (int h = 0; h< e.signalements.size(); h++) {
              				lat1 = e.signalements.get(h).latitude;
              				lon1 = e.signalements.get(h).longitude;
              				point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
              				displayTown(earth, "paris", point1, Color.GREEN);
              			}
        		  } catch (org.json.JSONException exception) {
        	        	//handle exception
        	        	Liste.setText("Mauvais nom, " + "\n" + "veuillez en entrer un valide");
        	       }
        		  }
        		});
        
        pas.setOnAction(new EventHandler<ActionEvent>() {
      	  public void handle(ActionEvent event) {
      		  ClearChild(earth);
      		  
      		  LocalDate d = debut.getValue();
      		  LocalDate f = fin.getValue();
      		  
      		  int de = d.getYear();
      		  int fi = f.getYear();
      		  
      		  int a = de;
      		  
      		  int p = Integer.parseInt(pas.getText());
      		  int b = de;
      		  
      		  Espece e = Taxon(Recherche.getText());
      		  
      		  if (p >= 1) {
      			  while (b<fi) {
      				  
      				ClearChild(earth);
      				e.signalements.clear();
      				
      				if(b + p > fi)
            		{
            			b = fi;
            		}
            		else
            		{
            			b = b + p;
            		}
      				
      				String start = a + "-01-01";
            		String end = b + "-01-01";
            		
      				OccurencePrecisionTime2(e, e.scientificName,  Integer.parseInt(precision.getText()), start, end);
      				
              		System.out.println(start);
              		System.out.println(end);
              		System.out.println(e.signalements.size());
      				
      				double lat1;
              		double lon1;
              		
              		Point3D point1 = new Point3D(0, 0,0);
              		
              		for (int h = 0; h< e.signalements.size(); h++) {
              			lat1 = e.signalements.get(h).latitude;
              			lon1 = e.signalements.get(h).longitude;
              			point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
              			displayTown(earth, "paris", point1, Color.YELLOW);
              		}
              		
              		try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
      			  }
      		  	}
      		  }
      		});
        
        
        geohash.setOnAction(new EventHandler<ActionEvent>() {
      	  public void handle(ActionEvent event) {
      		  ClearChild(earth);
      		  
      		  if (Recherche.getText() == "") 
      		  {
      			OccurenceGeometry(geohash.getText());
      			Liste.setText("");
      		  }
      		  else 
      		  {
      			Espece e = Taxon(Recherche.getText());
        		  OccurenceGlobaleGeometry(e, Recherche.getText(), geohash.getText());
            		Liste.setText("Scientific name : " + e.scientificName + "\n" + "acceptedNameUsageID : " + e.acceptedNameUsageID + "\n" + "id : " + e.id + "\n" + "rank : " +
      	                e.rank + "\n" + "kingdom : " + e.kingdom + "\n" + "phylum : " + e.phylum + "\n" + "Nombre de signalements : " + e.signalements.size());
            		
            		double lat1;
            		double lon1;
            		Point3D point1 = new Point3D(0, 0,0);
            		
            		for (int h = 0; h< e.signalements.size(); h++) {
            			lat1 = e.signalements.get(h).latitude;
            			lon1 = e.signalements.get(h).longitude;
            			point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
            			displayTown(earth, "paris", point1, Color.GREEN);
            		}
      		  }
      	  }
      	});
        
        //Exception in thread "JavaFX Application Thread" java.lang.NullPointerException: 
        //Cannot invoke "java.lang.Double.doubleValue()" because "java.util.List.get(int).latitude" is null
        precision.setOnAction(new EventHandler<ActionEvent>() {
      	  public void handle(ActionEvent event) {
      		ClearChild(earth);
      		Espece e = Taxon(Recherche.getText());
      		OccurencePrecision(e, e.scientificName, Integer.parseInt(precision.getText()) );
      		Liste.setText("Scientific name : " + e.scientificName + "\n" + "acceptedNameUsageID : " + e.acceptedNameUsageID + "\n" + "id : " + e.id + "\n" + "rank : " +
	                e.rank + "\n" + "kingdom : " + e.kingdom + "\n" + "phylum : " + e.phylum + "\n" + "Nombre de signalements : " + e.signalements.size());
      		
      		double lat1;
    		double lon1;
    		Point3D point1 = new Point3D(0, 0,0);
    		
    		for (int h = 0; h< e.signalements.size(); h++) {
    			lat1 = e.signalements.get(h).latitude;
    			lon1 = e.signalements.get(h).longitude;
    			point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
    			displayTown(earth, "paris", point1, Color.GREEN);
    		}
      	 }
      	});
        
        Recherche.textProperty().addListener((obs, oldText, newText) -> {
        	
        	ResultatRecherche.getItems().clear();
        	List<String> liste = new ArrayList<String>();
            liste = AutoCompletion(newText);
            
            for (int i = 0 ; i<liste.size(); i++) {
            	ResultatRecherche.getItems().add(liste.get(i));
            }
            
        });
        
        ResultatRecherche.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
            	ClearChild(earth);
                Espece e = Taxon(ResultatRecherche.getSelectionModel().getSelectedItem());
                OccurenceGlobale(e, e.scientificName);
        		Liste.setText("Scientific name : " + e.scientificName + "\n" + "acceptedNameUsageID : " + e.acceptedNameUsageID + "\n" + "id : " + e.id + "\n" + "rank : " +
  	                e.rank + "\n" + "kingdom : " + e.kingdom + "\n" + "phylum : " + e.phylum + "\n" + "Nombre de signalements : " + e.signalements.size());
        		
        		double lat1;
        		double lon1;
        		Point3D point1 = new Point3D(0, 0,0);
        		
        		for (int h = 0; h< e.signalements.size(); h++) {
        			lat1 = e.signalements.get(h).latitude;
        			lon1 = e.signalements.get(h).longitude;
        			point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
        			displayTown(earth, "paris", point1, Color.GREEN);
        		}
      		    Recherche.setText(ResultatRecherche.getSelectionModel().getSelectedItem());
            }
        });
        
        fin.valueProperty().addListener((ov, oldValue, newValue) -> {
            if (debut.getValue() != null &&  debut.getValue().isBefore(newValue)) {

            	ClearChild(earth);
            	LocalDate d = debut.getValue();
            	Espece e = Taxon(Recherche.getText());
            	
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            	
                newValue.format(formatter);
                d.format(formatter);
                
                if (precision.getText() != "") 
                {
                	OccurencePrecisionTime(e, e.scientificName, Integer.parseInt(precision.getText()), d.toString(), newValue.toString());
                }
                else 
                {
                	OccurenceGlobaleTime(e, e.scientificName,d.toString() ,newValue.toString() );
                    
                }
                
                Liste.setText("Scientific name : " + e.scientificName + "\n" + "acceptedNameUsageID : " + e.acceptedNameUsageID + "\n" + "id : " + e.id + "\n" + "rank : " +
    	                e.rank + "\n" + "kingdom : " + e.kingdom + "\n" + "phylum : " + e.phylum + "\n" + "Nombre de signalements : " + e.signalements.size());
                
                double lat1;
        		double lon1;
        		Point3D point1 = new Point3D(0, 0,0);
        		
        		for (int h = 0; h< e.signalements.size(); h++) {
        			lat1 = e.signalements.get(h).latitude;
        			lon1 = e.signalements.get(h).longitude;
        			point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
        			displayTown(earth, "paris", point1, Color.GREEN);
        		}
            }
        });
        
        
        debut.valueProperty().addListener((ov, oldValue, newValue) -> {
            if (fin.getValue() != null &&  fin.getValue().isAfter(newValue)) {

            	ClearChild(earth);
            	LocalDate f = fin.getValue();
            	Espece e = Taxon(Recherche.getText());
            	
            	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            	
                newValue.format(formatter);
                f.format(formatter);
                
                if (precision.getText() != "") {
                	OccurencePrecisionTime(e, e.scientificName, Integer.parseInt(precision.getText()), newValue.toString(), f.toString());
                }
                else
                {
                	OccurenceGlobaleTime(e, e.scientificName,newValue.toString() ,f.toString() );
                }
                
                Liste.setText("Scientific name : " + e.scientificName + "\n" + "acceptedNameUsageID : " + e.acceptedNameUsageID + "\n" + "id : " + e.id + "\n" + "rank : " +
    	                e.rank + "\n" + "kingdom : " + e.kingdom + "\n" + "phylum : " + e.phylum + "\n" + "Nombre de signalements : " + e.signalements.size());
                
                double lat1;
        		double lon1;
        		Point3D point1 = new Point3D(0, 0,0);
        		
        		for (int h = 0; h< e.signalements.size(); h++) {
        			lat1 = e.signalements.get(h).latitude;
        			lon1 = e.signalements.get(h).longitude;
        			point1 = geoCoordTo3dCoord((float)lat1, (float)lon1, 1);
        			displayTown(earth, "paris", point1, Color.GREEN);
        		}
            }
        });
        
        
        /*
         * 
         * CECI EST UN TEST POUR VERIFIER SI ON PEUT LIRE LE FICHIER JSON LOCAL, ETAPE AVANT L'AUTOMATISATION DES REQUETES
         * 
         * 
        try (Reader reader = new FileReader("data.json")){
        	
        	BufferedReader rd = new BufferedReader(reader);
        	String jsonText = readAll(rd);
        	JSONObject jsonRoot = new JSONObject (jsonText);
        	
        	JSONArray resultatRecherche = jsonRoot.getJSONObject("query").getJSONArray("search");
        }
        catch (IOException e) 
        {
        	e.printStackTrace();
        }
        */
        
       Espece espece = Taxon("Delphinidae");
       
       //OccurenceGlobale(espece, espece.scientificName);
       OccurencePrecision(espece, espece.scientificName, 3);
       
       double lat;
       double lon;
       
       float d = (float) 8;
       
       for (int i = 0 ; i<espece.signalements.size(); i++) {
           if (espece.signalements.get(i).latitude <= -67.5 && espece.signalements.get(i).latitude >= -90 
        		   && espece.signalements.get(i).longitude <= -135 && espece.signalements.get(i).longitude >= -180) {
        	   
        	   final PhongMaterial redMaterial = new PhongMaterial();
        	    lat = -90;
   				lon = -180;
   				
   				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
                Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
                Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
                Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
                redMaterial.setDiffuseColor(Color.AZURE);
                AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
           }
           if (espece.signalements.get(i).latitude <= -45 && espece.signalements.get(i).latitude >= -67.5
        		   || espece.signalements.get(i).longitude <= -90 && espece.signalements.get(i).longitude >= -135) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = -67.5;
  				lon = -135;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.BLACK);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
  				
           }
           if (espece.signalements.get(i).latitude <= -22.5 && espece.signalements.get(i).latitude >= -45
        		   || espece.signalements.get(i).longitude <= -45 && espece.signalements.get(i).longitude >= -90) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = -45;
  				lon = -90;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.BLUE);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
               
           }
           if (espece.signalements.get(i).latitude <= 0 && espece.signalements.get(i).latitude >= -22.5
        		   || espece.signalements.get(i).longitude <= -0 && espece.signalements.get(i).longitude >= -45) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = -22.5;
  				lon = -45;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.BROWN);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
               
           }
           if (espece.signalements.get(i).latitude <= 22.5 && espece.signalements.get(i).latitude >= 0
        		   || espece.signalements.get(i).longitude <= 45 && espece.signalements.get(i).longitude >= 0) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = 0;
  				lon = 0;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.CORAL);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
               
           }
           if (espece.signalements.get(i).latitude <= 50 && espece.signalements.get(i).latitude >= 22.5
        		   || espece.signalements.get(i).longitude <= 90 && espece.signalements.get(i).longitude >= 45) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = 22.5;
  				lon = 45;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.FIREBRICK);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
               
           }
           if (espece.signalements.get(i).latitude <= 72.5 && espece.signalements.get(i).latitude >= 50
        		   || espece.signalements.get(i).longitude <= 135 && espece.signalements.get(i).longitude >= 90) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = 50;
  				lon = 90;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.RED);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
               
           }
           if (espece.signalements.get(i).latitude <= 90 && espece.signalements.get(i).latitude >= 72.5
        		   || espece.signalements.get(i).longitude <= 180 && espece.signalements.get(i).longitude >= 135) {
        	   final PhongMaterial redMaterial = new PhongMaterial();
       	    lat = 67.5;
  				lon = 135;
  				
  				Point3D p1 = geoCoordTo3dCoord((float)lat + d, (float)lon + d, (float)1.02);
               Point3D p2 = geoCoordTo3dCoord((float)lat + 0, (float)lon + d, (float)1.02);
               Point3D p3 = geoCoordTo3dCoord((float)lat + 0, (float)lon + 0, (float)1.02);
               Point3D p4 = geoCoordTo3dCoord((float)lat + d, (float)lon + 0, (float)1.02);
               redMaterial.setDiffuseColor(Color.GREEN);
               AddQuadrilateral(earth, p1, p2, p3, p4, redMaterial);
               
           }
       }
	}
	
	public static JSONObject readJsonFromUrl(String url ) {
    	String json = "";
    	HttpClient client = HttpClient.newBuilder()
    			.version(Version.HTTP_1_1)
    			.followRedirects(Redirect.NORMAL)
    			.connectTimeout(Duration.ofSeconds(20))
    			.build();
    	HttpRequest request = HttpRequest.newBuilder()
    			.uri(URI.create(url))
    			.timeout(Duration.ofMinutes(2))
    			.header("Content-Type", "application/json")
    		.GET()
    		.build();
    	try {
    		json = client.sendAsync (request, BodyHandlers.ofString())
    				.thenApply(HttpResponse::body).get(10, TimeUnit.SECONDS);
    		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return new JSONObject(json);
    }
    
    /*
     * 
     * CETTE FONCTION SERT POUR LE TEST DE LECTURE DU FICHIER JSON, IL NE SERT PLUS DANS LA SUITE
     * 
     * 
    private static String readAll(Reader rd ) throws IOException {
    	StringBuilder sb = new StringBuilder();
    	int cp;
    	while ((cp = rd.read()) != -1) {
    		sb.append((char) cp);
    	}
    	return sb.toString();
    }
    */
    
    public void displayTown(Group parent, String name,Point3D pt, Color couleur) {
    	Sphere sphere = new Sphere(0.01);
    	
    	sphere.setTranslateX(pt.getX());
    	sphere.setTranslateY(pt.getY());
    	sphere.setTranslateZ(pt.getZ());
    	
    	
    	//Create Material
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(couleur);
        blueMaterial.setSpecularColor(couleur);
        
        sphere.setMaterial(blueMaterial);
    	
    	parent.getChildren().add(sphere);
    }
    
    public void AddQuadrilateral(Group parent, Point3D topRight, Point3D bottomRight, Point3D bottomLeft, Point3D topLeft, PhongMaterial material) {
    	final TriangleMesh triangleMesh = new TriangleMesh();
    	final float[] points = {
    			(float)topRight.getX(),(float)topRight.getY(), (float)topRight.getZ(),
    			(float)topLeft.getX(),(float)topLeft.getY(), (float)topLeft.getZ(),
    			(float)bottomLeft.getX(),(float)bottomLeft.getY(), (float)bottomLeft.getZ(),
    			(float)bottomRight.getX(),(float)bottomRight.getY(), (float)bottomRight.getZ()
    	};
    
    	final float[] textCoords = {
    			1, 1,
    			1, 0,
    			0, 1,
    			0, 0
    			
    	};
    	final int[] faces = {
    			0, 1, 1, 0, 2, 2, 
    			0, 1, 2, 2, 3, 3
    	};
    	
    	triangleMesh.getPoints().setAll(points);
    	triangleMesh.getTexCoords().setAll(textCoords);
    	triangleMesh.getFaces().setAll(faces);
    	
    	final MeshView meshView = new MeshView(triangleMesh);
    	meshView.setMaterial(material);
    	parent.getChildren().addAll(meshView);
    }
    
    public void ClearChild(Group parent) {
        int l = parent.getChildren().size();
        for (int i = l-1 ; i> 0; i--) {
        	parent.getChildren().remove(i);
        }
    }
    
    // From Rahel Lüthy : https://netzwerg.ch/blog/2015/03/22/javafx-3d-line/
    public Cylinder createLine(Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(0.01f, height);

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    public static Point3D geoCoordTo3dCoord(float lat, float lon, float radius) {
        float lat_cor = lat + TEXTURE_LAT_OFFSET;
        float lon_cor = lon + TEXTURE_LON_OFFSET;
        return new Point3D(
                -java.lang.Math.sin(java.lang.Math.toRadians(lon_cor))
                        * java.lang.Math.cos(java.lang.Math.toRadians(lat_cor))*radius,
                -java.lang.Math.sin(java.lang.Math.toRadians(lat_cor))*radius,
                java.lang.Math.cos(java.lang.Math.toRadians(lon_cor))
                        * java.lang.Math.cos(java.lang.Math.toRadians(lat_cor))*radius);
    }
    
    //Hashcode
    private static final float TEXTURE_OFFSET = 1.01f;
    
    public static Point2D SpaceCoordToGeoCoord(Point3D p) {
    	float lat = (float)(Math.asin(-p.getY()/TEXTURE_OFFSET)*(180/Math.PI)- TEXTURE_LAT_OFFSET);
    	float lon;
    	
    	if(p.getZ()<0) {
    		lon = 180-(float)(Math.asin(-p.getX()/(TEXTURE_OFFSET*Math.cos((Math.PI/180)*(lat + TEXTURE_LAT_OFFSET))))*180/Math.PI + TEXTURE_LON_OFFSET);
    		
    	}else {
    		lon = (float)(Math.asin(-p.getX()/(TEXTURE_OFFSET*Math.cos((Math.PI/180)*(lat + TEXTURE_LAT_OFFSET))))*180/Math.PI - TEXTURE_LON_OFFSET);
    	}
    	
    	return new Point2D(lat,lon);
    }
    
    //	Fonction qui permet de vérifier si un attribut existe dans le fichier json, permet de mettre une valeur par défaut
    public static String chercherAttribut(JSONObject json, String attribut)
    {
    	
    	String att;
    	
    	try 
    	{
    		att = json.getString(attribut);
    	}
    	catch (Exception e)
    	{
    		att = "MissingAttribute";
    	}
    	
    	return att;
    }
    
    //	Fonction qui permet de vérifier si un attribut existe dans le fichier json, permet de mettre une valeur par défaut
    public static int chercherAttributInt(JSONObject json, String attribut)
    {
    	
    	int att;
    	
    	try 
    	{
    		att = json.getInt(attribut);
    	}
    	catch (Exception e)
    	{
    		att = -1;
    	}
    	
    	return att;
    }
    
    //	Fonction qui permet de vérifier si un attribut existe dans le fichier json, permet de mettre une valeur par défaut
    public static double chercherAttributDouble(JSONObject json, String attribut)
    {
    	
    	double att;
    	
    	try 
    	{
    		att = json.getDouble(attribut);
    	}
    	catch (Exception e)
    	{
    		att = -1;
    	}
    	
    	return att;
    }
    
    
    
    // quand il tape entrer
    //	Fonction qui créé une espèce à partir de la requête taxon de l'API
	public static Espece Taxon(String nom) {
		
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else 
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/taxon/" + nom2;
		
		 JSONObject espece = readJsonFromUrl(url);
	        JSONArray liste = espece.getJSONArray("results");
	        JSONObject attribut = liste.getJSONObject(0);

	        String scientificName = chercherAttribut(attribut,"scientificName");
	        int id = chercherAttributInt(attribut,"taxonID");
	        int acceptedNameUsageID = chercherAttributInt(attribut,"acceptedNameUsageID");

	        String rank = chercherAttribut(attribut,"taxonRank");
	        String kingdom = chercherAttribut(attribut,"kingdom");
	        String phylum = chercherAttribut(attribut,"phylum");

	        String classe = chercherAttribut(attribut,"class");
	        String order = chercherAttribut(attribut,"order");
	        String family = chercherAttribut(attribut,"family");
	        String genus = chercherAttribut(attribut,"genus");
	        String species = chercherAttribut(attribut,"species");

	        return new Espece(id, scientificName, acceptedNameUsageID, rank, kingdom, phylum, classe, order, family, genus, species);
	}
	// ajoute coordonné du signalement dans espece
	//	On ajoute à l'espèce, la liste des occurences suivant une précision
	public static void OccurencePrecision(Espece e, String nom, int precision)
	{
		
		e.signalements.clear();
		
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else 
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence/grid/" + precision + "?scientificname=" + nom2;
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray features = json.getJSONArray("features");
		
		for (int i = 0; i < features.length() ; i++)
		{
			JSONObject sign = features.getJSONObject(i);
		
			int id = sign.getJSONObject("properties").getInt("n");
			List<Double> coo = new ArrayList<Double>(10);
			
			JSONObject geometry = sign.getJSONObject("geometry");
			JSONArray coord = geometry.getJSONArray("coordinates");
			JSONArray list = coord.getJSONArray(0);
			
			for (int j = 0; j < list.length() ; j++)
			{
				JSONArray duo = list.getJSONArray(j);
				
				coo.add(duo.getDouble(0));
				coo.add(duo.getDouble(1));	
			}
			
			e.signalements.add(new Signalement(id, e.scientificName, coo));
		} 
	}
	
	//	On fait la même chose qu'au-dessus mais pour un intervalle de temps voulu
	public static void OccurencePrecisionTime(Espece e, String nom, int precision, String debut, String fin)
	{
		e.signalements.clear();
		
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence/grid/" + precision + "?scientificname=" + nom2 + "&startdate=" + debut + "&enddate=" + fin ;
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray features = json.getJSONArray("features");
		
		for (int i = 0; i < features.length() ; i++)
		{
			JSONObject sign = features.getJSONObject(i);
		
			int id = sign.getJSONObject("properties").getInt("n");
			List<Double> coo = new ArrayList<Double>(10);
			
			JSONObject geometry = sign.getJSONObject("geometry");
			JSONArray coord = geometry.getJSONArray("coordinates");
			JSONArray list = coord.getJSONArray(0);
			
			for (int j = 0; j < list.length() ; j++)
			{
				JSONArray duo = list.getJSONArray(j);
				
				coo.add(duo.getDouble(0));
				coo.add(duo.getDouble(1));	
			}
			
			e.signalements.add(new Signalement(id, e.scientificName, coo));
		} 
	}
	
	public static void OccurencePrecisionTime2(Espece e, String nom, int precision, String debut, String fin)
	{
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence/grid/" + precision + "?scientificname=" + nom2 + "&startdate=" + debut + "&enddate=" + fin ;
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray features = json.getJSONArray("features");
		
		for (int i = 0; i < features.length() ; i++)
		{
			JSONObject sign = features.getJSONObject(i);
		
			int id = sign.getJSONObject("properties").getInt("n");
			List<Double> coo = new ArrayList<Double>(10);
			
			JSONObject geometry = sign.getJSONObject("geometry");
			JSONArray coord = geometry.getJSONArray("coordinates");
			JSONArray list = coord.getJSONArray(0);
			
			for (int j = 0; j < list.length() ; j++)
			{
				JSONArray duo = list.getJSONArray(j);
				
				coo.add(duo.getDouble(0));
				coo.add(duo.getDouble(1));	
			}
			
			e.signalements.add(new Signalement(id, e.scientificName, coo));
		} 
	}
	
	
	//ajouter un bouton qui lance cette fonction pour donner le nb d'occurences globales
	//	On ajoute à l'espèce la liste des occurences, mais sans précision cette fois-ci
	public static void OccurenceGlobale(Espece e, String nom)
	{
		e.signalements.clear();
		
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence?scientificname=" + nom2;		
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray results = json.getJSONArray("results");
		
		for (int i = 0; i < results.length() ; i++)
		{
			JSONObject occ = results.getJSONObject(i);
		
			int date = chercherAttributInt(occ,"date_year");
			String id = chercherAttribut(occ,"id");
			
			Double longitude = chercherAttributDouble(occ,"decimalLongitude");
			Double latitude = chercherAttributDouble(occ,"decimalLatitude");
			
			e.signalements.add(new Signalement(id, e.scientificName, date, longitude, latitude));
		} 
	}
	
	//	Même travail qu'au dessus mais sur un intervalle de temps défini
	public static void OccurenceGlobaleTime(Espece e, String nom, String debut, String fin)
	{
		e.signalements.clear();
		
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence?scientificname=" + nom2 + "&startdate=" + debut + "&enddate=" + fin;
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray results = json.getJSONArray("results");
		
		for (int i = 0; i < results.length() ; i++)
		{
			JSONObject occ = results.getJSONObject(i);
		
			int date = chercherAttributInt(occ,"date_year");
			String id = chercherAttribut(occ,"id");
			
			Double longitude = chercherAttributDouble(occ,"decimalLongitude");
			Double latitude = chercherAttributDouble(occ,"decimalLatitude");
			
			e.signalements.add(new Signalement(id, e.scientificName, date, longitude, latitude));
		} 
	}
	
	
	//meme fonction sans le .clear du début. On utilise cette fonction pour afficher avec le pas
	public static void OccurenceGlobaleTime2(Espece e, String nom, String debut, String fin)
	{
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}
			else
			{
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence?scientificname=" + nom2 + "&startdate=" + debut + "&enddate=" + fin;
		System.out.println(url);
		JSONObject json = readJsonFromUrl(url);
		JSONArray results = json.getJSONArray("results");
		
		for (int i = 0; i < results.length() ; i++)
		{
			JSONObject occ = results.getJSONObject(i);
		
			int date = chercherAttributInt(occ,"date_year");
			String id = chercherAttribut(occ,"id");
			
			Double longitude = chercherAttributDouble(occ,"decimalLongitude");
			Double latitude = chercherAttributDouble(occ,"decimalLatitude");
			
			e.signalements.add(new Signalement(id, e.scientificName, date, longitude, latitude));
		} 
	}

	//	Fonction de recherche des occurences pour un nom et une geometry
	public static void OccurenceGlobaleGeometry(Espece e, String nom, String geometry)
	{
		e.signalements.clear();
		String nom2 = "";
		for (int i =0; i<nom.length(); i++) {
			
			if (nom.charAt(i) != ' ') {
				nom2 = nom2 + nom.charAt(i);
			}else {
				nom2 = nom2 + "%20";
			}
		}
		
		String url = "https://api.obis.org/v3/occurrence?scientificname=" + nom2 + "&geometry=" + geometry;		
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray results = json.getJSONArray("results");
		
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - ");
		System.out.println("|  SIGNALEMENTS POUR UNE ESPECE ET UNE GEOMETRY | ");
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - ");
		
		for (int i = 0; i < results.length() ; i++)
		{
			JSONObject occ = results.getJSONObject(i);
		
			int date = chercherAttributInt(occ,"date_year");
			String id = chercherAttribut(occ,"id");
			
			Double longitude = chercherAttributDouble(occ,"decimalLongitude");
			Double latitude = chercherAttributDouble(occ,"decimalLatitude");
			
			String scientificName = chercherAttribut(occ,"scientificName");
			
			String order = chercherAttribut(occ,"order");
			String superclass = chercherAttribut(occ,"superclass");
			String recordedBy= chercherAttribut(occ,"recordedBy");
			String species = chercherAttribut(occ,"species");
			
			System.out.println("- - - - - - - - - - -");
			System.out.println("scientificName : " + e.scientificName);
			System.out.println("date : " + date);
			System.out.println("id : " + id);
			System.out.println("longitude : " + longitude);
			System.out.println("latitude : " + latitude);
			System.out.println("order : " + order);
			System.out.println("superclass : " + superclass);
			System.out.println("recordedBy : " + recordedBy);
			System.out.println("species : " + species);
			System.out.println("- - - - - - - - - - -");
			
			e.signalements.add(new Signalement(id, scientificName, date, longitude, latitude, order, superclass, recordedBy, species));
		} 
	}
	
	//	Fonction de recherche des occurences pour un nom et une geometry
	public static void OccurenceGeometry(String geometry)
	{
		String url = "https://api.obis.org/v3/occurrence?geometry=" + geometry;		
		
		JSONObject json = readJsonFromUrl(url);
		JSONArray results = json.getJSONArray("results");
		
		System.out.println("- - - - - - - - - - - - - - - - - - ");
		System.out.println("|  SIGNALEMENTS POUR UNE GEOMETRY | ");
		System.out.println("- - - - - - - - - - - - - - - - - - ");
		
		for (int i = 0; i < results.length() ; i++)
		{
			JSONObject occ = results.getJSONObject(i);
		
			int date = chercherAttributInt(occ,"date_year");
			String id = chercherAttribut(occ,"id");
			
			Double longitude = chercherAttributDouble(occ,"decimalLongitude");
			Double latitude = chercherAttributDouble(occ,"decimalLatitude");
			
			String scientificName = chercherAttribut(occ,"scientificName");
			
			String order = chercherAttribut(occ,"order");
			String superclass = chercherAttribut(occ,"superclass");
			String recordedBy= chercherAttribut(occ,"recordedBy");
			String species = chercherAttribut(occ,"species");
			
			System.out.println("- - - - - - - - - - -");
			System.out.println("scientificName : " + scientificName);
			System.out.println("date : " + date);
			System.out.println("id : " + id);
			System.out.println("longitude : " + longitude);
			System.out.println("latitude : " + latitude);
			System.out.println("order : " + order);
			System.out.println("superclass : " + superclass);
			System.out.println("recordedBy : " + recordedBy);
			System.out.println("species : " + species);
			System.out.println("- - - - - - - - - - -");
		} 
	}
	
	
	public static JSONArray readJsonFromUrlArray(String url) {
    	
    	String json = "";
    	
    	HttpClient client = HttpClient.newBuilder()
    			.version(Version.HTTP_1_1)
    			.followRedirects(Redirect.NORMAL)
    			.connectTimeout(Duration.ofSeconds(20))
    			.build();
    	
    	HttpRequest request = HttpRequest.newBuilder()
    			.uri(URI.create(url))
    			.timeout(Duration.ofMinutes(2))
    			.header("Content-Type", "application/json")
    		.GET()
    		.build();
    	
    	try {
    		json = client.sendAsync (request, BodyHandlers.ofString())
    				.thenApply(HttpResponse::body).get(10, TimeUnit.SECONDS);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return new JSONArray(json);
    }
    
	
	//  listener a chaque fois qu'il écrit un caractere
	//	Fonction qui récupère les 20 noms d'espèces se rapprochant de l'entrée utilisateur
	public static List<String> AutoCompletion(String mot)
	{
		List<String> liste = new ArrayList<String>();
		String nom2 = "";
		for (int i =0; i<mot.length(); i++) {
			
			if (mot.charAt(i) != ' ') {
				nom2 = nom2 + mot.charAt(i);
			}
			else 
			{
				nom2 = nom2 + "%20";
			}
		}
		String url = "https://api.obis.org/v3/taxon/complete/verbose/" + nom2;
		
		//	On utilise une fonction différente de celle du Tuto, celle du tuto fourni un objet et non un tableau
		//	On a donc ajouter la fonction readJsonFromUrlArray pour pouvoir écrire l'autocomplétion
		
		JSONArray json = readJsonFromUrlArray(url);
	
		for (int i = 0; i < json.length() ; i++)
		{
			JSONObject nom = json.getJSONObject(i);
			//System.out.println(nom.getString("scientificName"));
			liste.add(nom.getString("scientificName"));
		} 
		return liste;
	}
    
}
