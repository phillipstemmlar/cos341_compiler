class Variable{

	public Type type;
	public String ogname;

	private static int count = 0;
	public int ID;

	public Integer index;
	public Integer scopeLevel;

	public Variable(Type type, String name, Integer ind, Integer scopelvl){
		this.type = type;
		this.ogname = name;
		this.ID = count++;
		index = ind;
		scopeLevel = scopelvl;

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

	public static Integer IDfromName(String name){
		return Integer.parseInt(name.substring(1));
	}

	public static enum Type{num,string,bool,undefined}
}