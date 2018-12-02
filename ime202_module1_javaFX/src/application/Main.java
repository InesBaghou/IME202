package application;
	
import java.io.File;
import java.io.IOException;

import application.view.ConteneurPrincipalMapping;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application
{
	private Stage stagePrincipal;
	private BorderPane conteneurPrincipal;
	
	public Main(){}
	
	public static void main(String[] args) throws IOException 
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Ouvrir fichier txt");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		
		System.out.println(fileChooser);
		
		/*File selectedFile = fileChooser.showOpenDialog(stagePrincipal);
		if (selectedFile != null)
		{
			stagePrincipal.display(selectedFile);
		}*/
		
		launch(args);
	}
	
	
	public Stage getStage() 
	{
		return stagePrincipal;
	}

	
	@Override
	public void start(Stage primaryStage) 
	{
		stagePrincipal = primaryStage;
		stagePrincipal.setTitle("module 1");
		
		initialisationConteneurPrincipal();
	}
	
	
	private void initialisationConteneurPrincipal()
	{
		//On créé un chargeur de FXML
		FXMLLoader loader = new FXMLLoader();
		//On lui spécifie le chemin relatif à notre classe
		//du fichier FXML a charger : dans le sous-dossier view
		loader.setLocation(Main.class.getResource("view/ConteneurPrincipal.fxml"));
		try 
		{
			//Le chargement nous donne notre conteneur
			conteneurPrincipal = (BorderPane) loader.load();
			System.out.println(conteneurPrincipal);
			//On définit une scène principale avec notre conteneur
			Scene scene = new Scene(conteneurPrincipal);
			//Que nous affectons à notre Stage
			stagePrincipal.setScene(scene);
			
			//Initialisation de notre contrôleur
			ConteneurPrincipalMapping controleur = loader.getController();
			//On spécifie la classe principale afin de pour récupérer le Stage
			//Et ainsi fermer l'application
			controleur.setMainApp(this);
					
			stagePrincipal.show();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
}
