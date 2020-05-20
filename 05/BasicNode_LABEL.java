public class BasicNode_LABEL extends BasicNode{

	public BasicNode_LABEL(String line){
		super(line);
		label = true;
	}
	
	public String line(){
		return val() + ":\n";
	}


}