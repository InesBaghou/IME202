package application.view;

import application.Main;
import application.controler.Module1;
import application.model.DAO;
import application.model.Module1DAO;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class ConteneurPrincipalMapping 
{
	private Main main;
	
	private Stage stage;
	
	private Module1 m1;
	private DAO<Module1> mDAO;
	
	@FXML
	private TextField chemin;
	@FXML
	private TextField cim10;
	@FXML
	private TextField transMorpho;
	@FXML
	private TextField transTopo;
	@FXML
	private TextField pmsi;
	@FXML
	private TextField anapath;
	@FXML
	private TextField adLesion;
	@FXML
	private TextField adOrgane;
	@FXML
	private TextField cui;
	@FXML
	private TextField cimo3topo;
	@FXML
	private TextField cimo3morpho;
	@FXML
	private TextField base;
	@FXML
	private Label crModule1;
	
	
	
	
	public void setMainApp(Main mainApp)
	{
		this.main = mainApp;
		stage = main.getStage();
	}
	
	public ConteneurPrincipalMapping() {}
	
	
	@FXML
	public void chargerData()
	{
		long startTime = System.currentTimeMillis();
		m1 = new Module1(chemin.getText(), cim10.getText(), transMorpho.getText(), transTopo.getText(), pmsi.getText(), anapath.getText(), cui.getText(), cimo3morpho.getText(), cimo3topo.getText(), adLesion.getText(), adOrgane.getText(), base.getText());
		mDAO = new Module1DAO(m1);	
		crModule1.setText(m1.getCrModule1());
		
		mDAO.traitementTables();
		System.out.println("Temps de lecture : " + (System.currentTimeMillis() - startTime) + " ms.");
		
	}
	
}
