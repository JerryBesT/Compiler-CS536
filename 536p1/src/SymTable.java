

import java.util.*;

/**
 * Symbol table is a data structure that stores the identifiers
 * and information about each identifier.
 * 
 * This symbol table is implemented using a List of HashMaps
 * I use linked list since we always deal with the beginning of the list
 * add or remove first entry of a linked list is simple and convenient
 */
public class SymTable {
	private List<HashMap<String, Sym>> table;
	
	/**
	 * the constructor of the symbol table, we add a new HashMap 
	 * to the table when a new table is constructed
	 */
	public SymTable(){
		table = new LinkedList<HashMap<String, Sym>>();
		table.add(0, new HashMap<String, Sym>());
	}
	
	/**
	 * add name and symbol as a key-value pair to current scope 
	 * 
	 * @param name, sym
	 * @throws DuplicateSymException, 
	 * @throws EmptySymTableException, NullPointerException
	 */
	public void addDecl(String name, Sym sym) throws DuplicateSymException, 
	EmptySymTableException, NullPointerException{
		
		//if this SymTable's list is empty, throw an EmptySymTableException
		if(table.size() ==0){
			throw new EmptySymTableException();
		}
		
		//if either name or sym (or both) is null, throw a NullPointerException
		if (name ==null || sym == null){
			throw new NullPointerException();
		}
		
		//if the first HashMap in the list already 
		//contains the given name as a key, throw a DuplicateSymException
		if (table.get(0).containsKey(name)){
			throw new DuplicateSymException();
		}
		
		//add name and sym as a key-value pair to current scope
		table.get(0).put(name, sym);
	}
	
	/**
	 * Add a new, empty HashMap to the front of the list.
	 */
	public void addScope(){
		//add new HashMap to the front of the list
		table.add(0, new HashMap<String, Sym>());
	}
	
	/**
	 * this method check if current scope contains name as a key, return 
	 * the associated Sym, otherwise return null
	 * 
	 * @param name
	 * @return sym
	 * @throws EmptySymTableException
	 */
	public Sym lookupLocal(String name) throws EmptySymTableException{
		Sym localSym = null;
		
		//if this SymTable's list is empty, throw an EmptySymTableException
		if(table.size() ==0){
			throw new EmptySymTableException();
		}
		
		//return associated sym if find the given key, otherwise return null
		if(table.get(0).containsKey(name)){
			localSym = table.get(0).get(name);
		}
		return localSym;
	}
	
	/**
	 * This method check if any HashMap in the list contains name as a key, 
	 * return the first associated Sym
	 * 
	 * @param name
	 * @return sym
	 * @throws EmptySymTableException
	 */
	public Sym lookupGlobal(String name) throws EmptySymTableException{
		//if this SymTable's list is empty, throw an EmptySymTableException
		if(table.size() ==0){
			throw new EmptySymTableException();
		}
		
		//iterate through all HashMaps, see if any HashMap contains name as key
		for (HashMap<String, Sym> scope: table){
			if(scope.containsKey(name)){
				return scope.get(name);	
			}
		}
		return null; //if not found
		
	}
	
	/**
	 * This method removes current scope (delete the HashMap at front)
	 * 
	 * @throws EmptySymTableException
	 */
	public void removeScope() throws EmptySymTableException{
		//if this SymTable's list is empty, throw an EmptySymTableException
		if(table.size() ==0){
			throw new EmptySymTableException();
		}
		
		//delete current scope
		table.remove(0);
	}
	
	/**
	 * print key-value pairs in all scopes in the symbol table
	 */
	public void print(){
		System.out.println("\nSym Table\n");
		//iterate through every scope(HashMap)
		for (HashMap<String, Sym> scope: table){
			System.out.println(scope.toString());
		}
		System.out.println();
	}
	
}
