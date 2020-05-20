public class BasicNode{

	private String line_value;
	protected Boolean label = false;
	protected Boolean goto_ = false;
	protected Boolean gosub = false;

	public BasicNode(String line){
		line_value = line;
	}

	public String val(){
		return line_value;
	}

	public String line(){
		return val() + "\n";
	}

	public Boolean isLABEL(){
		return label;
	}

	public Boolean isGOTO(){
		return goto_;
	}

	public Boolean isGOSUB(){
		return gosub;
	}
}