public class Lexer{

	Boolean logging = true;

	public Lexer(){

	}


	private void log(String out){
		if(logging){
			System.out.println("LEXER:\t" + out);
		}
	}
}