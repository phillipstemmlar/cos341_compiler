public class BasicNode{

	private Integer line_number;
	private String line_value;
	protected Boolean label = false;
	protected Boolean goto_ = false;
	protected Boolean gosub = false;
	protected Boolean end = false;

	public BasicNode(String line){
		line_value = line;
		line_number = -1;
	}

	public String val(){
		return line_value;
	}

	public Integer line(){
		return line_number;
	}

	public void line(Integer ln){
		line_number = ln;
	}

	public String str(){
		return val() + "\n";
	}

	public String basic(){
		return line() + " " + str();
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