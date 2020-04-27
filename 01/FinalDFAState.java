public class FinalDFAState extends DFAstate{

	public Lexer.Token token;
	public Boolean isKeyword;

	public FinalDFAState( String name, Lexer.Token token){
		super(true, name);
		this.token = token;
		isKeyword = false; 
	}

	public FinalDFAState( String name, Lexer.Token token, Boolean iskeyword){
		super(true, name);
		this.token = token;
		isKeyword = iskeyword; 
	}

};