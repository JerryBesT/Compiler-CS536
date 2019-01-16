import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a cdull program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  RepeatStmtNode,
//        CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void printSpace(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++) p.print(" ");
    }
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        declList = L;
    }

    public void unparse(PrintWriter p, int indent) {
        declList.unparse(p, indent);
    }

    public void nameAls() {
        s = new SymTable();
        declList.nameAls(s);
    }

    // 1 kid
    private DeclListNode declList;
    private SymTable s;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        decls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = decls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    public void nameAls(SymTable structTable, SymTable s) {
        Iterator it = decls.iterator();
        try {
            while(it.hasNext()) {
                ((VarDeclNode)it.next()).nameAls(structTable, s);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
        
    }

    public void nameAls(SymTable s) {
        Iterator it = decls.iterator();
        try {
            while(it.hasNext()) {
                ((DeclNode)it.next()).nameAls(s);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
        
    }

    // list of kids (DeclNodes)
    private List<DeclNode> decls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        formalDecls = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = formalDecls.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    public void nameAls(FnSym fn, SymTable s) {
        Iterator<FormalDeclNode> it = formalDecls.iterator();
        try {
            while(it.hasNext()) {
                ((FormalDeclNode)it.next()).nameAls(fn, s);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in FormalsListNode.print");
            System.exit(-1);
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> formalDecls;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        this.declList = declList;
        this.stmtList = stmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        declList.unparse(p, indent);
        stmtList.unparse(p, indent);
    }

    public void nameAls(SymTable s) {
        declList.nameAls(s);
        stmtList.nameAls(s);
    }

    // 2 kids
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        stmts = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = stmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    public void nameAls(SymTable s){
        Iterator<StmtNode> it = stmts.iterator();
        try {
            while(it.hasNext()) {
                ((StmtNode)it.next()).nameAls(s);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in StmtNode.print");
            System.exit(-1);
        } 
    }

    // list of kids (StmtNodes)
    private List<StmtNode> stmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        exps = S;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = exps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }
    
    // possibly wrong again and again
    public void nameAls(SymTable s){
        Iterator<ExpNode> it = exps.iterator();
        try {
            while(it.hasNext()) {
                ((ExpNode)it.next()).nameAls(s);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in FormalsListNode.print");
            System.exit(-1);
        } 
    }

    // list of kids (ExpNodes)
    private List<ExpNode> exps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    abstract public void nameAls(SymTable s);
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        this.type = type;
        this.id = id;
        this.size = size;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        type.unparse(p, 0);
        p.print(" ");
        id.unparse(p, 0);
        p.println(";");
    }
    public void nameAls(SymTable s) {
        this.nameAls(s, s);
    }

    public void nameAls(SymTable structTable, SymTable s) {
        String typeName = type.getSym().getType();
        if(typeName.equals("void")) {
            ErrMsg.badVoid(id.getLineNum(), id.getCharNum(),
                            "Non-function declared void");    
            return;
        }

        // first check if the type is struct
        if(typeName.equals("struct")) {
            String structId = ((StructNode)type).getId();
            // if no such struct in program
            if(s.lookupGlobal(structId) == null) {
                ErrMsg.undeclared(id.getLineNum(), id.getCharNum(),
                            "Undeclared identifier");    
                return;
            } 
            else {
                // if the found one is not struct type
                if(!s.lookupGlobal(structId).getType().equals("struct")) {
                    ErrMsg.badDecl(id.getLineNum(), id.getCharNum(), 
                                    "Invalid name of struct type");
                    return;
                }
            }
            
            StructDefSym def = (StructDefSym)(s.lookupGlobal(structId));
            // this del contains informations of the struct body
            StructDeclSym del = new StructDeclSym(def, structId);
            try {
                structTable.addDecl(id.getString(), del);
            } catch (DuplicateSymException ex) {
                ErrMsg.duplicate(id.getLineNum(), id.getCharNum(),
                        "Multiply declared identifier");    
            } catch (EmptySymTableException ex) { 
                System.err.println("There is no scope!!");
            } catch (WrongArgumentException ex) {
                System.err.println(ex);
            }
            return;
	    }

        // if it is variable, add to scope
        try {
            structTable.addDecl(id.getString(), type.getSym());
            //id.nameAls(structTable);
        } catch (DuplicateSymException ex) {
            ErrMsg.duplicate(id.getLineNum(), id.getCharNum(),
                        "Multiply declared identifier");    
        } catch (EmptySymTableException ex) { 
            System.err.println("There is no scope!!");
        } catch (WrongArgumentException ex) {
            System.err.println(ex);
        }
    }


    // 3 kids
    private TypeNode type;
    private IdNode id;
    private int size;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        this.type = type;
        this.id = id;
        formalsList = formalList;
        fnBody = body;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        type.unparse(p, 0);
        p.print(" ");
        id.unparse(p, 0);
        p.print("(");
        formalsList.unparse(p, 0);
        p.println(") {");
        fnBody.unparse(p, indent+4);
        p.println("}\n");
    }

    public void nameAls(SymTable s) {
        FnSym curr = new FnSym(type.getSym(), "fn");
        try {
            s.addDecl(id.getString(), curr);
        } catch (DuplicateSymException ex) {
            ErrMsg.duplicate(id.getLineNum(), id.getCharNum(),
                        "Multiply declared identifier");    
        } catch (EmptySymTableException ex) { 
            System.err.println("There is no scope!!");
        } catch (WrongArgumentException ex) {
            System.err.println(ex);
        }
        
        s.addScope();
        formalsList.nameAls(curr, s);
        fnBody.nameAls(s);

        try {
            s.removeScope(); 
        } catch (EmptySymTableException ex) {
            System.err.println("There is no scope!!"); 
        }
    }

    // 4 kids
    private TypeNode type;
    private IdNode id;
    private FormalsListNode formalsList;
    private FnBodyNode fnBody;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        this.type = type;
        this.id = id;
    }

    public void unparse(PrintWriter p, int indent) {
        type.unparse(p, 0);
        p.print(" ");
        id.unparse(p, 0);
    }

    public void nameAls(SymTable s){}

    public void nameAls(FnSym fn, SymTable s) {
        String typeName = type.getSym().getType();
        if(typeName.equals("void")) {
            ErrMsg.badVoid(id.getLineNum(), id.getCharNum(),
                            "Non-function declared void");    
            return;
        }

        try {
            s.addDecl(id.getString(), type.getSym());
            fn.addFormal(type.getSym().toString());
        } catch (DuplicateSymException ex) {
            ErrMsg.duplicate(id.getLineNum(), id.getCharNum(),
                        "Multiply declared identifier");    
        } catch (EmptySymTableException ex) { 
            System.err.println("There is no scope!!");
        } catch (WrongArgumentException ex) {
            System.err.println(ex);
        }
        
    }

    // 2 kids
    private TypeNode type;
    private IdNode id;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        this.id = id;
        this.declList = declList;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("struct ");
        id.unparse(p, 0);
        p.println("{");
        declList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("};\n");

    }

    public void nameAls(SymTable s) {
	    SymTable structTable = new SymTable();
	    declList.nameAls(structTable, s);

        StructDefSym curr = new StructDefSym(structTable, "struct");

        try {
            s.addDecl(id.getString(), curr);
        } catch (DuplicateSymException ex) {
            ErrMsg.duplicate(id.getLineNum(), id.getCharNum(),
                        "Multiply declared identifier");    
        } catch (EmptySymTableException ex) { 
            System.err.println("There is no scope!!");
        } catch (WrongArgumentException ex) {
            System.err.println(ex);
        }

    }

    // 2 kids
    private IdNode id;
    private DeclListNode declList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    // possibly wrong again
    abstract public Sym getSym();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }

    public Sym getSym() {
        return new Sym("int");
    }

}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }

    public Sym getSym() {
        return new Sym("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }

    public Sym getSym() {
        return new Sym("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        this.id = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        id.unparse(p, 0);
    }

    public Sym getSym() {
        return new Sym("struct");
    }

    public String getId() {
        return id.getString();
    }
    
    // 1 kid
    private IdNode id;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAls(SymTable s);
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        this.assign = assign;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        assign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    public void nameAls(SymTable s) {
        assign.nameAls(s);
    }

    // 1 kid
    private AssignNode assign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        exp.unparse(p, 0);
        p.println("++;");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
    // 1 kid
    private ExpNode exp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        exp.unparse(p, 0);
        p.println("--;");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
    // 1 kid
    private ExpNode exp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("cin >> ");
        exp.unparse(p, 0);
        p.println(";");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode exp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("cout << ");
        exp.unparse(p, 0);
        p.println(";");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
    // 1 kid
    private ExpNode exp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        declList = dlist;
        this.exp = exp;
        stmtList = slist;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("if (");
        exp.unparse(p, 0);
        p.println(") {");
        declList.unparse(p, indent+4);
        stmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
        s.addScope();
        declList.nameAls(s);
        stmtList.nameAls(s);

        try {
            s.removeScope(); 
        } catch (EmptySymTableException ex) {
            System.err.println("There is no scope!!"); 
        }
    }
    // e kids
    private ExpNode exp;
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode thenDeclList,
                          StmtListNode thenStmtList, DeclListNode elseDeclList,
                          StmtListNode elseStmtList) {
        this.exp = exp;
        this.thenDeclList = thenDeclList;
        this.thenStmtList = thenStmtList;
        this.elseDeclList = elseDeclList;
        this.elseStmtList = elseStmtList;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("if (");
        exp.unparse(p, 0);
        p.println(") {");
        thenDeclList.unparse(p, indent+4);
        thenStmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
        printSpace(p, indent);
        p.println("else {");
        elseDeclList.unparse(p, indent+4);
        elseStmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");        
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
        s.addScope();
        thenDeclList.nameAls(s);
        thenStmtList.nameAls(s);

        try {
            s.removeScope(); 
        } catch (EmptySymTableException ex) {
            System.err.println("There is no scope!!"); 
        }
        
        s.addScope();
        elseDeclList.nameAls(s);
        elseStmtList.nameAls(s);

        try {
            s.removeScope(); 
        } catch (EmptySymTableException ex) {
            System.err.println("There is no scope!!"); 
        }
    }
    // 5 kids
    private ExpNode exp;
    private DeclListNode thenDeclList;
    private StmtListNode thenStmtList;
    private StmtListNode elseStmtList;
    private DeclListNode elseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        this.exp = exp;
        declList = dlist;
        stmtList = slist;
    }
    
    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("while (");
        exp.unparse(p, 0);
        p.println(") {");
        declList.unparse(p, indent+4);
        stmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
        s.addScope();
        declList.nameAls(s);
        stmtList.nameAls(s);

        try {
            s.removeScope(); 
        } catch (EmptySymTableException ex) {
            System.err.println("There is no scope!!"); 
        }
    }
    // 3 kids
    private ExpNode exp;
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        this.exp = exp;
        declList = dlist;
        stmtList = slist;
    }
	
    public void unparse(PrintWriter p, int indent) {
	printSpace(p, indent);
        p.print("repeat (");
        exp.unparse(p, 0);
        p.println(") {");
        declList.unparse(p, indent+4);
        stmtList.unparse(p, indent+4);
        printSpace(p, indent);
        p.println("}");
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
        s.addScope();
        declList.nameAls(s);
        stmtList.nameAls(s);

        try {
            s.removeScope(); 
        } catch (EmptySymTableException ex) {
            System.err.println("There is no scope!!"); 
        }
    }
    // 3 kids
    private ExpNode exp;
    private DeclListNode declList;
    private StmtListNode stmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        callExp = call;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        callExp.unparse(p, indent);
        p.println(";");
    }

    public void nameAls(SymTable s) {
        callExp.nameAls(s);
    }
    // 1 kid
    private CallExpNode callExp;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        this.exp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        printSpace(p, indent);
        p.print("return");
        if (exp != null) {
            p.print(" ");
            exp.unparse(p, 0);
        }
        p.println(";");
    }

    public void nameAls(SymTable s) {
        if(exp != null)
            exp.nameAls(s);
    }
    // 1 kid
    private ExpNode exp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    abstract public void nameAls(SymTable s);
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.intVal = intVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(intVal);
    }

    public void nameAls(SymTable s) {
    }
    private int lineNum;
    private int charNum;
    private int intVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.strVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(strVal);
    }

    public void nameAls(SymTable s) {
    }
    private int lineNum;
    private int charNum;
    private String strVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        this.lineNum = lineNum;
        this.charNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    public void nameAls(SymTable s) {
    }
    private int lineNum;
    private int charNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        this.lineNum = lineNum;
        this.charNum = charNum;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    public void nameAls(SymTable s) {
    }
    private int lineNum;
    private int charNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        this.lineNum = lineNum;
        this.charNum = charNum;
        this.strVal = strVal;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(strVal);
        if(this.sym != null) {
            p.print("(");
            p.print(sym.getType());
            p.print(")");
        }
    }

    public Sym getSym() {
        return this.sym;
    }

    public String getString() {
        return strVal;
    }

    public int getCharNum() {
        return charNum;
    }

    public int getLineNum() {
        return lineNum;
    }

    // should be checking local first, then global
    public void nameAls(SymTable s) {
        Sym local = s.lookupLocal(this.strVal);
        if(local != null) {
            this.sym = local;
            return;
        }

        Sym global = s.lookupGlobal(this.strVal);
        if(global != null) {
            this.sym = global;
            return;
        }

        ErrMsg.undeclared(this.lineNum, this.getCharNum(),
                            "Undeclared identifier");    
    }

    public void setForDot(Sym k) {
        this.sym = k;
    }

    private int lineNum;
    private int charNum;
    private String strVal;
    private Sym sym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        this.loc = loc;    
        this.id = id;
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        loc.unparse(p, 0);
        p.print(").");
        id.unparse(p, 0);
    }

    public void nameAls(SymTable s) {
        err = false;
        loc.nameAls(s);
 
        if(loc instanceof IdNode) {
            if(((IdNode)loc).getSym() == null) {
                ErrMsg.badDotLHS(((IdNode)id).getLineNum(), ((IdNode)id).getCharNum(),
                                "Dot-access of non-struct type");     
                err = true;
                return;
            }
            else {
                // if the loc is not null and not a struct type
                if(!(((IdNode)loc).getSym() instanceof StructDeclSym)) {
                    ErrMsg.badDotLHS(((IdNode)id).getLineNum(), ((IdNode)id).getCharNum(),
                                    "Dot-access of non-struct type");     
                    err = true;
                    return;
                }

                StructDeclSym left = (StructDeclSym)((IdNode)loc).getSym();
                StructDefSym type = left.getBody();
                SymTable structTable = type.getList();
                if(structTable.lookupLocal(id.getString()) == null) {
                    ErrMsg.badDotRHS(id.getLineNum(), id.getCharNum(),
                                "Invalid struct field name");     
                    err = true;
                    return;
                }
    
                if(structTable.lookupLocal(id.getString()) instanceof Sym)
                    id.setForDot(structTable.lookupLocal(id.getString()));

                if(structTable.lookupLocal(id.getString()) instanceof StructDeclSym) {
                    this.prev = (StructDeclSym)structTable.lookupLocal(id.getString());
                    id.setForDot((StructDeclSym)structTable.lookupLocal(id.getString()));
                }
            } 
        }

        if(loc instanceof DotAccessExpNode) {
            if(((DotAccessExpNode)loc).err == true) {
                err = true;
                return;
            }
            StructDeclSym left = ((DotAccessExpNode)loc).prev;
            if(left == null) {
                err = true;
                return;
            }
            StructDefSym type = left.getBody();
            SymTable structTable = type.getList();
            if(structTable.lookupLocal(id.getString()) == null) {
                ErrMsg.badDotRHS(id.getLineNum(), id.getCharNum(),
                                "Invalid struct field name");     
                err = true;
                return;
            } 
            if(structTable.lookupLocal(id.getString()) instanceof Sym)
                id.setForDot(structTable.lookupLocal(id.getString()));

            if(structTable.lookupLocal(id.getString()) instanceof StructDeclSym) {
                this.prev = (StructDeclSym)structTable.lookupLocal(id.getString());
                id.setForDot((StructDeclSym)structTable.lookupLocal(id.getString()));
            }
        }
    }

    // 2 kids
    private boolean err;
    private StructDeclSym prev;  
    private ExpNode loc;    
    private IdNode id;
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        this.lhs = lhs;
        this.exp = exp;
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        lhs.unparse(p, 0);
        p.print(" = ");
        exp.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    public void nameAls(SymTable s) {
        //unfinished
        lhs.nameAls(s);
        exp.nameAls(s);
    }
    // 2 kids
    private ExpNode lhs;
    private ExpNode exp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        id = name;
        expList = elist;
    }

    public CallExpNode(IdNode name) {
        id = name;
        expList = new ExpListNode(new LinkedList<ExpNode>());
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        id.unparse(p, 0);
        p.print("(");
        if (expList != null) {
            expList.unparse(p, 0);
        }
        p.print(")");
    }

    public void nameAls(SymTable s) {
        id.nameAls(s);
        if(expList != null)
            expList.nameAls(s);
    }
    // 2 kids
    private IdNode id;
    private ExpListNode expList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        this.exp = exp;
    }

    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
    // one child
    protected ExpNode exp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
    // two kids
    protected ExpNode exp1;
    protected ExpNode exp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        exp.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        exp.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp.nameAls(s);
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

class PlusNode extends BinaryExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" + ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class MinusNode extends BinaryExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" - ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class TimesNode extends BinaryExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" * ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class DivideNode extends BinaryExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" / ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class AndNode extends BinaryExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" && ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
    }
}

class OrNode extends BinaryExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" || ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class EqualsNode extends BinaryExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" == ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class NotEqualsNode extends BinaryExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" != ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class LessNode extends BinaryExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" < ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class GreaterNode extends BinaryExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" > ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class LessEqNode extends BinaryExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" <= ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}

class GreaterEqNode extends BinaryExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        exp1.unparse(p, 0);
        p.print(" >= ");
        exp2.unparse(p, 0);
        p.print(")");
    }
    public void nameAls(SymTable s) {
        exp1.nameAls(s);
        exp2.nameAls(s);
    }
}
