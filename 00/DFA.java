import java.util.*;

public class DFA{

	Boolean logging = true;
	DFAstate startState;

	public DFA(DFAstate startState){
		this.startState = startState;
	}

	public String evaluate(String input_str){

		String output = "";

		Queue<Lexer.Token> tokens = new LinkedList<>();
		Queue<String> strings = new LinkedList<>();

		DFAstate curState = startState;
		DFAstate prevState = null;

		DFAstate savedState = null;
		int savedIndex = -1;

		int i = 0;
		int startIndex = i;

		while(i < input_str.length() && curState != null){
			String c = input_str.substring(i,i+1);

			prevState = curState;
			curState = curState.transitions.get(c);

			// System.out.println("Symbol: " + c);
			// System.out.println("| " + prevState.name + "\t-- " + c + " -->\t" + ((curState==null)?"null":curState.name) + " |");

			if(curState != null && curState.isFinal){
				if(savedState == null){
					savedState = curState;
					savedIndex = i;
				}
				else if(((FinalDFAState)curState).isKeyword){
					savedState = curState;
					savedIndex = i;
				}else if(savedState != null && !((FinalDFAState)savedState).isKeyword && !((FinalDFAState)curState).isKeyword){
					savedState = curState;
					savedIndex = i;
				}
			}else if(curState == null){
				if(savedState != null){
					Lexer.Token token =  ((FinalDFAState)savedState).token;
					// System.out.println("===>| " + savedState.name + " ----> " + token);
					
					tokens.add(((FinalDFAState)savedState).token);

					String str = input_str.substring(startIndex,i);
					// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$:" + str);
					// if(str == null){
					// 	str = input_str.substring(startIndex,i);
					// }
					strings.add( str );
					startIndex = i;

					curState = startState;
					i = savedIndex;
					savedState = null;
					savedIndex = -1;
				}else{
					System.out.println("ERROR");
					return "";
				}
			}

			i++;
		}
	
		if(curState != null && curState.isFinal){
			// System.out.println("===>| " + curState.name + " ----> " + ((FinalDFAState)curState).token );
			tokens.add(((FinalDFAState)curState).token);
			strings.add( input_str.substring(startIndex,i) );
		}else{
			System.out.println("ERROR");
			return "";
		}

		// System.out.println("----TOKENS----");

		while(tokens.size() > 0){
			Lexer.Token token = tokens.poll();
			String str = strings.poll();
			if(token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline)
				// System.out.println(token + "\t\t---->#" + str);
				output += str + " ("  + token + ((tokens.size() > 0)?")\n" : ")"); 
		}

		return output;
	}

	public void addKeywordStates(String keyword, Lexer.Token token){
		final boolean isKeyword = true;
		if(keyword.length() <= 1){
			startState.transitions.put(keyword, new FinalDFAState(keyword,token,isKeyword));
		}else{
			DFAstate curState = null;
			DFAstate prevState = startState;
			for(int i = 0; i < keyword.length(); ++i){

				if(prevState.transitions.containsKey(keyword.toCharArray()[i] + "")){
					prevState = prevState.transitions.get(keyword.toCharArray()[i] + "");
				}else{
					if(i == keyword.length()-1){
						curState = new FinalDFAState(keyword.toCharArray()[i] + "" + i, token,isKeyword);
					}else{
						curState = new NormalState(keyword.toCharArray()[i] + "" + i);
					}
				
					prevState.transitions.put(keyword.toCharArray()[i]+"", curState);
					prevState = curState;
				}
			}
		}

	}

	public void addKeywordStates(String keyword, Lexer.Token token,DFAstate nextState, Lexer.Token intermediateToken){
		final boolean isKeyword = true;
		if(keyword.length() <= 1){
			DFAstate fin = new FinalDFAState(keyword,token,isKeyword);
			
			for(char i = 'a'; i <= 'z' && nextState != null; ++i) fin.addTransition(i+"", nextState);
			for(int i = 1; i <= 9 && nextState != null; ++i) fin.addTransition(i+"", nextState);

			startState.transitions.put(keyword, fin);
		}else{
			DFAstate curState = null;
			DFAstate prevState = startState;
			for(int i = 0; i < keyword.length(); ++i){

				if(prevState.transitions.containsKey(keyword.toCharArray()[i] + "")){

					DFAstate temp = prevState.transitions.get(keyword.toCharArray()[i] + "");

				
					if(temp.isFinal && temp == nextState ){
						
						if(i == keyword.length()-1){
							curState = new FinalDFAState(keyword.toCharArray()[i] + "" + i, token,isKeyword);
							prevState.transitions.put(keyword.toCharArray()[i]+"", curState);
						}else{
							curState = new FinalDFAState(keyword.toCharArray()[i] + "" + i, intermediateToken);				
						}
										
						

						for(char c = 'a'; c <= 'z'; ++c){
							if( ! curState.transitions.containsKey(c+"")) curState.addTransition(c + "", nextState);
						}
						for(int c = 0; c <= 9 && prevState != startState; ++c) curState.addTransition(c + "", nextState);

						prevState.transitions.put(keyword.toCharArray()[i] + "", curState);
						prevState = curState;

					}else{
						prevState = temp;
					}
					
				}else{
					if(i == keyword.length()-1){
						curState = new FinalDFAState(keyword.toCharArray()[i] + "" + i, token,isKeyword);

						// for(char c = 'a'; c <= 'z' && nextState != null; ++c) curState.addTransition(c+"", nextState);
						// for(int c = 1; c <= 9 && nextState != null; ++c) curState.addTransition(c+"", nextState);

						prevState.transitions.put(keyword.toCharArray()[i]+"", curState);

						// for(char c = 'a'; c <= 'z'; ++c){
						// 	if( ! prevState.transitions.containsKey(c+"")) prevState.addTransition(c + "", nextState);
						// }
						// for(int c = 0; c <= 9; ++c) prevState.addTransition(c + "", nextState);

					}else{
						curState = new FinalDFAState(keyword.toCharArray()[i] + "" + i, intermediateToken);

						
					}	
					prevState.transitions.put(keyword.toCharArray()[i]+"", curState);

					for(char c = 'a'; c <= 'z'; ++c){
						if( ! prevState.transitions.containsKey(c+"")) prevState.addTransition(c + "", nextState);
					}
					for(int c = 0; c <= 9 && prevState != startState; ++c) prevState.addTransition(c + "", nextState);		

					prevState = curState;
				}
			}
		}

	}



	public void print(){
		startState.printTransitionTable();
	}

	private void log(String out){
		if(logging){
			System.out.println("DFA:\t" + out);
		}
	}

}