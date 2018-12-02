package application.model;

import application.controler.Module1;
import application.model.DAO;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



public class Module1DAO extends DAO<Module1> {
	
		
	// Constante pour le chemin du dossier contenant les fichiers txt
	private String INDEX_DIR = "";	
	private String urlCon = "";
	private Map<String, Path> hm = new HashMap<>();
	
	
	public Module1DAO(Module1 m)
	{
		initialisationConnexion(m.getBase());
		
		selectFichier(m.getChemin(), m.getCim10(), m.getTransMorpho(), m.getTransTopo(), m.getPmsi(), m.getAnapath(), m.getCui(), m.getCimo3morpho(), m.getCimo3topo(), m.getAdLesion(), m.getAdOrgane());
		
		m.setCrModule1("Les données seront stockées dans la base : " + m.getBase() + "\nLes fichiers ont été chargés.");
	}
	
	
	
	public void initialisationConnexion(String base)
	{
		System.out.println("La base à utiliser se nomme : " + base);
		// URL pour la connexion à la base de données
		urlCon = URL_RACINE + base;
	}
	
	
	
	
	public void traitementTables()
	{
		Path pathFichier = null;
		try
	    {
	    	// Connexion
	        Class.forName("com.mysql.jdbc.Driver");
	        Connection con = DriverManager.getConnection(urlCon, USER, MDP);
	        Statement stt = con.createStatement();
	        String requeteSQL = "";
	        
	        System.out.println("Connecté, suppression de la table"); 
	        // suppression des tables dans la base de données
	        requeteSQL = supprimerTables();
	        stt.execute(requeteSQL);
	        
	        
	        System.out.println("Création de la table"); 
	        
			// création table patient
			requeteSQL = "CREATE TABLE IF NOT EXISTS `patient` " +
	        		"  (`NumPatient` int(6) NOT NULL," + 
	    			"  `Sexe` int(1) NOT NULL," + 
	    			"  `DDN` varchar(30) NOT NULL," + 
	    			"  `Prenom` varchar(30) NOT NULL," + 
	    			"  `Nom` varchar(30) NOT NULL);";
	        stt.execute(requeteSQL);
	        
	        // création de la table diagnostic
	        requeteSQL = "CREATE TABLE IF NOT EXISTS `diagnostic` " +
	        		"  (`NumDiag` int(6)," + 
	    			"  `CodeDiag` varchar(10)," + 
	    			"  `TypeCode` int(1)," + 
	    			"  `CodeTopo` varchar(30)," + 
	    			"  `CodeMorpho` varchar(30)," +
	    			"  `GroupeTopo` varchar(30)," + 
	    			"  `GroupeMorpho` varchar(30));";
	        stt.execute(requeteSQL);
	        
	        // table entre patient et diagnostic
	        requeteSQL = "CREATE TABLE IF NOT EXISTS `patient_diag` " +
	        		"  (`NumPatient` int(6) NOT NULL," + 
	    			"  `NumDiag` int(6) NOT NULL);";
	        stt.execute(requeteSQL);
	        
	        
	     // parcourir les différents fichiers
	        //Map<String, Path> listPath = selectFichier();			
			Set<Entry<String, Path>> setHm = hm.entrySet();
		    Iterator<Entry<String, Path>> it = setHm.iterator();
		    while(it.hasNext())
		    {
		       Entry<String, Path> entry = it.next();
		       System.out.println("Chargement du fichier : " + entry.getKey() + " : " + entry.getValue());
		       pathFichier = entry.getValue();
		       
		       // la table à créer dépend du nom du fichier
		       requeteSQL = requeteCreerTab(pathFichier);
		       stt.execute(requeteSQL);
		    }	        
	        
		    
		    
		    requeteSQL = "";
            
            
            ////////////////////////////
            // INSERTION DONNEES BRUTES
            ////////////////////////////
            // lecture de chaque fichier pour insérer les données brutes
            //Map<String, Path> listPath = selectFichier();			
			Set<Entry<String, Path>> setHm2 = hm.entrySet();
		    Iterator<Entry<String, Path>> it2 = setHm2.iterator();
		    while(it2.hasNext())
		    {
		        Entry<String, Path> entry = it2.next();
		        System.out.println("Chargement des données du fichier : " + entry.getKey() + " : " + entry.getValue());
		        pathFichier = entry.getValue();
	            // lecture du fichier txt ligne par ligne
				try (BufferedReader reader = Files.newBufferedReader(pathFichier, StandardCharsets.ISO_8859_1))
				{
					String ligneTransformee = "";
					requeteSQL = requeteRemplirTab(pathFichier);
					
					// lecture de la première ligne
					String ligne = reader.readLine();
					
					// on passe directement à la seconde car la première contient le titre des colonnes
					ligne = reader.readLine();
					while (ligne != null)
					{
						// on remplace les caractères pouvant poser problèmes
						if (ligne.contains("'"))
						{
							ligne = ligne.replace("'", " ");
						}
						
						if (ligne.contains("¤"))
						{
							ligne = ligne.replace("¤", "");
							//System.out.println("corrigé");
						}
						
						
						// on change les | pour mettre des caractères compréhensibles en SQL
						ligne = ligne.replace("|", "', '");
						
						
						// on ajoute au début et à la fin de la ligne des parenthèse pour que SQL comprenne qu'il s'agit d'une ligne
						ligneTransformee = "('" + ligne + "'),";
						//System.out.println(ligneTransformee);
						
						// on ajoute cette ligne à notre requête finale
						requeteSQL += ligneTransformee;
						
						ligne = reader.readLine();
						
						int longueur = requeteSQL.length();
						if(longueur>1000000||ligne == null)
						{
							requeteSQL = requeteSQL.substring(0, longueur-1) + ";";
					        stt.execute(requeteSQL);
					        requeteSQL = requeteRemplirTab(pathFichier);
					    }					
					}
				}
			}
			requeteSQL = "";
	        
			////////////////
			// TABLE PATIENT
			////////////////
	        // récupérer les lignes pour le patient de sejour et anapath
			requeteSQL = "CREATE TEMPORARY TABLE `pmsiPatient` AS ( SELECT * FROM `pmsi`);";
	        stt = con.createStatement();
	        stt.execute(requeteSQL);
	        requeteSQL = "CREATE TEMPORARY TABLE `anapathPatient` AS ( SELECT * FROM `anapath`);";
	        stt = con.createStatement();
	        stt.execute(requeteSQL);
	            
	        // modification de anapathPatient pour avoir le même format de date que pour sejourPatient
	        requeteSQL = "UPDATE `anapathPatient` SET `DateDeNaissance` = LEFT(`DateDeNaissance`, 9)";
	        stt.execute(requeteSQL);
	        requeteSQL = "ALTER TABLE `anapathPatient` CHANGE `DateDeNaissance` `DDN` varchar(30) NOT NULL";
	        stt.execute(requeteSQL);
	        
	        // suppression des colonnes en trop dans les tables temporaires
	        requeteSQL = "ALTER TABLE `anapathPatient` DROP COLUMN `DatePrelev`, DROP COLUMN `Adicap`, DROP COLUMN `Adicap2`;";
	        stt.execute(requeteSQL);
	        requeteSQL = "ALTER TABLE `pmsiPatient` DROP COLUMN `edath`, DROP COLUMN `sdath`, DROP COLUMN `edat`, DROP COLUMN `sdat`, DROP COLUMN `DP`, DROP COLUMN `DR`, DROP COLUMN `NumSejour`, DROP COLUMN `DAS`;";
	        stt.execute(requeteSQL);
	            
	        // Union des tables et insertion dans patient
	        requeteSQL = "INSERT INTO patient (SELECT * FROM (SELECT * FROM anapathpatient UNION SELECT * FROM pmsipatient) AS reunion);";
	        stt.executeUpdate(requeteSQL);

	        
	        
	        //////////////////////
	        // ADICAP
	        ////////////////////
	        System.out.println("Traitement de la table ADICAP");
	        //System.out.println("Temps de lecture début traitement : " + (System.currentTimeMillis() - startTime) + " ms.");
	        
	        //récupérer les codes adicap2, puis UPDATE adicp1 pour coler le code adicap2 si non vide en ajoutant ";"	        
	        // supprimer les colonnes inutiles pour gagner en rapidité
	        requeteSQL = "ALTER TABLE `anapath` DROP COLUMN `Sexe`, DROP COLUMN `DateDeNaissance`, DROP COLUMN `Prenom`, DROP COLUMN `Nom`;";
	        stt.execute(requeteSQL);
	        
	        
	        
	        
	        // selectionner toutes les colonnes sauf adicap1 pour toutes les lignes où adicap2 != null
	        /*requeteSQL = "SELECT `NumPatient`,`DatePrelev`,`Adicap2` FROM `anapath` WHERE LENGTH(`Adicap2`) != 0;";
	        ResultSet rs;
	        rs=stt.executeQuery(requeteSQL);
	        while (rs.next())
	        {
	        	Statement sttInsert = con.createStatement();
	        	String sqlinsert="INSERT INTO `anapath` VALUES('"+rs.getInt("NumPatient")+"','"+rs.getString("DatePrelev")+"','"+rs.getString("Adicap2")+"','');";
	          	sttInsert.executeUpdate(sqlinsert);
	        }*/
	        //////// ALTERNATIVE
	        requeteSQL = "INSERT INTO `anapath` (SELECT `NumPatient`,`DatePrelev`,`Adicap2`, NULL FROM `anapath` " + 
					"WHERE LENGTH(`Adicap2`) != 0);";
	        stt.execute(requeteSQL);
	        
	        
	        // supprimer la colonne Adicap2
	        requeteSQL = "ALTER TABLE `anapath` DROP COLUMN `Adicap2`;";
	        stt.execute(requeteSQL);
	        // supprimer les lignes avec Adicap vide
	        requeteSQL = "DELETE FROM `anapath` WHERE LENGTH(`Adicap`) = 0;";
	        stt.execute(requeteSQL);
	        
	        // compter nombre de codes à chaque ligne
	        // créer de nouvelles colonnes
	        requeteSQL = "ALTER TABLE `anapath` ADD COLUMN `positionPointVir` int(2);";
	        stt.execute(requeteSQL);
	        
	        // opérations
	        requeteSQL = "UPDATE `anapath` SET `positionPointVir`=LOCATE(';',`Adicap`);";
	        stt.execute(requeteSQL);
	        int pointVirgule = 1;
	        
	        ResultSet rs;
	        
	        while(pointVirgule>0)
	        {
		        /*requeteSQL = "SELECT `NumPatient`,`DatePrelev`,SUBSTR(`Adicap`,`positionPointVir`+1,LENGTH(`Adicap`)) AS `newAdicap` FROM `anapath` WHERE `positionPointVir`>0;";
		        rs=stt.executeQuery(requeteSQL);
		        while (rs.next())
		        {
		        	Statement sttInsert = con.createStatement();
		        	requeteSQL="INSERT INTO `anapath` (`NumPatient`,`DatePrelev`,`Adicap`) VALUES('"+rs.getInt("NumPatient")+"','"+rs.getString("DatePrelev")+"','"+rs.getString("newAdicap")+"');";
		        	//System.out.println(requeteSQL);
		        	sttInsert.executeUpdate(requeteSQL);
		        }*/
		        ///////// ALTERNATIVE
		        requeteSQL="INSERT INTO `anapath` (SELECT `NumPatient`,`DatePrelev`,SUBSTR(`Adicap`,`positionPointVir`+1,LENGTH(`Adicap`)) AS `Adicap`, NULL FROM `anapath` WHERE `positionPointVir`>0);";
		        stt.execute(requeteSQL);
		        
		        
		        
		        requeteSQL = "UPDATE `anapath` SET `Adicap` = SUBSTR(`Adicap`,1,`positionPointVir`-1) WHERE `positionPointVir`>0;";
		        stt.execute(requeteSQL);        
		        requeteSQL = "UPDATE `anapath` SET `positionPointVir`=LOCATE(';',`Adicap`);";
		        stt.execute(requeteSQL);
		        
		        requeteSQL = "SELECT MAX(`positionPointVir`) AS `max` FROM `anapath`;";
		        rs=stt.executeQuery(requeteSQL);
		        rs.next();
		        pointVirgule = rs.getInt("max");
	        }
	        
	        requeteSQL = "ALTER TABLE `anapath` DROP COLUMN `positionPointVir`, DROP COLUMN `DatePrelev`;";
	        stt.execute(requeteSQL);
	        
	        requeteSQL = "DELETE FROM `anapath` WHERE LENGTH(`Adicap`) != 8;";
	        stt.execute(requeteSQL);
	        

	        // suppression des doublons
	        System.out.println("Suppression des doublons de la table ADICAP");
	        //System.out.println("Temps de lecture début suppressions doublons : " + (System.currentTimeMillis() - startTime) + " ms.");
	        /*requeteSQL = "SELECT DISTINCT * FROM `anapath`;";
	        rs=stt.executeQuery(requeteSQL);
	        
	        Statement stt2 = con.createStatement();
	        requeteSQL = "TRUNCATE TABLE `anapath`;";
	        stt2.execute(requeteSQL);
	        
	        while (rs.next())
	        {
	        	Statement sttInsert = con.createStatement();
	        	String sqlinsert="INSERT INTO `anapath` VALUES('"+rs.getInt("NumPatient")+"','"+rs.getString("Adicap")+"');";
	          	sttInsert.executeUpdate(sqlinsert);
	        }*/
	        ////////// ALTERNATIVE creation nouvelle table
	        requeteSQL = "CREATE TABLE `anapath2` AS SELECT DISTINCT * FROM `anapath`;";
	        stt.execute(requeteSQL);
	        
	        
	        // récupérer tous les diagnostics différents
	        System.out.println("Remplissage de la table diagnostic");
	        //System.out.println("Temps de lecture remplissage diag : " + (System.currentTimeMillis() - startTime) + " ms.");
	        //requeteSQL = "SELECT DISTINCT `Adicap` FROM `anapath`;";
	        requeteSQL = "SELECT DISTINCT `Adicap` FROM `anapath2`;";
	        rs=stt.executeQuery(requeteSQL);
	        
	        int numDiag = 1;
	        
	        while (rs.next())
	        {
	        	Statement sttInsert = con.createStatement();
	        	String sqlinsert="INSERT INTO `diagnostic` (`NumDiag`, `CodeDiag`,`TypeCode`) VALUES('" + numDiag + "','"+rs.getString("Adicap")+"','1');";
	          	sttInsert.executeUpdate(sqlinsert);
	          	numDiag += 1;
	        }
	        
	        // selection pour remplir table de liaison
	        //requeteSQL = "INSERT into  patient_diag (SELECT anapath.NumPatient, diagnostic.NumDiag FROM anapath LEFT JOIN diagnostic ON anapath.Adicap = diagnostic.CodeDiag);";
	        requeteSQL = "INSERT into  patient_diag (SELECT anapath2.NumPatient, diagnostic.NumDiag FROM anapath2 LEFT JOIN diagnostic ON anapath2.Adicap = diagnostic.CodeDiag);";
	        stt.executeUpdate(requeteSQL);
	        
	        
	        
	        
	        /////////////////////    
		    // CORRECTION TABLES
	        /////////////////////
	        
	        
	        // CIM10 : enlever les ligne si type != D puis supprimer le type
	        requeteSQL = "DELETE FROM `cim10` WHERE `type` != 'D';";
	        stt.execute(requeteSQL);
	            
	            
	        // suppression des colonnes en trop
	        System.out.println("Suppression tables");
	        //System.out.println("Temps de lecture suppression tables : " + (System.currentTimeMillis() - startTime) + " ms.");
	        
	        it = setHm.iterator();
	        while(it.hasNext())
		    {
		        Entry<String, Path> entry = it.next();
		        System.out.println("Suppression de colonnes : " + entry.getKey() + " : " + entry.getValue());
		        pathFichier = entry.getValue();
		        
		        requeteSQL = requeteSupprimerCol(pathFichier);
			    if (!requeteSQL.isEmpty())
			    {
			      	stt.execute(requeteSQL);
			    }
			    
		    }
	        
	        
	        requeteSQL = "CREATE TABLE `PMSI2` AS SELECT `NumPatient`, `DP` AS `DiagPMSI` FROM `pmsi` WHERE " + 
	        		"((substr(DP,2,2) between '00' and '07' AND DP LIKE 'C%' ) OR " + 
	        		"(substr(DP,2,2) between '10' and '48' AND DP LIKE 'D%' ));";
	        stt.execute(requeteSQL);
	        
	        requeteSQL = "INSERT into  `PMSI2` (SELECT `NumPatient`, `DR` AS `DiagPMSI`" + 
	        		" FROM `PMSI`" + 
	        		" WHERE ((substr(DR,2,2) between '00' and '07' AND DR LIKE 'C%' ) OR" + 
	        		" (substr(DR,2,2) between '10' and '48' AND DR LIKE 'D%' )));";
	        stt.execute(requeteSQL);
	        
	        requeteSQL = "INSERT into  `PMSI2` (SELECT `NumPatient`, `DAS` AS `DiagPMSI`" + 
	        		" FROM `PMSI`" + 
	        		" WHERE ((substr(DAS,2,2) between '00' and '07' AND DAS LIKE 'C%') OR" + 
	        		" (substr(DAS,2,2) between '10' and '48' AND DAS LIKE 'D%' )));";
	        stt.execute(requeteSQL);
	        
	        
	        // remplissage avec la CIM10
	        System.out.println("Remplissage de la table diagnostic CIM10");
	        //System.out.println("Temps de lecture remplissage diag CIM10 : " + (System.currentTimeMillis() - startTime) + " ms.");
	        requeteSQL = "SELECT DISTINCT `DiagPMSI` FROM `PMSI2`;";
	        rs=stt.executeQuery(requeteSQL);
	        
	       //numDiag += 1;
	        
	        while (rs.next())
	        {
	        	Statement sttInsert = con.createStatement();
	        	String sqlinsert="INSERT INTO `diagnostic` (`NumDiag`, `CodeDiag`,`TypeCode`) VALUES('" + numDiag + "','"+rs.getString("DiagPMSI")+"','2');";
	          	sttInsert.executeUpdate(sqlinsert);
	          	numDiag += 1;
	        }
	        
	        // selection pour remplir table de liaison
	        //requeteSQL = "INSERT into  patient_diag (SELECT anapath.NumPatient, diagnostic.NumDiag FROM anapath LEFT JOIN diagnostic ON anapath.Adicap = diagnostic.CodeDiag);";
	        requeteSQL = "INSERT into  patient_diag (SELECT PMSI2.NumPatient, diagnostic.NumDiag FROM PMSI2 LEFT JOIN diagnostic ON PMSI2.DiagPMSI = diagnostic.CodeDiag);";
	        stt.executeUpdate(requeteSQL);
	        
	        
	        // code cimo3 pour les codes adicap
	        /*
	         * SELECT `CODTOPOCIMO3`, `CODMORPHOCIMO3` FROM `transcodage_adicapcimo3_topo`, `transcodage_adicapcimo3_morpho`,
(SELECT SUBSTR(`CodeDiag`,3,2) AS `Organe`, SUBSTR(`CodeDiag`,5,4) AS `Lesion` FROM `Diagnostic` WHERE `TypeCode`=1) AS selection
WHERE transcodage_adicapcimo3_morpho.LESION= selection.Lesion AND transcodage_adicapcimo3_topo.ORGANE=selection.Organe
	         
	         *
	         *
	         *CREATE TABLE selectionTable AS 
SELECT `CODTOPOCIMO3`, `CODMORPHOCIMO3` FROM `transcodage_adicapcimo3_topo`, `transcodage_adicapcimo3_morpho`,
(SELECT SUBSTR(`CodeDiag`,3,2) AS `Organe`, SUBSTR(`CodeDiag`,5,4) AS `Lesion` FROM `Diagnostic` WHERE `TypeCode`=1) AS selection
WHERE transcodage_adicapcimo3_morpho.LESION= selection.Lesion AND transcodage_adicapcimo3_topo.ORGANE=selection.Organe
	         *
	         *
	         */
			  
        }catch (Exception e)
	    {
	         e.printStackTrace();
	    }
	}
	
	
	
	
	
	
	
	
	
	
	void selectFichier(String chemin, String cim10, String transMorpho, String transTopo, String pmsi, String anapath, String cui, String cimo3morpho, String cimo3topo, String adLesion, String adOrgane)
	{
		this.INDEX_DIR = chemin;
	    hm.put("adicap_lesion", Paths.get(INDEX_DIR + adLesion));
	    hm.put("adicap_organe", Paths.get(INDEX_DIR + adOrgane));
	    hm.put("Anapath", Paths.get(INDEX_DIR + anapath));
	    hm.put("PMSI", Paths.get(INDEX_DIR + pmsi));
	    hm.put("cim10", Paths.get(INDEX_DIR + cim10));
	    hm.put("cimo3_cui_UMLS", Paths.get(INDEX_DIR + cui));
	    hm.put("CIMO3_MORPHO", Paths.get(INDEX_DIR + cimo3morpho));
	    hm.put("CIMO3_TOPO", Paths.get(INDEX_DIR + cimo3topo));
	    hm.put("TRANSCODAGE_ADICAPCIMO3_MORPHO", Paths.get(INDEX_DIR + transMorpho));
	    hm.put("TRANSCODAGE_ADICAPCIMO3_TOPO", Paths.get(INDEX_DIR + transTopo));  
	    
	    System.out.println("Parcours de l'objet HashMap : ");
	    Set<Entry<String, Path>> setHm = hm.entrySet();
	    Iterator<Entry<String, Path>> it = setHm.iterator();
	    while(it.hasNext())
	    {
	       Entry<String, Path> e = it.next();
	       System.out.println(e.getKey() + " : " + e.getValue());
	    }
	}
	
	
	String requeteCreerTab(Path fichier)
	{
		String requete = "";
		if (fichier.endsWith("adicap_lesion.txt"))
        {
			requete = "CREATE TABLE IF NOT EXISTS `adicap_lesion` "
            		+ "(`codeadicap` varchar(4) NOT NULL, `libelle` varchar(190) NOT NULL);";
        }
        else if (fichier.endsWith("adicap_organe.txt"))
        {
        	requete = "CREATE TABLE IF NOT EXISTS `adicap_organe`"+ 
        			"  (`organe` varchar(2) NOT NULL," + 
        			"  `liborgane` varchar(130) NOT NULL, `datemodif` varchar(10) NOT NULL);";
        }
        else if (fichier.endsWith("Anapath.txt"))
        {
        	requete = "CREATE TABLE IF NOT EXISTS `anapath` " + 
        			"  (`NumPatient` int(6) NOT NULL," + 
        			"  `Sexe` int(1) NOT NULL," + 
        			"  `DateDeNaissance` varchar(20) NOT NULL," + 
        			"  `Prenom` varchar(30) NOT NULL," + 
        			"  `Nom` varchar(30) NOT NULL," + 
        			"  `DatePrelev` varchar(10) NOT NULL," + 
        			"  `Adicap` varchar(80) NOT NULL," +
        			"  `Adicap2` varchar(80));";
        }
        else if(fichier.endsWith("PMSI.txt"))  {
       	requete = "CREATE TABLE IF NOT EXISTS `PMSI` " + 
        		"  (`NumPatient` int(6) NOT NULL," + 
        		"  `Sexe` int(1) NOT NULL," + 
        		"  `DDN` varchar(10) NOT NULL," + 
        		"  `Prenom` varchar(30) NOT NULL," + 
        		"  `Nom` varchar(30) NOT NULL," + 
        		"  `edath` varchar(10) NOT NULL," + 
        		"  `sdath` varchar(10) NOT NULL," + 
        		"  `edat` varchar(10) NOT NULL," + 
        		"  `sdat` varchar(10) NOT NULL," + 
        		"  `DP` varchar(6) NOT NULL," + 
        		"  `DR` varchar(6) NOT NULL," + 
        		"  `NumSejour` varchar(6) NOT NULL,"+ 
        		"  `DAS` varchar(6) NOT NULL);";
        }
        else if (fichier.endsWith("Sejour.txt"))
        {
        	requete = "CREATE TABLE IF NOT EXISTS `sejour` " + 
        			"  (`NumPatient` int(6) NOT NULL," + 
        			"  `Sexe` int(1) NOT NULL," + 
        			"  `DDN` varchar(10) NOT NULL," + 
        			"  `Prenom` varchar(30) NOT NULL," + 
        			"  `Nom` varchar(30) NOT NULL," + 
        			"  `edath` varchar(10) NOT NULL," + 
        			"  `sdath` varchar(10) NOT NULL," + 
        			"  `edat` varchar(10) NOT NULL," + 
        			"  `sdat` varchar(10) NOT NULL," + 
        			"  `DP` varchar(6) NOT NULL," + 
        			"  `DR` varchar(6) NOT NULL," + 
        			"  `NumSejour` varchar(6) NOT NULL);";
        }
        else if (fichier.endsWith("cim10.txt"))
        {
        	requete = "CREATE TABLE IF NOT EXISTS `cim10` " + 
        			"  (`type` varchar(1) NOT NULL," + 
        			"  `code` varchar(6) NOT NULL," + 
        			"  `lib` varchar(250) NOT NULL," +
        			"  `txt` varchar(250) NOT NULL," +
        			"  `findevalid` varchar(150) NOT NULL," +
        			"  `extension` varchar(1) NOT NULL," + 
        			"  `cma_niv` varchar(1) NOT NULL," + 
        			"  `cma` varchar(1) NOT NULL," + 
        			"  `cmas` varchar(1) NOT NULL," + 
        			"  `cmas_nt` varchar(1) NOT NULL," + 
        			"  `code_imprecis` varchar(1) NOT NULL," + 
        			"  `interdits_en_DP` varchar(1) NOT NULL," + 
        			"  `interdits_en_DR` varchar(1) NOT NULL," + 
        			"  `requerant_un_DR` varchar(1) NOT NULL," + 
        			"  `ssrFP` varchar(1) NOT NULL," + 
        			"  `ssrMP` varchar(1) NOT NULL," + 
        			"  `ssrAE` varchar(1) NOT NULL," + 
        			"  `ssrDAS` varchar(1) NOT NULL," + 
        			"  `ssrZ_CMC` varchar(10) NOT NULL," + 
        			"  `pmsi` varchar(1) NOT NULL," + 
        			"  `MAJ` varchar(20) NOT NULL," + 
        			"  `auteur_MAJ` varchar(2) NOT NULL," + 
        			"  `remarque1` varchar(150) NOT NULL," + 
        			"  `_009_EXT__LIB__finvalid__doublon` varchar(70) NOT NULL);";
        }
        else if(fichier.endsWith("cimo3_cui_UMLS.txt"))  {
        	 requete = "CREATE TABLE IF NOT EXISTS `cimo3_cui_UMLS` "
             		+ "(`CODTOPOCIMO3` varchar(4) NOT NULL, `CODMORPHOCIMO3` varchar(5) NOT NULL, `CUI_UMLS` varchar(190) NOT NULL);";
        }
	
	    else if(fichier.endsWith("CIMO3_MORPHO.txt"))  {
	    	 requete = "CREATE TABLE IF NOT EXISTS `CIMO3_MORPHO` (`codmorphocimo3` int(5) NOT NULL,`libmorphocimo3` varchar(120) NOT NULL,`morpho_iacr` varchar(90) NOT NULL,`groupe_morpho_iacr` varchar(3) NOT NULL,`reg_5` varchar(3) NOT NULL, `reg_14` varchar(3) NOT NULL,`reg_17` varchar(3) NOT NULL);";
	    }
	
	    else if(fichier.endsWith("CIMO3_TOPO.txt"))  {
	    	 requete = "CREATE TABLE IF NOT EXISTS `CIMO3_TOPO`(\r\n" + 
	         		"  `cimo3` varchar(10) NOT NULL,\r\n" + 
	         		"  `libtopocimo3` varchar(190) NOT NULL,\r\n" + 
	         		"  `codtopocimo3` varchar(5) NOT NULL,\r\n" + 
	         		"  `reg_codtopocimo3` varchar(5) NOT NULL,\r\n" + 
	         		"  `reg_libtopocimo3` varchar(190) NOT NULL,\r\n" + 
	         		"  `Topo_encr` varchar(190) NOT NULL,\r\n" + 
	         		"  `TOPO_IACR` varchar(80) NOT NULL,\r\n" + 
	         		"  `GROUPE_TOPO_IACR` varchar(5) NOT NULL\r\n" + 
	         		");";
	     }
	
	    else if(fichier.endsWith("TRANSCODAGE_ADICAPCIMO3_MORPHO.txt"))  {
	   	 requete = "CREATE TABLE IF NOT EXISTS `TRANSCODAGE_ADICAPCIMO3_MORPHO`(  `LIBMORPHOCIMO3` varchar(190) NOT NULL,\r\n" + 
	     		"  `CIMO3` varchar(10) NOT NULL,\r\n" + 
	     		"  `CODMORPHOCIMO3` varchar(10) NOT NULL,\r\n" + 
	     		"  `Comm` varchar(8) NOT NULL,\r\n" + 
	     		"  `LESION` varchar(90) NOT NULL,\r\n" + 
	     		"  `LIBLESION` varchar(180) NOT NULL,\r\n" + 
	     		"  `MODIFADICAP` varchar(80) NOT NULL) ;";
	    }
	    else if(fichier.endsWith("TRANSCODAGE_ADICAPCIMO3_TOPO.txt"))  {
      	 requete = "CREATE TABLE IF NOT EXISTS `TRANSCODAGE_ADICAPCIMO3_TOPO` "
          		+ "(`CODTOPOCIMO3` varchar(4) NOT NULL, `ORGANE` varchar(2) NOT NULL, `TOPO` varchar(2) NOT NULL);";
	    }

		return requete;
	}
	
	
	String requeteRemplirTab(Path fichier)
	{
		String requete = "";
		if (fichier.endsWith("adicap_lesion.txt"))
        {
			requete = "INSERT INTO `adicap_lesion` (`codeadicap`, `libelle`) VALUES ";
        }
        else if (fichier.endsWith("adicap_organe.txt"))
        {
        	requete = "INSERT INTO `adicap_organe` (`organe`, `liborgane`, `datemodif`) VALUES ";
        }
        else if (fichier.endsWith("Anapath.txt"))
        {
        	requete = "INSERT INTO `anapath` (`NumPatient`, `Sexe`, `DateDeNaissance`, `Prenom`, `Nom`, `DatePrelev`, `Adicap`, `Adicap2`) VALUES ";
        }
        else if (fichier.endsWith("cim10.txt"))
        {
        	requete = "INSERT INTO `cim10` (`type`, `code`, `lib`, `txt`, `findevalid`, `extension`, `cma_niv`, `cma`, `cmas`, `cmas_nt`, `code_imprecis`, `interdits_en_DP`, `interdits_en_DR`, `requerant_un_DR`, `ssrFP`, `ssrMP`, `ssrAE`, `ssrDAS`, `ssrZ_CMC`, `pmsi`, `MAJ`, `auteur_MAJ`, `remarque1`, `_009_EXT__LIB__finvalid__doublon`) VALUES ";
        }
        else if (fichier.endsWith("Sejour.txt"))
        {
        	requete = "INSERT INTO `sejour` (`NumPatient`, `Sexe`, `DDN`, `Prenom`, `Nom`, `edath`, `sdath`, `edat`, `sdat`, `DP`, `DR`, `NumSejour`) VALUES ";
        }
        else if (fichier.endsWith("PMSI.txt"))
        {
        	requete = "INSERT INTO `pmsi` (`NumPatient`, `Sexe`, `DDN`, `Prenom`, `Nom`, `edath`, `sdath`, `edat`, `sdat`, `DP`, `DR`, `NumSejour`,`DAS`) VALUES ";
        }
        else if (fichier.endsWith("cimo3_cui_UMLS.txt"))
        {
        	requete = "INSERT INTO `cimo3_cui_UMLS` (`CODTOPOCIMO3`, `CODMORPHOCIMO3`,`CUI_UMLS`) VALUES ";
        }
        else if (fichier.endsWith("cimo3_MORPHO.txt"))
        {
        	requete ="INSERT INTO `CIMO3_MORPHO` (`codmorphocimo3`, `libmorphocimo3`, `morpho_iacr`, `groupe_morpho_iacr`, `reg_5`, `reg_14`, `reg_17`) VALUES ";
        }
        else if (fichier.endsWith("cimo3_TOPO.txt"))
        {
        	requete ="INSERT INTO `CIMO3_TOPO` (`cimo3`, `libtopocimo3`, `codtopocimo3`, `reg_codtopocimo3`, `reg_libtopocimo3`, `Topo_encr`, `TOPO_IACR`, `GROUPE_TOPO_IACR`) VALUES";
        }
        else if (fichier.endsWith("TRANSCODAGE_ADICAPCIMO3_MORPHO.txt"))
        {
        	requete ="INSERT INTO `TRANSCODAGE_ADICAPCIMO3_MORPHO`(`LIBMORPHOCIMO3`, `CIMO3`, `CODMORPHOCIMO3`, `Comm`, `LESION`, `LIBLESION`, `MODIFADICAP`) VALUES";
        }
        else if (fichier.endsWith("TRANSCODAGE_ADICAPCIMO3_TOPO.txt"))
        {
        	requete ="INSERT INTO `TRANSCODAGE_ADICAPCIMO3_TOPO` (`CODTOPOCIMO3`,`ORGANE`,`TOPO`) VALUES ";
        }
		
		return requete;
	}
	
	
	String requeteSupprimerCol(Path fichier)
	{
		String requete = "";
		if (fichier.endsWith("adicap_organe.txt"))
        {
			requete = "ALTER TABLE `adicap_organe` DROP COLUMN `datemodif`;";
        }
        else if (fichier.endsWith("Anapath.txt"))
        {
        	//requete = "ALTER TABLE `anapath` DROP COLUMN `Sexe`, DROP COLUMN `DateDeNaissance`, DROP COLUMN `Prenom`, DROP COLUMN `Nom`;";
        }
        else if (fichier.endsWith("PMSI.txt"))
        {
        	requete = "ALTER TABLE `PMSI` DROP COLUMN `Sexe`, DROP COLUMN `DDN`, DROP COLUMN `Prenom`, DROP COLUMN `Nom`;";
        }
        else if (fichier.endsWith("Sejour.txt"))
        {
        	requete = "ALTER TABLE `sejour` DROP COLUMN `Sexe`, DROP COLUMN `DDN`, DROP COLUMN `Prenom`, DROP COLUMN `Nom`;";
        }
        else if (fichier.endsWith("cim10.txt"))
        {
        	requete = "ALTER TABLE `cim10` DROP COLUMN `type`, DROP COLUMN `lib`, DROP COLUMN `findevalid`, DROP COLUMN `extension`, DROP COLUMN `cma_niv`, DROP COLUMN `cma`, DROP COLUMN `cmas`, DROP COLUMN `cmas_nt`, DROP COLUMN `code_imprecis`, DROP COLUMN `interdits_en_DP`, DROP COLUMN `interdits_en_DR`, DROP COLUMN `requerant_un_DR`, DROP COLUMN `ssrFP`, DROP COLUMN `ssrMP`, DROP COLUMN `ssrAE`, DROP COLUMN `ssrDAS`, DROP COLUMN `ssrZ_CMC`, DROP COLUMN `pmsi`, DROP COLUMN `MAJ`, DROP COLUMN `auteur_MAJ`, DROP COLUMN `remarque1`, DROP COLUMN `_009_EXT__LIB__finvalid__doublon`;";
        }
        else if (fichier.endsWith("CIMO3_MORPHO.txt"))
        {
        	requete = "ALTER TABLE `CIMO3_MORPHO` DROP COLUMN `reg_5`, DROP COLUMN `reg_14`, DROP COLUMN `reg_17`;";
        
        }
        else if (fichier.endsWith("CIMO3_TOPO.txt"))
        {
        	requete = "ALTER TABLE  `CIMO3_TOPO` DROP COLUMN  `reg_codtopocimo3`, DROP COLUMN `reg_libtopocimo3`, DROP COLUMN `Topo_encr`, DROP COLUMN `TOPO_IACR`,DROP COLUMN `GROUPE_TOPO_IACR`;";

        }
        else if (fichier.endsWith("TRANSCODAGE_ADICAPCIMO3_MORPHO.txt"))
        {
        	requete = "ALTER TABLE `TRANSCODAGE_ADICAPCIMO3_MORPHO` DROP COLUMN `Comm`, DROP COLUMN `MODIFADICAP`;";
        }
		return requete;
	}
	

	String supprimerTables()
	{	
		String requete = "DROP TABLE IF EXISTS `patient`,`diagnostic`,`patient_diag`,`adicap_lesion`,`adicap_organe`,`anapath`, `anapath2`,`pmsi`, `PMSI2`,`sejour`,`cim10`,`cimo3_cui_UMLS`,`CIMO3_MORPHO`,`CIMO3_TOPO`,`TRANSCODAGE_ADICAPCIMO3_MORPHO`,`TRANSCODAGE_ADICAPCIMO3_TOPO`;";		
		return requete;
	}
}
