import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

import gov.nih.nlm.uts.webservice.AtomDTO;
import gov.nih.nlm.uts.webservice.AttributeDTO;
import gov.nih.nlm.uts.webservice.Psf;
import gov.nih.nlm.uts.webservice.SourceAtomClusterDTO;
import gov.nih.nlm.uts.webservice.UiLabel;
import gov.nih.nlm.uts.webservice.UiLabelRootSource;
import gov.nih.nlm.uts.webservice.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.UtsWsContentController;
import gov.nih.nlm.uts.webservice.UtsWsContentControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsFinderController;
import gov.nih.nlm.uts.webservice.UtsWsFinderControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsMetadataController;
import gov.nih.nlm.uts.webservice.UtsWsMetadataControllerImplService;
import gov.nih.nlm.uts.webservice.UtsWsSecurityController;
import gov.nih.nlm.uts.webservice.UtsWsSecurityControllerImplService;

public class Main {

	private static String ticketGrantingTicket;
	private static UtsWsSecurityController utsSecurityService;
	private static UtsWsMetadataController utsMetadataService;
	private static UtsWsContentController utsContentService;
	private static String singleUseTicket;
	private static String serviceName;

	
	public static void main(String[] args) throws UtsFault_Exception {	
		//__________________________________________________________________________________________________________
			
		try {
			 utsSecurityService = (new UtsWsSecurityControllerImplService()).getUtsWsSecurityControllerImplPort();
			 utsMetadataService = (new UtsWsMetadataControllerImplService()).getUtsWsMetadataControllerImplPort();
			 utsContentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();
			 
			 
			} catch (Exception e) {
			 System.out.println("Error!!!" + e.getMessage());
			}
		//__________________________________________________________________________________________________________
			
		String url = "jdbc:mysql://localhost/ime";
        String user = "root";
        String pwd = "";
        
        try {
        	Class.forName("com.mysql.jdbc.Driver").newInstance();
            System.out.println("Driver O.K.");
            Connection con = DriverManager.getConnection(url, user, pwd);
            System.out.println("Connexion effective !"); 	
		
	        Map<String, String> map = new HashMap<String, String>();
			java.sql.Statement stt = con.createStatement();
			java.sql.ResultSet res = stt.executeQuery("SELECT CUI_UMLS "
	        		+ "FROM  cimo3_cui_umls "); 
			 while (res.next()) {
				 map.put(res.getString("CUI_UMLS"), "");
			 }
			 
			 System.out.println(map.isEmpty()); 
	
			
			// Authentification 
			// Step 1 - Proxy Grant ticket
				String username = "deannawung";
				String password = "m2sitistermino#";
				
				try {
					ticketGrantingTicket = utsSecurityService.getProxyGrantTicket(username, password);
				} catch (Exception e) {
					System.out.println("Ticket failed");
				}
				
			// Step 2 - Single use ticket (service ticket) 
				serviceName = "http://umlsks.nlm.nih.gov";
				newTicket();			
		
				
				//Setp 3 - Preparation du myPsf
				String currentUmlsRelease = utsMetadataService.getCurrentUMLSVersion(singleUseTicket);
				Psf myPsf = new Psf();	
				myPsf.setIncludeObsolete(false);
				myPsf.setIncludeSuppressible(false);	
				myPsf.getIncludedSources().add("ICD10CM");	
				
				List<AtomDTO> atoms = new ArrayList<AtomDTO>();
				List<String> result_ui = new ArrayList<String>(); //recupere les codes CIM10
				System.out.println("top"); 
				// Application de la methode pour chaque cle de la map
				for(String key: map.keySet()) {
					newTicket();
					atoms = utsContentService.getConceptAtoms(singleUseTicket, currentUmlsRelease, key, myPsf);
					if(atoms.isEmpty() == false) {
						for (AtomDTO atom:atoms) {				
							String sourceId = atom.getCode().getUi();
					        result_ui.add(0, sourceId);
						}
					map.put(key, result_ui.get(0));				
					}
					//else {
						//map.remove(key, "");
					//}
					
					//System.out.println(map.keySet());
					//System.out.println(map.values());
				}	
				System.out.println("methode done"); 
				
				// Association dans la Map des codes CIM10 a leur code UMLS associe 
				String requete="CREATE TABLE IF NOT EXISTS UMLS_CIM10 (`CodeCIM10` VARCHAR(50), `CodeUMLS` VARCHAR(50))";
				stt.execute(requete);
				System.out.println("fini 2");
				for (Map.Entry<String, String> entry:map.entrySet()) {
					String codeUMLS = entry.getKey();
					String codeCIM10 = entry.getValue();
					String insert = "INSERT INTO UMLS_CIM10 (CodeCIM10, CodeUMLS) VALUES (?,?)";	
					java.sql.PreparedStatement prepare = con.prepareStatement(insert);
					prepare.setString(1, codeCIM10);
					prepare.setString(2, codeUMLS);
					prepare.execute();				
				}
				System.out.println("associations done");
	

        	}catch (Exception e) {
	            e.printStackTrace();
	          }
	}
        
        
	
	private static  String getProxyTicket(String ticket, String serviceName){	 
			try {
				return utsSecurityService.getProxyTicket(ticket, serviceName);
			} catch (Exception e) {
				return "";
			}
			 	 
	}
	
	private static void newTicket() {
		singleUseTicket = getProxyTicket(ticketGrantingTicket, serviceName);
	}

		/**
	SELECT *
	FROM diagnostic as D, patient_diag as PD, patient as P, cimo3_cui_umls as C, umls_cim10 as U
	WHERE D.TypeCode='2' AND D.NumDiag = PD.NumDiag AND PD.NumPatient=P.NumPatient AND D.CodeDiag=U.CodeCIM10 AND U.CodeUMLS=C.CUI_UMLS;
	
	}**/
}

