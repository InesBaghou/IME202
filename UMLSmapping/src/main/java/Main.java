import java.util.ArrayList; 
import java.util.List;
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
	
			
			//Search
			String currentUmlsRelease = utsMetadataService.getCurrentUMLSVersion(singleUseTicket);
			
			Psf myPsf = new Psf();	
			
			myPsf.setIncludeObsolete(false);
			myPsf.setIncludeSuppressible(false);
			

			myPsf.getIncludedSources().add("ICD10CM");	

			
			List<AtomDTO> atoms = new ArrayList<AtomDTO>();
			List<String> result_ui = new ArrayList<String>();
			
			newTicket();
			
		    atoms = utsContentService.getConceptAtoms(singleUseTicket, currentUmlsRelease, "C0496758", myPsf);
		    
		    for (AtomDTO atom:atoms) {				
		        String aui = atom.getUi();
		        String tty = atom.getTermType();
		        String name = atom.getTermString().getName();
		        String sourceId = atom.getCode().getUi();
		        result_ui.add(sourceId);	       
		        result_ui.add(name);
		        }


			System.out.println(result_ui);
	
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


	
}

