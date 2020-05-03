import java.util.*;

public class DFA{

	Boolean logging = false;
	DFAstate startState;

	public DFA(DFAstate startState){
		this.startState = startState;
	}

	public Queue<Token> evaluate(String input_str){
		Queue<Token> tokens = new LinkedList<>();
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
					Token.eToken token =  ((FinalDFAState)savedState).token;
					String str = input_str.substring(startIndex,i);
					
					if(true || token != Token.eToken.tok_space && token != Token.eToken.tok_newline && token != Token.eToken.tok_tab){
						// log(green +token + white +"\n\t" + str);
					}

					err = p;
					p = i;
					// System.out.println("ERR: " + err);

					// System.out.println("=====================");
					// System.out.println(token);
					// System.out.println(str);
					// System.out.println(getLineNumber(input_str,i));
					// System.out.println(getColumnNumber(input_str, i));
					// System.out.println("=====================");

					tokens.add(new Token(token,str,getLineNumber(input_str,i), getColumnNumber(input_str, i)));
					// tokens.add(token);
					// strings.add( str );
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
			Token.eToken token =  ((FinalDFAState)savedState).token;
			String str = input_str.substring(startIndex,i);

			if(token != Token.eToken.tok_space && token != Token.eToken.tok_newline && token != Token.eToken.tok_tab){
				// log(green +token + white +"\n\t" + str);
			}

			// System.out.println("=====================");
			// System.out.println(token);
			// System.out.println(str);
			// System.out.println(getLineNumber(input_str,i));
			// System.out.println(getColumnNumber(input_str, i));
			// System.out.println("=====================");

			tokens.add(new Token(token,str,getLineNumber(input_str,i), getColumnNumber(input_str, i)));
			// tokens.add(token);
			// strings.add(str);
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

		Queue<Token> filter = new LinkedList<>();

		while(tokens.size() > 0){
			Token token = tokens.poll();
			if(token.get() != Token.eToken.tok_space && token.get() != Token.eToken.tok_newline && token.get() != Token.eToken.tok_tab){
				filter.add(token);
			}
		}

		return filter;

		// Token[] output = new Token[filter.size()];
		// int oi = 0;

		// while(filter.size() > 0){
		// 	Token token = filter.poll();
		// 	if(token.get() != Token.eToken.tok_space && token.get() != Token.eToken.tok_newline && token.get() != Token.eToken.tok_tab){
		// 		output[oi] = token;
		// 		oi++;
		// 	}
		// }

		// // System.out.println(blue + "----TOKENS-END----" + white);

		// return output;
	}

	public String evaluateToFile(String input_str){
		Queue<Token> tokensQ = evaluate(input_str);
		Token[] tokens = new Token[tokensQ.size()];
		int inp = 0;
		while(tokensQ.size() > 0){
			tokens[inp] = tokensQ.poll();
			inp++;
		}

		String output = "";

		for(int i = 0; i < tokens.length; ++i){
			output += tokens[i].str() + " (" + tokens[i].get() + ")" + ((i < tokens.length-1)?"\n": "");
		}

		return output;
	}

	public void addKeywordStates(String keyword, Token.eToken token){
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

	public void addKeywordStates(String keyword, Token.eToken token,DFAstate nextState, Token.eToken intermediateToken){
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