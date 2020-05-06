import java.io.*;
import java.util.*; 

public class Lexer{

	public static String PREFIX;
	DFA automata = null;
	public List<Token> tokensQ = null;

	public Lexer(String prefix){
		PREFIX = prefix;
		DFAstate q0 = new FinalDFAState("start", Token.eToken.tok_none);
		q0.setErrorString(defaultError);

		//User defined identifiers

		DFAstate q1 = new FinalDFAState("udi0", Token.eToken.tok_user_defined_identifier);
		DFAstate q6 = new FinalDFAState("udi1", Token.eToken.tok_user_defined_identifier);

		for(char i = 'a'; i <= 'z'; ++i) q6.addTransition(i + "", q6);
		for(int i = 0; i <= 9; ++i) q6.addTransition(i + "", q6);


		for(char i = 'a'; i <= 'z'; ++i) q1.addTransition(i + "", q6);
		for(int i = 0; i <= 9; ++i) q1.addTransition(i + "", q6);


		q1.setErrorString(UserDefinedLiteral_Error);
		q6.setErrorString(UserDefinedLiteral_Error);

		//Integer literals

		DFAstate q2 = new FinalDFAState("int0", Token.eToken.tok_integer_literal);
		DFAstate q3 = new FinalDFAState("int1", Token.eToken.tok_integer_literal);
		DFAstate q4 = new NormalDFAState("int2");
		DFAstate q5 = new FinalDFAState("int3", Token.eToken.tok_integer_literal);
		DFAstate qZero = new FinalDFAState("intZero", Token.eToken.tok_integer_literal);

		for(int i = 0; i <= 9; ++i) q5.addTransition(i+"",q5);
		for(int i = 0; i <= 9; ++i) q3.addTransition(i+"",q5);

		for(int i = 1; i <= 9; ++i) q4.addTransition(i+"",q3);
		for(int i = 1; i <= 9; ++i) q0.addTransition(i+"",q3);

		q0.addTransition("-",q4);
		q0.addTransition("0",qZero);

		q2.setErrorString(IntegerLiteral_Error);
		q3.setErrorString(IntegerLiteral_Error);
		q4.setErrorString(IntegerLiteral_Error);
		q5.setErrorString(IntegerLiteral_Error);
		qZero.setErrorString(IntegerLiteral_Error);

		//String Literals

		DFAstate q9 = new FinalDFAState("q9", Token.eToken.tok_string_literal);
		q9.setErrorString(StringLiteral_Error);

		DFAstate qS = null;
		DFAstate qP = new NormalDFAState("q16");
		qP.setErrorString(StringLiteralLength_Error);

		qP.transitions.put("\"", q9);

		int min = 7;	int max = 15;
		for(int n = max; n > min; --n){
			qS = new NormalDFAState("q" + n);
			qS.setErrorString(StringLiteral_Error);

			for(char i = 'a'; i <= 'z'; ++i) qS.transitions.put(i + "", qP);
			for(int i = 0; i <= 9 ; ++i) qS.transitions.put(i + "", qP);
			qS.transitions.put(" ", qP);

			qS.transitions.put("\"", q9);
			qP = qS;
		}
		q0.transitions.put("\"",qS);

		//Create DFA
		automata = new DFA(q0);

		//Add keywords
		for(int i = 0 ;i < keywords.length; ++i){
			if(keywords[i] == "T" || keywords[i] == "F") automata.addKeywordStates(keywords[i], keywordTokens[i]);
			else automata.addKeywordStates(keywords[i], keywordTokens[i], q6, Token.eToken.tok_user_defined_identifier);
		}

		// //Add operators
		for(int i = 0 ;i < operators.length; ++i){
			automata.addKeywordStates(operators[i], operatorTokens[i]);
		}

		for(char i = 'a'; i <= 'z'; ++i){
			if(!q0.transitions.containsKey(i+"")){
				q0.addTransition(i + "", q1);
			}
		}
	}

	public List<Token> execute(String inputfile){	
		if(automata == null) return null;
		tokensQ = automata.evaluate(Helper.filetoString(inputfile));
		return tokensQ;
	}


	public List<Token> executeToFile(String inputfile, String outputfile){ 	
		if(automata == null) return null;
		tokensQ = execute(inputfile);
		if(tokensQ != null){
			String output = stringifyTokens();
			Helper.writeToFile(outputfile, output);
			return tokensQ;
		}
		return null;
	}

	private String stringifyTokens(){
		if(tokensQ != null){
			String output = "";
			for(int i = 0; i < tokensQ.size(); ++i){
				Token tok = tokensQ.get(i);
				output += tok.str() + " (" + tok.get() + ")\n";
			}
			return output;
		}
		return "";
	}

	public static String __quote__ = '"' + ""; 
	public static String __space__ = " ";
	public static String __newln__ = "\n";
	public static String __tab__ = "\t";

	public static String[] keywords = {
		"and", "or", "not", "add", "sub", "mult", "if",
		"then", "else","while", "for", "eq", "input", "output",
		"halt", "num", "bool","string", "proc", "T", "F"
	};

	public static Token.eToken[] keywordTokens = {
		Token.eToken.tok_and,
		Token.eToken.tok_or,
		Token.eToken.tok_not,
		Token.eToken.tok_add,
		Token.eToken.tok_sub,
		Token.eToken.tok_mult,
		Token.eToken.tok_if,
		Token.eToken.tok_then,
		Token.eToken.tok_else,
		Token.eToken.tok_while,
		Token.eToken.tok_for,
		Token.eToken.tok_eq,
		Token.eToken.tok_input,
		Token.eToken.tok_output,
		Token.eToken.tok_halt,
		Token.eToken.tok_num,
		Token.eToken.tok_bool,
		Token.eToken.tok_string,
		Token.eToken.tok_proc,
		Token.eToken.tok_T,
		Token.eToken.tok_F
	};

	public static String[] operators = {
		"<", ">", " ", "\n", "(", ")", "{", "}", "=", ",", ";", "\t"
	};
 
	public static Token.eToken[] operatorTokens = {
		Token.eToken.tok_less_than,				// <
		Token.eToken.tok_greater_than,		// >
		Token.eToken.tok_space,						// " "
		Token.eToken.tok_newline,					// \n
		Token.eToken.tok_open_parenth,		// (
		Token.eToken.tok_close_parenth,		// )
		Token.eToken.tok_open_brace,			// {
		Token.eToken.tok_close_brace,			// }
		Token.eToken.tok_assignment,			// =
		Token.eToken.tok_comma,						// ,
		Token.eToken.tok_semi_colon,			// ;
		Token.eToken.tok_tab							// \t
	};

	public static final String InvalidCharacter_Error = "is not a valid character";
	public static final String UserDefinedLiteral_Error = "user defined literal contains illegal characters";
	public static final String IntegerLiteral_Error = "integer contains illegal characters";
	public static final String StringLiteral_Error = "string contains illegal characters";
	public static final String StringLiteralLength_Error = "strings have at most 8 characters";
	public static final String defaultError = "token contains illegal characters";
}