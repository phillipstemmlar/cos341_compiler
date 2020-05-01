import java.util.*;

public class DFA{

	Boolean logging = false;
	DFAstate startState;

	public DFA(DFAstate startState){
		this.startState = startState;
	}

	public Lexer.Token[] evaluate(String input_str){
		Queue<Lexer.Token> tokens = new LinkedList<>();
		Queue<String> strings = new LinkedList<>();

		DFAstate curState = startState;
		DFAstate prevState = null;

		DFAstate savedState = null;
		int savedIndex = -1;

		int i = 0;
		int err = 0;
		int p = 0;
		int startIndex = i;

		String errorString = Lexer.UserDefinedLiteral_Error;
		String errorBuffer = "";

		// System.out.println(blue+"----TOKENS----"+white);

		while(i < input_str.length() && curState != null){
			String c = input_str.substring(i,i+1);

			prevState = curState;
			curState = curState.transitions.get(c);

			// System.out.println("Symbol: " + c);
			// System.out.println("| " + prevState.name + "\t-- " + c + " -->\t" + ((curState==null)?"null":curState.name) + " |");

			if(curState != null){
				errorString = curState.getErrorString();
				// System.out.println("ERR: " + errorString);
			}

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
					String str = input_str.substring(startIndex,i);
					
					if(true || token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline && token != Lexer.Token.tok_tab){
						// log(green +token + white +"\n\t" + str);
					}

					err = p;
					p = i;
					// System.out.println("ERR: " + err);

					tokens.add(token);
					strings.add( str );
					startIndex = i;

					curState = startState;
					i = savedIndex;
					savedState = null;
					savedIndex = -1;
					
					// err = i-1;
				}else{
					i++;
					String buff = input_str.substring(err,i);
					String str = removeLeadingWhitespace(buff);

					int lin_num = getLineNumber(input_str,i);
					int col_num = getColumnNumber(input_str,i);

					// System.out.println("\n" + blue + "----ERROR-OUT----" + white);

					// System.out.println("\nstart index: \t" + startIndex);
					// System.out.println("saved index: \t" + startIndex);
					// System.out.println("i: \t\t" + i);
					// System.out.println("err: \t\t" + err);
					// System.out.println("str: \t\t" + str);
					// System.out.println("error str: \t" + errorString);
					// System.out.println("error buff: \t" + errorBuffer);

					// System.out.println("Line: \t\t" + lin_num);
					// System.out.println("Column: \t" + col_num);

					System.out.println("\n" + red + "Lexical Error [line: "+lin_num+", col: "+col_num+"]: '"
						 + bold + str + red + "' " + errorString + white );

					// System.out.println("\n" + blue + "----TOKENS-END----" + white);
					return null;
				}
			}

