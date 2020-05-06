import java.util.*;

public class DFAstate{
	public HashMap<String, DFAstate> transitions;
	public Boolean isFinal;
	public String name;

	private String errorString = Lexer.UserDefinedLiteral_Error;

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

	public void addTransition(String key, DFAstate nextState){
		if(transitions != null){
			transitions.put(key,nextState);
		}
	}

	public void print(){
		System.out.println("======={ " + ((name == "\n")?"\\n" : name) + " }=======");
		for (String key : this.transitions.keySet()) {
			DFAstate state = this.transitions.get(key);
			System.out.println("{ " + ((key == "\n")?"\\n" : key) + "\t-->\t" + ((state == null)? "null": ((state.name == "\n")?"\\n" : state.name))  + " }");
		}
	}

	public void printTransitionTable(){
		System.out.println("======={ " + ((name == "\n")?"\\n" : name) + " }=======");
		List<DFAstate> list = new ArrayList<>();
		for (String key : this.transitions.keySet()) {
			DFAstate state = this.transitions.get(key);
			System.out.println("{ " + ((key == "\n")?"\\n" : key) + "\t-->\t" + ((state == null)? "null": ((state.name == "\n")?"\\n" : state.name))  + " \t}" + (state.isFinal? ((FinalDFAState)state).token:""));
			if(!list.contains(state)) list.add(state);
		}
		for(DFAstate state : list){
			if(!state.equals(this)) state.printTransitionTable();
		}
	}
	
	public void setErrorString(String err){
		errorString = err;
	}

	public String getErrorString(){
		return errorString;
	}

}