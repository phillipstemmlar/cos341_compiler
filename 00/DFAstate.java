import java.util.*;

public class DFAstate{
	public HashMap<String, DFAstate> transitions;
	public Boolean isFinal;
	public String name;

	public DFAstate(Boolean fin, String name){
		transitions = new HashMap<>();
		isFinal = fin;
		this.name = name;
	}

	public DFAstate nextState(String input){
		if(transitions.containsKey(input)){
			return transitions.get(input);
		}
		return null;
	}

	public void print(){
		System.out.println("======={ " + name + " }=======");
		for (String key : this.transitions.keySet()) {
			DFAstate state = this.transitions.get(key);
			System.out.println("{ " + key + "\t-->\t" + ((state == null)? "null": state.name)  + " }");
		}
	}
}