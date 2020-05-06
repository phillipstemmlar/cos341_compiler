import java.util.*;

public class DFA{
	DFAstate startState;

	public DFA(DFAstate startState){
		this.startState = startState;
	}

	public List<Token> evaluate(String input_str){
		List<Token> tokens = new ArrayList<>();
		List<String> strings = new ArrayList<>();

		DFAstate curState = startState;
		DFAstate prevState = null;

		DFAstate savedState = null;
		int savedIndex = -1;

		int i = 0;
		int err = 0;
		int p = 0;
		int startIndex = i;

		String errorString = Lexer.UserDefinedLiteral_Error;

		while(i < input_str.length() && curState != null){
			String c = input_str.substring(i,i+1);
			prevState = curState;
			curState = curState.transitions.get(c);
			if(curState != null){
				errorString = curState.getErrorString();
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
			}
			if(curState == null){
				if(savedState != null){
					Token.eToken token =  ((FinalDFAState)savedState).token;
					String str = input_str.substring(startIndex,i);
					err = p;
					p = i;
					tokens.add(new Token(token,str,Helper.getLineNumber(input_str,i), Helper.getColumnNumber(input_str, i)));
					startIndex = i;
					curState = startState;
					i = savedIndex;
					savedState = null;
					savedIndex = -1;
				}else{
					i++;
					String str = Helper.removeLeadingWhitespace(input_str.substring(err,i));
					Helper.error(Lexer.PREFIX, Helper.getLineNumber(input_str,i), Helper.getColumnNumber(input_str,i), "\"" + Helper.bold + str + Helper.red + "\" " + errorString);
					return null;
				}
			}
			i++;
		}
		if(curState != null && curState.isFinal){
			Token.eToken token =  ((FinalDFAState)savedState).token;
			String str = input_str.substring(startIndex,i);
			tokens.add(new Token(token,str,Helper.getLineNumber(input_str,i), Helper.getColumnNumber(input_str, i)));
		}else{
			i++;
			String str = Helper.removeLeadingWhitespace(input_str.substring(err,i));
			Helper.error(Lexer.PREFIX, Helper.getLineNumber(input_str,i), Helper.getColumnNumber(input_str,i),  "\"" + Helper.bold + str + Helper.red + "\" " + errorString);
			return null;
		}
		List<Token> filter = new ArrayList<>();
		for(int filt = 0; filt < tokens.size(); ++filt){
			Token token = tokens.get(filt);
			if(token.get() != Token.eToken.tok_space && token.get() != Token.eToken.tok_newline && token.get() != Token.eToken.tok_tab){
				filter.add(token);
			}
		}
		return filter;
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
							
							for(char c = 'a'; c <= 'z'; ++c){
								if( ! curState.transitions.containsKey(c+"")) curState.addTransition(c + "", nextState);
							}
							for(int c = 0; c <= 9 && prevState != startState; ++c) curState.addTransition(c + "", nextState);	
						}
						prevState.transitions.put(keyword.toCharArray()[i] + "", curState);
						prevState = curState;
					}else{
						prevState = temp;
					}
				}else{
					if(i == keyword.length()-1){
						curState = new FinalDFAState(keyword.toCharArray()[i] + "" + i, token,isKeyword);
						// prevState.transitions.put(keyword.toCharArray()[i]+"", curState);
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

}