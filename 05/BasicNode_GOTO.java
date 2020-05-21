public class BasicNode_GOTO extends BasicNode{

	String jumpTo;

	public BasicNode_GOTO(String Label){
		super("GOTO");
		goto_ = true;
		jumpTo = Label;
	}

	public String JumpTo(){
		return jumpTo;
	}

	public void JumpTo(String jmp){
		jumpTo = jmp;
	}

	public String str(){
		return val() + " " + JumpTo() + "\n";
	}

}