package application;

import java.util.ArrayList;
import java.util.List;

public class Espece {

	//	Variables
	public int id;
	public String scientificName;
	public int acceptedNameUsageID;
	
	public String rank;
	public String kingdom;
	public String phylum;
	
	//	Une liste avec tous les signalements relatifs à une espèce
	public List<Signalement> signalements = new ArrayList<Signalement>();
	
	//	Les variables ci-dessous sont potentielement toujours null, mais on les implémente au cas où
	public String classe;
	public String order;
	public String family;
	public String genus;
	public String species;
	
	public Espece(int id, String scientificName, int acceptedNameUsageID, String rank, String kingdom, String phylum, String classe, String order, String family, String genus, String species)
	{
		this.id = id;
		this.scientificName = scientificName;
		this.acceptedNameUsageID = acceptedNameUsageID;
		this.rank = rank;
		this.phylum = phylum;
		this.classe = classe;
		this.order = order;
		this.family = family;
		this.genus = genus;
		this.species = species;
	}
}