			i++;
		}
	
		if(curState != null && curState.isFinal){
			Lexer.Token token =  ((FinalDFAState)savedState).token;
			String str = input_str.substring(startIndex,i);

			if(token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline && token != Lexer.Token.tok_tab){
				// log(green +token + white +"\n\t" + str);
			}

			tokens.add(((FinalDFAState)curState).token);
			strings.add( input_str.substring(startIndex,i) );
		}else{
			i++;
			String buff = input_str.substring(err,i);
			String str = removeLeadingWhitespace(buff);

			int lin_num = getLineNumber(input_str,i);
			int col_num = getColumnNumber(input_str,i);

			System.out.println("\n" + red + "Lexical Error [line: "+lin_num+", col: "+col_num+"]: '"
					+ bold + str + red + "' " + errorString + white );
			return null;
		}

		Lexer.Token[] output = new Lexer.Token[tokens.size()];
		int oi = 0;

		while(tokens.size() > 0){
			Lexer.Token token = tokens.poll();
			String str = strings.poll();
			if(token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline && token != Lexer.Token.tok_tab){
				// System.out.println(token + "\t\t---->#" + str);
				// output += str + " ("  + token + ((tokens.size() > 0)?")\n" : ")"); 
				output[oi] = token;
				oi++;
			}
		}

		// System.out.println(blue + "----TOKENS-END----" + white);

		return output;
	}

	public String evaluateToFile(String input_str){

		String output = "";

		Queue<Lexer.Token> tokens = new LinkedList<>();
		Queue<String> strings = new LinkedList<>();

		DFAstate curState = startState;
		DFAstate prevState = null;

		DFAstate savedState = null;
		int savedIndex = -1;

		int i = 0;
		int err = 0;
		int p = 0;
		int startIndex = i;

		String errorString = Lexer.UserDefinedLiteral_Error;
		String errorBuffer = "";

		// System.out.println(blue+"----TOKENS----"+white);

		while(i < input_str.length() && curState != null){
			String c = input_str.substring(i,i+1);

			prevState = curState;
			curState = curState.transitions.get(c);

			// System.out.println("Symbol: " + c);
			// System.out.println("| " + prevState.name + "\t-- " + c + " -->\t" + ((curState==null)?"null":curState.name) + " |");

			if(curState != null){
				errorString = curState.getErrorString();
				// System.out.println("ERR: " + errorString);
			}

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
					String str = input_str.substring(startIndex,i);
					
					if(true || token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline && token != Lexer.Token.tok_tab){
						// log(green +token + white +"\n\t" + str);
					}

					err = p;
					p = i;
					// System.out.println("ERR: " + err);

					tokens.add(token);
					strings.add( str );
					startIndex = i;

					curState = startState;
					i = savedIndex;
					savedState = null;
					savedIndex = -1;
					
					// err = i-1;
				}else{
					i++;
					String buff = input_str.substring(err,i);
					String str = removeLeadingWhitespace(buff);

					int lin_num = getLineNumber(input_str,i);
					int col_num = getColumnNumber(input_str,i);

					// System.out.println("\n" + blue + "----ERROR-OUT----" + white);

					// System.out.println("\nstart index: \t" + startIndex);
					// System.out.println("saved index: \t" + startIndex);
					// System.out.println("i: \t\t" + i);
					// System.out.println("err: \t\t" + err);
					// System.out.println("str: \t\t" + str);
					// System.out.println("error str: \t" + errorString);
					// System.out.println("error buff: \t" + errorBuffer);

					// System.out.println("Line: \t\t" + lin_num);
					// System.out.println("Column: \t" + col_num);

					System.out.println("\n" + red + "Lexical Error [line: "+lin_num+", col: "+col_num+"]: '"
						 + bold + str + red + "' " + errorString + white );

					// System.out.println("\n" + blue + "----TOKENS-END----" + white);
					return "";
				}
			}

			i++;
		}
	
		if(curState != null && curState.isFinal){
			Lexer.Token token =  ((FinalDFAState)savedState).token;
			String str = input_str.substring(startIndex,i);

			if(token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline && token != Lexer.Token.tok_tab){
				// log(green +token + white +"\n\t" + str);
			}

			tokens.add(((FinalDFAState)curState).token);
			strings.add( input_str.substring(startIndex,i) );
		}else{
			i++;
			String buff = input_str.substring(err,i);
			String str = removeLeadingWhitespace(buff);

			int lin_num = getLineNumber(input_str,i);
			int col_num = getColumnNumber(input_str,i);

			System.out.println("\n" + red + "Lexical Error [line: "+lin_num+", col: "+col_num+"]: '"
					+ bold + str + red + "' " + errorString + white );
			return "";
		}

		while(tokens.size() > 0){
			Lexer.Token token = tokens.poll();
			String str = strings.poll();
			if(token != Lexer.Token.tok_space && token != Lexer.Token.tok_newline && token != Lexer.Token.tok_tab)
				// System.out.println(token + "\t\t---->#" + str);
				output += str + " ("  + token + ((tokens.size() > 0)?")\n" : ")"); 
		}

		// System.out.println(blue + "----TOKENS-END----" + white);

		return output;
	}

	public void addKeywordStates(String keyword, Lexer.Token token){
		final boolean isKeyword = true;
		if(keyword.length() <= 1){
			DFAstate state = new FinalDFAState(keyword,token,isKeyword);
			state.setErrorString(Lexer.UserDefinedLiteral_Error);
			
			startState.transitions.put(keyword, state);
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
						curState = new NormalDFAState(keyword.toCharArray()[i] + "" + i);
					}
					curState.setErrorString(Lexer.UserDefinedLiteral_Error);
				
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

	private int getLineNumber(String input, int charIndex){
		int line = 1;
		for(int i = 0; i < charIndex && i < input.length(); ++i){
			if(input.charAt(i) == '\n') line++;
		}
		return line;
	}

	private int getColumnNumber(String input, int charIndex){
		int col = 0;
		for(int i = 0; i < charIndex && i < input.length(); ++i){
			if(input.charAt(i) == '\n') col = 0;
			else col++;
		}
		return col;
	}

	private String removeLeadingWhitespace(String str){
		while(str.charAt(0) == ' ') str = str.substring(1);
		return str;
	}

	public void print(){
		startState.printTransitionTable();
	}

	private void log(String out){
		if(logging){
			System.out.println("DFA:\t" + out);
		}
	}

	public static final String bold = "\033[0;1m";
	public static final String red = "\033[0;31m";
	public static final String green = "\033[0;32m";
	public static final String blue = "\033[0;34m";
	public static final String white = "\033[0;37m";
	public static final String grey = "\033[1;30m";

}