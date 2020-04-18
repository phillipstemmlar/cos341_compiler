import java.io.*;
import java.util.*; 

public class Lexer{

	Boolean logging = true;

	DFA automata;

	public Lexer(){

		DFAstate q0 = new FinalDFAState("start", Token.tok_none);

		//User defined identifiers

		DFAstate q1 = new FinalDFAState("udi0", Token.tok_user_defined_identifier);
		DFAstate q6 = new FinalDFAState("udi1", Token.tok_user_defined_identifier);

		for(char i = 'a'; i <= 'z'; ++i) q6.addTransition(i + "", q6);
		for(int i = 0; i <= 9; ++i) q6.addTransition(i + "", q6);


		for(char i = 'a'; i <= 'z'; ++i) q1.addTransition(i + "", q6);
		for(int i = 0; i <= 9; ++i) q1.addTransition(i + "", q6);

		//Integer literals

		DFAstate q2 = new FinalDFAState("int0", Token.tok_integer_literal);
		DFAstate q3 = new FinalDFAState("int1", Token.tok_integer_literal);
		DFAstate q4 = new NormalState("int2");
		DFAstate q5 = new FinalDFAState("int3", Token.tok_integer_literal);

		for(int i = 0; i <= 9; ++i) q5.addTransition(i+"",q5);
		for(int i = 0; i <= 9; ++i) q3.addTransition(i+"",q5);

		for(int i = 1; i <= 9; ++i) q4.addTransition(i+"",q3);
		for(int i = 1; i <= 9; ++i) q0.addTransition(i+"",q3);

		q0.addTransition("-",q4);

		//String Literals

		DFAstate q9 = new FinalDFAState("q9", Token.tok_string_literal);

		DFAstate qS = null;
		DFAstate qP = new NormalDFAState("q16");
		qP.transitions.put("\"", q9);

		for(int n = 15; n > 7; --n){
			qS = new NormalDFAState("q" + n);
			
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
			else automata.addKeywordStates(keywords[i], keywordTokens[i], q6, Token.tok_user_defined_identifier);
		}

		// //Add operators
		for(int i = 0 ;i < operators.length; ++i){
			automata.addKeywordStates(operators[i], operatorTokens[i]);
		}

		
		automata.addKeywordStates("add", Token.tok_add, q6, Token.tok_user_defined_identifier);
		automata.addKeywordStates("and", Token.tok_and, q6, Token.tok_user_defined_identifier);

		automata.addKeywordStates(" ", Token.tok_space);

		for(char i = 'a'; i <= 'z'; ++i){
			if(!q0.transitions.containsKey(i+"")){
				q0.addTransition(i + "", q1);
			}
		}


		// automata.print();
	}

	public void execute(String inputfile, String outputfile){	
		
		String input = filetoString(inputfile); 
		// String input = "   (F ) add \"aaa bbb\" or  123 not -452 and myvar1234 submultif then else while < for > eq \n input {output} halt bool = num , string proc T ;";
		// String input = "   (F ) add \"aaa bbb\" or  123 not -452 and submultif then else while < for > eq \n input {output} halt bool = num , string proc T ;";
		// String input = "   (F ) add or myvar1234  not and submultif then else while < for > eq \n input {output} halt bool = num , string proc T ;";
		// String input = " afna and add adnfagfgadf ";

		line();
		System.out.println(inputfile + ":\n" + input);

		String output = automata.evaluate(input);
		line();

		System.out.println(outputfile + ":\n" + output);
		writeToFile(outputfile, output);

		line();
	}

	private String filetoString(String filename){
		String file = "";
		try {
			File srcFile = new File(filename);
			Scanner srcReader = new Scanner(srcFile);

			while(srcReader.hasNextLine()){
				String line = srcReader.nextLine();
				file += line;
				if(srcReader.hasNextLine()) file += "\n";
			}
			srcReader.close();
			
    } catch (FileNotFoundException e) {
      System.out.println("File could not be read: " + filename);
      e.printStackTrace();
		}
		return file;		
	}

	private void writeToFile(String filename, String content){	
		try {
			FileWriter myWriter = new FileWriter(filename);		
      myWriter.write(content);
			myWriter.close();	
    } catch (IOException e) {
      System.out.println("File could not be written to: " + filename);
			e.printStackTrace();		
    }
		
		
		// try {
		// 	File outfile = new File(filename);
			
    //   if (outfile.createNewFile()) {
    //     System.out.println("File created: " + outfile.getName());
    //   } else {
    //     System.out.println("File already exists.");
		// 	}
			
    // } catch (IOException e) {
    //   System.out.println("An error occurred.");
    //   e.printStackTrace();
		// }
		

	}

