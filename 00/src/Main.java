import java.util.*;

public class Main{

	public static void main(String[] args) {
		String filename = args[0];
		
		String[] grammar = {"0", "1"};

		DFAstate q0 = new DFAstate(false, "q0");
		DFAstate q1 = new DFAstate(false, "q1");
		DFAstate q2 = new DFAstate(false, "q2");
		DFAstate q3 = new DFAstate(true, "q3");

		q0.transitions.put(grammar[0], q1);
		q0.transitions.put(grammar[1], q0);

		q1.transitions.put(grammar[0], q2);

		q2.transitions.put(grammar[0], q3);

		q3.transitions.put(grammar[0], q1);
		q3.transitions.put(grammar[1], q3);

		DFA dfa = new DFA(q0);

		String[] input = {"000", "0001", "1000","10001", "111100"};

		for(int i = 0; i < input.length; ++i){
			System.out.println("==============================");
			System.out.println("Testing:\t" + input[i] + "\t" );
			if(dfa.testString(input[i])){
				System.out.println("SUCCESS!");
			}else{
				System.out.println("FAILED!");
			}
		}
	}

}