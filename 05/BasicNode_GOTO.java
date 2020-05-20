public class BasicNode_GOTO extends BasicNode{

	public BasicNode_GOTO(String line){
		super(line);
		goto_ = true;
	}
	
	public String line(){
		return val() + "\n";
	}


}