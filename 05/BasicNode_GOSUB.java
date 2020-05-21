public class BasicNode_GOSUB extends BasicNode{

	String jumpTo;

	public BasicNode_GOSUB(String Label){
		super("GOSUB");
		gosub = true;
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