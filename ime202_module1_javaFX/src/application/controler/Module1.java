package application.controler;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Module1 {
	
	// variables DAO
	private String chemin;
	private String cim10;
	private String transMorpho;
	private String transTopo;
	private String pmsi;
	private String anapath;
	private String cui;
	private String cimo3morpho;
	private String cimo3topo;
	private String adLesion;
	private String adOrgane;
	private String base;
	private String crModule1;
	
	
	// variables javaFX
	private StringProperty sp_chemin = new SimpleStringProperty();
	private StringProperty sp_cim10 = new SimpleStringProperty();
	private StringProperty sp_transMorpho = new SimpleStringProperty();
	private StringProperty sp_transTopo = new SimpleStringProperty();
	private StringProperty sp_pmsi = new SimpleStringProperty();
	private StringProperty sp_anapath = new SimpleStringProperty();
	private StringProperty sp_cimo3morpho = new SimpleStringProperty();
	private StringProperty sp_cimo3topo = new SimpleStringProperty();
	private StringProperty sp_cui = new SimpleStringProperty();
	private StringProperty sp_adLesion = new SimpleStringProperty();
	private StringProperty sp_adOrgane = new SimpleStringProperty();
	private StringProperty sp_base = new SimpleStringProperty();
	private StringProperty sp_crModule1 = new SimpleStringProperty();
	
	// getters et setters javaFX
	public StringProperty getChemin_sp() {return sp_chemin;}
	public void setChemin_sp(StringProperty chemin) {this.sp_chemin = chemin;}
	public StringProperty getCim10_sp() {return sp_cim10;}
	public void setCim10_sp(StringProperty cim10) {this.sp_cim10 = cim10;}
	public StringProperty getTransMorpho_sp() {return sp_transMorpho;}
	public void setTransMorpho_sp(StringProperty transMorpho) {this.sp_transMorpho = transMorpho;}
	public StringProperty getTransTopo_sp() {return sp_transTopo;}
	public void setTransTopo_sp(StringProperty transTopo) {this.sp_transTopo = transTopo;}
	public StringProperty getPmsi_sp() {return sp_pmsi;}
	public void setPmsi_sp(StringProperty pmsi) {this.sp_chemin = pmsi;}
	public StringProperty getAnapath_sp() {return sp_anapath;}
	public void setAnapath_sp(StringProperty anapath) {this.sp_anapath = anapath;}
	public StringProperty getCimo3morpho_sp() {return sp_cimo3morpho;}
	public void setCimo3morpho_sp(StringProperty cimo3morpho) {this.sp_cimo3morpho = cimo3morpho;}
	public StringProperty getCimo3topo_sp() {return sp_cimo3topo;}
	public void setimo3topo_sp(StringProperty cimo3topo) {this.sp_cimo3topo = cimo3topo;}
	public StringProperty getCui_sp() {return sp_cui;}
	public void setCui_sp(StringProperty cui) {this.sp_cui = cui;}
	public StringProperty getAdLesion_sp() {return sp_adLesion;}
	public void setAdLesion_sp(StringProperty adLesion) {this.sp_adLesion = adLesion;}
	public StringProperty getAdOrgane_sp() {return sp_adOrgane;}
	public void setAdOrgane_sp(StringProperty adOrgane) {this.sp_adOrgane = adOrgane;}
	public StringProperty getBase_sp() {return sp_base;}
	public void setBase_sp(StringProperty base) {this.sp_base = base;}
	public StringProperty getCRmodule1_sp() {return sp_crModule1;}
	public void setCRmodule1_sp(StringProperty crModule1) {this.sp_crModule1 = crModule1;}
	
	
	// Constructeurs
	public Module1()
	{
		this.chemin = "";
		this.cim10 = "";
		this.transMorpho = "";
		this.transTopo = "";
		this.pmsi = "";
		this.anapath = "";
		this.cui = "";
		this.cimo3morpho = "";
		this.cimo3topo = "";
		this.adLesion = "";
		this.adOrgane = "";
		this.base = "";
		this.crModule1 = "";
	}
	
	
	
	public Module1(String chemin, String cim10, String transMorpho, String transTopo, String pmsi, String anapath, String cui, String cimo3morpho, String cimo3topo, String adLesion, String adOrgane, String base)
	{
		this.chemin = chemin;
		this.cim10 = cim10;
		this.transMorpho = transMorpho;
		this.transTopo = transTopo;
		this.pmsi = pmsi;
		this.anapath = anapath;
		this.cui = cui;
		this.cimo3morpho = cimo3morpho;
		this.cimo3topo = cimo3topo;
		this.adLesion = adLesion;
		this.adOrgane = adLesion;
		this.base = base;
		this.crModule1 = "";
	}
	
	
	// fonctions
	
	public void afficherCR()
	{
		this.sp_crModule1.set(this.crModule1);
	}
	
	
	
	
	
	
	
	
	
	
	
	// getters et setters
	
	/**
	 * @return the chemin
	 */
	public String getChemin() {
		return chemin;
	}
	/**
	 * @param chemin the chemin to set
	 */
	public void setChemin(String chemin) {
		this.chemin = chemin;
	}
	/**
	 * @return the cim10
	 */
	public String getCim10() {
		return cim10;
	}
	/**
	 * @param cim10 the cim10 to set
	 */
	public void setCim10(String cim10) {
		this.cim10 = cim10;
	}
	/**
	 * @return the pmsi
	 */
	public String getPmsi() {
		return pmsi;
	}
	/**
	 * @param pmsi the pmsi to set
	 */
	public void setPmsi(String pmsi) {
		this.pmsi = pmsi;
	}
	/**
	 * @return the anapath
	 */
	public String getAnapath() {
		return anapath;
	}
	/**
	 * @param anapath the anapath to set
	 */
	public void setAnapath(String anapath) {
		this.anapath = anapath;
	}
	/**
	 * @return the cui
	 */
	public String getCui() {
		return cui;
	}
	/**
	 * @param cui the cui to set
	 */
	public void setCui(String cui) {
		this.cui = cui;
	}
	/**
	 * @return the cimo3morpho
	 */
	public String getCimo3morpho() {
		return cimo3morpho;
	}
	/**
	 * @param cimo3morpho the cimo3morpho to set
	 */
	public void setCimo3morpho(String cimo3morpho) {
		this.cimo3morpho = cimo3morpho;
	}
	/**
	 * @return the cimo3topo
	 */
	public String getCimo3topo() {
		return cimo3topo;
	}
	/**
	 * @param cimo3topo the cimo3topo to set
	 */
	public void setCimo3topo(String cimo3topo) {
		this.cimo3topo = cimo3topo;
	}
	/**
	 * @return the adLesion
	 */
	public String getAdLesion() {
		return adLesion;
	}
	/**
	 * @param adLesion the adLesion to set
	 */
	public void setAdLesion(String adLesion) {
		this.adLesion = adLesion;
	}
	/**
	 * @return the adOrgane
	 */
	public String getAdOrgane() {
		return adOrgane;
	}
	/**
	 * @param adOrgane the adOrgane to set
	 */
	public void setAdOrgane(String adOrgane) {
		this.adOrgane = adOrgane;
	}
	/**
	 * @return the base
	 */
	public String getBase() {
		return base;
	}
	/**
	 * @param base the base to set
	 */
	public void setBase(String base) {
		this.base = base;
	}
	/**
	 * @return the crModule1
	 */
	public String getCrModule1() {
		return crModule1;
	}
	/**
	 * @param crModule1 the crModule1 to set
	 */
	public void setCrModule1(String crModule1) {
		this.crModule1 = crModule1;
	}
	/**
	 * @return the transMorpho
	 */
	public String getTransMorpho() {
		return transMorpho;
	}
	/**
	 * @param transMorpho the transMorpho to set
	 */
	public void setTransMorpho(String transMorpho) {
		this.transMorpho = transMorpho;
	}
	/**
	 * @return the transTopo
	 */
	public String getTransTopo() {
		return transTopo;
	}
	/**
	 * @param transTopo the transTopo to set
	 */
	public void setTransTopo(String transTopo) {
		this.transTopo = transTopo;
	}
	
}
