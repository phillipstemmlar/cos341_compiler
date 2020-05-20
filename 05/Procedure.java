import java.util.*;

class Procedure{

	public String ogname;
	public Scope declScope = null;
	public Scope innerScope = null;

	private static int count = 0;
	public int ID;

	public Integer index;
	public Integer basic_line = -1;
	public Boolean first = false;
	public static Boolean foundFirst = false;

	public Procedure(String name, Integer ind, Scope scope_, Scope innerScope_){
		this.ogname = name;
		this.ID = count++;
		index = ind;
		declScope = scope_;
		innerScope = innerScope_;
		if(innerScope != null) innerScope.declProc = this;
		// System.out.println("---> New PROC: " + name() + "\t" + ogname);
	}

	public String name(){
		return "P" + ID;
	}

	public static String noName(){
		return "U";
	}

	public static Integer IDfromName(String name){
		return Integer.parseInt(name.substring(1));
	}

	public Integer lvl(){
		return declScope.lvl();
	}

	public Integer scopeID(){
		return declScope.ID();
	}

	public Integer innerScopeID(){
		return innerScope.ID();
	}
	public Integer innerLvl(){
		return innerScope.lvl();
	}

	public Integer index(){
		return index;
	}

	public SyntaxNode innerNode(){
		if(innerScope == null) return null;
		return innerScope.scopeRoot;
	}

	public Integer declLine(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			return table.get(index()).line();
		}
		return null;
	}

	public String toString(){
		return name() + ":" + basic_line;
	}

	public static final Variable.Type Type = Helper.tokenToType(Token.eToken.PROC);
}