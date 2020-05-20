import java.util.*;

class Variable{

	public final Type type;
	public String ogname;

	private static int count = 0;
	public int ID;

	public Integer index;
	public Integer scopeLevel;
	public Variable.Confirmation hasValue;

	public Integer initValueIndex = -1;

	public Variable(Type type, String name, Integer ind, Integer scopelvl){
		this.type = type;
		this.ogname = name;
		this.ID = count++;
		index = ind;
		scopeLevel = scopelvl;
		hasValue = Variable.Confirmation.no;
		// System.out.println("---> New VAR: " + name() + "\t" + type + "\t" + ogname);
	}

	public String name(){
		return "V" + ID;
	}

	public static String noName(){
		return "U";
	}

	public String toString(){
		return name();
	}

	public Integer lvl(){
		return scopeLevel;
	}

	public Integer index(){
		return index;
	}

	public Variable.Confirmation hasVal(){
		return hasValue;
	}

	public void hasVal(Variable.Confirmation b){
		hasValue = b;
	}

	public Integer declLine(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			return table.get(index()).line();
		}
		return null;
	}

	public static Integer IDfromName(String name){
		return Integer.parseInt(name.substring(1));
	}

	public static Type notype(){
		return Type.notype;
	}

	public static Type undefined(){
		return Type.undefined;
	}

	public static int count(){
		return count;
	}

	public static enum Type{num,string,bool,procedure,undefined,none,notype}
	public static enum Confirmation{yes,no,maybe}
}