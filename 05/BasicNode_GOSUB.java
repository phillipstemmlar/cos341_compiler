public class BasicNode_GOSUB extends BasicNode{

	public Procedure proc;
	protected Boolean end = false;

	public BasicNode_GOSUB(Procedure p){
		super("GOSUB");
		gosub = true;
		proc = p;
	}

	private String procName(){
		if(end || proc == null) return "H"; else return proc.name();
	}

	public String line(){
		return val() + " " + procName() + "\n";
	}

	public Boolean end(){
		return end;
	}

}