	private void log(String out){
		if(logging){
			System.out.println("LEXER:\t" + out);
		}
	}

	private void line(char c){
		if(logging){
			String ln = "";
			for(int i = 0; i < 30; ++i) ln += c;
			System.out.println(ln);
		}
	}

	private void line(){
		line('=');
	}

	public static String __quote__ = '"' + ""; 
	public static String __space__ = " ";
	public static String __newln__ = "\n";

	public static enum Token{
		tok_and,
		tok_or,
		tok_not,
		tok_add,
		tok_sub,
		tok_mult,
		tok_if,
		tok_then,
		tok_else,
		tok_while,
		tok_for,
		tok_eq,
		tok_input,
		tok_output,
		tok_halt,
		tok_num,
		tok_bool,
		tok_string,
		tok_proc,
		tok_T,
		tok_F,
		tok_user_defined_identifier,
		tok_string_literal,
		tok_integer_literal,
		tok_less_than,
		tok_greater_than,
		tok_space,
		tok_newline,
		tok_open_parenth,
		tok_close_parenth,
		tok_open_brace,
		tok_close_brace,
		tok_assignment,
		tok_comma,
		tok_semi_colon,
		tok_none
	}

	public static String[] keywords = {
		"and", "or", "not", "add", "sub", "mult", "if",
		"then", "else","while", "for", "eq", "input", "output",
		"halt", "num", "bool","string", "proc", "T", "F"
	};

	public static Token[] keywordTokens = {
		Token.tok_and,
		Token.tok_or,
		Token.tok_not,
		Token.tok_add,
		Token.tok_sub,
		Token.tok_mult,
		Token.tok_if,
		Token.tok_then,
		Token.tok_else,
		Token.tok_while,
		Token.tok_for,
		Token.tok_eq,
		Token.tok_input,
		Token.tok_output,
		Token.tok_halt,
		Token.tok_num,
		Token.tok_bool,
		Token.tok_string,
		Token.tok_proc,
		Token.tok_T,
		Token.tok_F
	};

	public static String[] operators = {
		"<", ">", " ", "\n", "(", ")", "{", "}", "=", ",", ";"	
	};
 
	public static Token[] operatorTokens = {
		Token.tok_less_than,			// <
		Token.tok_greater_than,		// >
		Token.tok_space,					// " "
		Token.tok_newline,				// \n
		Token.tok_open_parenth,		// (
		Token.tok_close_parenth,	// )
		Token.tok_open_brace,			// {
		Token.tok_close_brace,		// }
		Token.tok_assignment,			// =
		Token.tok_comma,					// ,
		Token.tok_semi_colon			// ;
	};

}

/*
		//User-Defined Identifiers
		DFAstate q6 = new FinalDFAState("q6", Token.tok_user_defined_identifier);
		for(char i = 'a'; i <= 'z'; ++i)q6.transitions.put(i + "", q6);
		for(int i = 0; i <= 9 ; ++i) q6.transitions.put(i + "", q6);

		DFAstate q1 = new FinalDFAState("q1", Token.tok_user_defined_identifier);
		for(char i = 'a'; i <= 'z'; ++i)q1.transitions.put(i + "", q6);
		for(int i = 0; i <= 9 ; ++i) q1.transitions.put(i + "", q6);

		for(char i = 'a'; i <= 'z'; ++i) q0.transitions.put(i + "", q1);

		//Integer Literals
		DFAstate q2 = new FinalDFAState("q2", Token.tok_integer_literal);

		DFAstate q5 = new FinalDFAState("q5", Token.tok_integer_literal);
		for(int i = 0; i <= 9 ; ++i) q5.transitions.put(i + "", q5);

		DFAstate q3 = new FinalDFAState("q3", Token.tok_integer_literal);
		for(int i = 0; i <= 9 ; ++i) q3.transitions.put(i + "", q5);

		DFAstate q4 = new NormalDFAState("q4");
		for(int i = 1; i <= 9 ; ++i) q4.transitions.put(i + "", q3);

		q0.transitions.put("0",q2);
		for(int i = 1; i <= 9 ; ++i) q0.transitions.put(i + "", q3);
		q0.transitions.put("-",q4);

		//String Literals
		DFAstate q9 = new FinalDFAState("q9", Token.tok_string_literal);

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
*/