import java.io.*;
import java.util.*; 

public class Helper{

	public static Boolean logging = false;
	public static Boolean erroring = true;
	public static Boolean successing = true;
	public static Boolean warning = true;

	public static Variable.Type tokenToType(Token.eToken token){
		switch(token){
			case tok_string: return Variable.Type.string;
			case tok_string_literal: return Variable.Type.string;
			case tok_num: 	 return Variable.Type.num;
			case tok_integer_literal: 	 return Variable.Type.num;
			case tok_bool:	 return Variable.Type.bool;
			case tok_T:	 return Variable.Type.bool;
			case tok_F:	 return Variable.Type.bool;
			case PROC:	 return Variable.Type.procedure;
			default: return Variable.Type.undefined;
		}
	}

	public static Token.eToken typeToToken(Variable.Type type){
		switch(type){
			case string: return Token.eToken.tok_string;
			case num: 	 return Token.eToken.tok_num;
			case bool:	 return Token.eToken.tok_bool;
			case procedure:	 return Token.eToken.PROC;
			default: return Token.eToken.tok_none;
		}
	}

	public static String typeStr(Variable.Type type){
		switch(type){
			case string: return "S";
			case num: 	 return "N";
			case bool:	 return "B";
			case procedure:	 return "P";
			default: return type + "";
		}
	}

	public static String tokenStr(Token.eToken token){
		switch(token){
			case tok_input: return "input";
			case tok_output: 	 return "output";
			case tok_add: 	 return "add";
			case tok_sub: 	 return "sub";
			case tok_mult: 	 return "mult";
			case tok_or: 	 return "or";
			case tok_and: 	return "and";
			case tok_not: 	 return "not";
			case tok_eq: 	 return "eq";
			case tok_greater_than: 	 return ">";
			case tok_less_than: 	 return "<";
			case tok_if: 	 return "if";
			case tok_while: 	 return "while";
			default: return token + "";
		}
	}

	public static  String YesNo(Boolean bool){
		return bool? "Yes" : "No";
	}

	public static String yesno(Boolean bool){
		return bool? "yes" : "no";
	}

	public static String filetoString(String filename){
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

	public static void writeToFile(String filename, String content){	
		if(content.length() == 0) return;
		try {
			FileWriter myWriter = new FileWriter(filename);		
      myWriter.write(content);
			myWriter.close();	
    } catch (IOException e) {
      System.out.println("File could not be written to: " + filename);
			e.printStackTrace();		
    }
	}

	public static String removeLeadingWhitespace(String str){
		while(str.charAt(0) == ' ') str = str.substring(1);
		return str;
	}

	public static int getLineNumber(String input, int charIndex){
		int line = 1;
		for(int i = 0; i < charIndex && i < input.length(); ++i){
			if(input.charAt(i) == '\n') line++;
		}
		return line;
	}

	public static int getColumnNumber(String input, int charIndex){
		int col = 0;
		for(int i = 0; i < charIndex && i < input.length(); ++i){
			if(input.charAt(i) == '\n') col = 0;
			else col++;
		}
		return col + 1;
	}

	private static String boldQuotes(String msg, String col){
		Boolean first = true;
		String newMsg = "";
		char[] msgc = msg.toCharArray();
		for (int i = 0; i < msgc.length; ++i){
			char c = msgc[i];
			String cstr = c + "";
			if(c == '\"'){
				cstr = (first? c + bold: col + c);
				first = !first;
			}
			newMsg += cstr;
		}
		return newMsg;
	}

	public static Boolean compstr(String a, String b){
		char[] A = a.toCharArray();
		char[] B = b.toCharArray();
		if(A.length == B.length){
			for(int i = 0; i < A.length; ++i){
				if(A[i] != B[i]) return false;
			}
			return true;
		}return false;
	}

	public static void error(String prefix, int line, int col, String msg){
		if(erroring)System.out.println(red + prefix + " [line:" + line + ", col:" + col + "]:\n\t" + boldQuotes(msg,red) + white);
	}

	public static void error(String msg){
		if(erroring)System.out.println(red + boldQuotes(msg,red) + white);
	}

	public static void warn(String prefix, int line, int col, String msg){
		if(warning)System.out.println(yellow + prefix + " [line:" + line + ", col:" + col + "]:\n\t" + boldQuotes(msg,yellow) + white);
	}

	public static void warn(String msg){
		if(warning)System.out.println(yellow + boldQuotes(msg,yellow) + white);
	}

	public static void success(String str){
		if(successing) System.out.println(green + str + white);
	}

	public static void logln(String str){
		if(logging) System.out.println(str);
	}

	public static void log(String str){
		if(logging) System.out.print(str);
	}

	public static void line(char c){
		if(logging){
			String ln = "";
			for(int i = 0; i < 30; ++i) ln += c;
			logln(ln);
		}
	}

	public static void line(){
		line('=');
	}

	public static final String bold = "\033[0;1m";
	public static final String red = "\033[0;31m";
	public static final String yellow = "\033[0;33m";
	public static final String green = "\033[0;32m";
	public static final String blue = "\033[0;34m";
	public static final String white = "\033[0;37m";
	public static final String grey = "\033[1;30m";

}