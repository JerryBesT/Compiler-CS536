
/**
 * The symbol class is used to store the type of a specific symbol
 */
public class Sym {
	String type;
	
	/**
	 * constructor of the Sym class, it contains type of the symbol
	 */
	public Sym(String type){
		this.type = type;
	}
	
	/**
	 * return the type of this symbol
	 * @return String
	 */
	public String getType(){
		return this.type;
	}
	
	/**
	 * return the type of this symbol (will have future modification)
	 * @return String
	 */
	public String toString(){
		return this.type;
	}
}
