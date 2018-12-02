package application.model;

import java.sql.Connection;

public abstract class DAO<T> 
{
	protected Connection connect = null;
	
	protected final String URL_RACINE = "jdbc:mysql://localhost:3306/";
	
	protected final String USER = "root";
	
	protected final String MDP = "";
	
	public abstract void initialisationConnexion(String base);
	
	public abstract void traitementTables();

}
