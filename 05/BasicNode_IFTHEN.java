public class BasicNode_IFTHEN extends BasicNode_GOTO{
	String Condition;
	
	public BasicNode_IFTHEN(String Bool, String Label){
		super(Label);
		Condition = Bool;		
	}

	public String str(){
		return "IF " + Condition + " THEN " + val() + " " + JumpTo() + "\n";
	}

	public Boolean end(){
		return end;
	}

}