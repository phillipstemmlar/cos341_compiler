import java.util.*;

public class Main{

	public static void main(String[] args) {
		String inputfile = args[0];
		String outputfile = args[1];
		
		Lexer lex = new Lexer();

		lex.execute(inputfile,outputfile);

	}

}