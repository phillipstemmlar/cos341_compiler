import java.util.*;

public class Main{

	public static void main(String[] args) {
		String filename = args[0];
		
		Lexer lex = new Lexer();

		lex.execute(filename);

	}

}