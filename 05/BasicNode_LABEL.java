public class BasicNode_LABEL extends BasicNode{

	private static int COUNT = -1;

	public BasicNode_LABEL(String line){
		super(line);
		label = true;
	}
	
	public static int countINC(){
		return ++COUNT;
	}

	public String str(){
		return val() + ":\n";
	}

	public String basic(){
		if(end && label){
			return line() + "";
		}
		return line() + " " + str();
	}
}