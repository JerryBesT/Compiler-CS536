import java.util.*;

public class Sym {
	private String type;

	public Sym(String type) {
		this.type = type;
	}


	public String getType() {
		return type;
	}

	public String toString() {
		return type;
	}

}

class StructDefSym extends Sym {
	private String type;
	private SymTable structList;

	public StructDefSym(SymTable s, String type) {
		super(type);
		this.type = type;
        this.structList = s;
	}        

	public String getType() {
		return type;
	}

	public String toString() {
		return type;
	}

	// is the sym table field gonna change when the ast.java sym table changes? 
	public SymTable getList() {
		return this.structList;
	}
}


class StructDeclSym extends Sym {
	private String name;
	private StructDefSym body;

	public StructDeclSym(StructDefSym body, String name) {
		super(name);
		this.name = name;
		this.body = body;
	}        

	public String getName() {
		return this.name;
	}

	public StructDefSym getBody() {
		return this.body;
	}
}

class FnSym extends Sym {
	private String type;
	private Sym retType;
    private List<String> formals;

	public FnSym(Sym ret, String type) {
		super(type);
		this.type = type;
        this.retType = ret;
        formals = new ArrayList<>();
	}        

	public String getType() {
        if(formals.size() == 0)
            return "->" + this.retType.getType();
        String format = "";
        for(int i = 0;i < this.formals.size();i++) {
            if(i == formals.size() - 1)
                format += this.formals.get(i) + "->" + this.retType.getType();
            else
                format += this.formals.get(i) + ",";
        }
        return format;
	}

	public String toString() {
		return type;
	}

    public void addFormal(String type) {
        this.formals.add(type);
    }

	public Sym getReturn() {
		return this.retType;
	}     
}
