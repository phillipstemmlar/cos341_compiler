class Procedure{

	public String ogname;
	public Scope scope = null;

	private static int count = 0;
	public int ID;

	public Integer index;
	public Integer scopeLevel;

	public Procedure(String name, Integer ind, Integer scopelvl){
		this.ogname = name;
		scope = null;
		this.ID = count++;
		index = ind;
		scopeLevel = scopelvl;

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

	public String toString(){
		return name();
	}
}