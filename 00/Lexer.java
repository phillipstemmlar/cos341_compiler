public class Lexer{

	Boolean logging = true;

	DFA automata;

	public Lexer(){

	}

	public void execute(String filename){	

	}


	private void log(String out){
		if(logging){
			System.out.println("LEXER:\t" + out);
		}
	}

	public String[] keywords = {
		"and", "or", "not", "add", "sub", "mult", "if",
		"then", "else","while", "for", "eq", "input", "output",
		"halt", "num", "bool","string", "proc", "T", "F"
	};

	public String[] operators = {
		"<", ">", " ", "\n", "(", ")", "{", "}", "=", ",", ";"	
	};

}

/*
DFAstate q0 = new NormalDFAState("q0");
		
		//User-Defined Identifiers
		DFAstate q6 = new FinalDFAState("q6", "Token_UserDefinedIdentifier");
		for(char i = 'a'; i <= 'z'; ++i)q6.transitions.put(i + "", q6);
		for(int i = 0; i <= 9 ; ++i) q6.transitions.put(i + "", q6);

		DFAstate q1 = new FinalDFAState("q1", "Token_UserDefinedIdentifier");
		for(char i = 'a'; i <= 'z'; ++i)q1.transitions.put(i + "", q6);
		for(int i = 0; i <= 9 ; ++i) q1.transitions.put(i + "", q6);

		for(char i = 'a'; i <= 'z'; ++i) q0.transitions.put(i + "", q1);
	
		//Integer Literals
		DFAstate q2 = new FinalDFAState("q2", "Token_IntegerLiteral");

		DFAstate q5 = new FinalDFAState("q5", "Token_IntegerLiteral");
		for(int i = 0; i <= 9 ; ++i) q5.transitions.put(i + "", q5);

		DFAstate q3 = new FinalDFAState("q3", "Token_IntegerLiteral");
		for(int i = 0; i <= 9 ; ++i) q3.transitions.put(i + "", q5);

		DFAstate q4 = new NormalDFAState("q4");
		for(int i = 1; i <= 9 ; ++i) q4.transitions.put(i + "", q3);

		q0.transitions.put("0",q2);
		for(int i = 1; i <= 9 ; ++i) q0.transitions.put(i + "", q3);
		q0.transitions.put("-",q4);

		//String Literals
		DFAstate q9 = new FinalDFAState("q9", "Token_StringLiteral");

		DFAstate qS = null;
		DFAstate qP = new NormalDFAState("q16");
		qP.transitions.put(Q, q9);

		for(int n = 15; n > 7; --n){
			qS = new NormalDFAState("q" + n);
			
			for(char i = 'a'; i <= 'z'; ++i) qS.transitions.put(i + "", qP);
			for(int i = 0; i <= 9 ; ++i) qS.transitions.put(i + "", qP);
			qS.transitions.put(" ", qP);

			qS.transitions.put(Q, q9);
			qP = qS;
		}

		q0.transitions.put(Q,qS);


		//DFA
		DFA dfa = new DFA(q0);

		String[] input = {"h3", "xyz", "a3b","3a", "0", "-42", "901", "014", "--9", Q+"2helllo "+Q, Q+Q, Q+"  "+Q, Q+"123456789"+Q, Q+"al"};

		for(int i = 0; i < input.length; ++i){
			System.out.println("==============================");
			if(dfa.testString(input[i])){
				System.out.println("SUCCESS!");
			}else{
				System.out.println("FAILED!");
			}
		}
		System.out.println("==============================");
*/