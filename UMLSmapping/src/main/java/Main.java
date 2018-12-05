import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import gov.nih.nlm.uts.webservice.AtomDTO;
import gov.nih.nlm.uts.webservice.Psf;
import gov.nih.nlm.uts.webservice.UtsFault_Exception;
import gov.nih.nlm.uts.webservice.UtsWsContentController;
import gov.nih.nlm.uts.webservice.UtsWsContentControllerImplService;
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
		long startTime = System.nanoTime(); // start Timer
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
		
	        Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>(); 
	        //key is UMLS, ArrayList contains all matching CIM10 possibilities
			java.sql.Statement stt = con.createStatement();
			java.sql.ResultSet res = stt.executeQuery("SELECT CUI_UMLS "
	        		+ "FROM  cimo3_cui_umls ");
			
			//get UMLS code results from table 
			 while (res.next()) { // get while there are results,
				 ArrayList<String> myArray = new ArrayList<String>();
				 map.put(res.getString("CUI_UMLS"), myArray); //populate hashmap with UMLS code, leave other side empty 
			 }
			 
			 System.out.println(map.isEmpty()); 
	
			
			// Authentification to connect to API 
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
				
				
			//Step 3 - Preparation du myPsf
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
					        result_ui.add(0, sourceId.replaceAll("[\\s\\p{Punct}]","")); //remove dots from CIM10 code
						}
					map.get(key).add(result_ui.get(0));	//get key (umls code) and add CIM10 code to arraylist			
					}

				}	
				
				//remove empty values in hashmap (UMLS codes with no CIM10 code)
				Iterator<Map.Entry<String, ArrayList<String>>> it = map.entrySet().iterator();
				while (it.hasNext()) {
				    Map.Entry<String, ArrayList<String>> e = it.next();
				    String key = e.getKey();
				    ArrayList<String>value = e.getValue();
				    if (value.isEmpty()) {
				        it.remove();
				    }
				}
				System.out.println("hashmap done");
				
				//System.out.println(map); //print map
				
				// Association dans la Map des codes CIM10 a leur code UMLS associe 
				String requete="CREATE TABLE IF NOT EXISTS UMLS_CIM10 (`CodeCIM10` VARCHAR(50), `CodeUMLS` VARCHAR(50))";
				stt.execute(requete);
				System.out.println("fini 2");
				for (Map.Entry<String, ArrayList<String>> entry:map.entrySet()) {
					String codeUMLS = entry.getKey();
					String codeCIM10 = entry.getValue().get(0);  //get first value of arraylist (there is only one value anyway)
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
        
        // end Timer and print time
        long endTime   = System.nanoTime();
    	long totalTime = (endTime - startTime)/(1000000000*60);
    	System.out.println("Total time : "+totalTime+" mins");
        
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
	//la requete maaaarche :)
	}**/
}