import java.util.*;

public class DFA{

	Boolean logging = true;
	DFAstate startState;

	public DFA(DFAstate startState){
		this.startState = startState;
	}

	public Boolean testString(String input){
		if(startState == null){
			log("EMPTY---{"+input+"}");
			return false;
		}

		DFAstate curState = startState;

		for(int i = 0; i < input.length() && curState != null; ++i){
			// curState.print();
			String key = input.substring(i,i+1);
			// log("Key: " + key);
			curState = curState.transitions.get(key);
		}
		
		if(curState == null){
			log("ERROR---{"+input+"}");
			return false;
		}else if( curState.isFinal){
			log("SUCCESS---{"+input+"}");
			return true;
		}else{
			log("-FAILED---{"+input+"}");
			return false;
		}
		


	}

	private void log(String out){
		if(logging){
			System.out.println("DFA:\t" + out);
		}
	}

}