public class FinalDFAState extends DFAstate{

	public Token.eToken token;
	public Boolean isKeyword;

	public FinalDFAState( String name, Token.eToken token){
		super(true, name);
		this.token = token;
		isKeyword = false; 
	}

	public FinalDFAState( String name, Token.eToken token, Boolean iskeyword){
		super(true, name);
		this.token = token;
		isKeyword = iskeyword; 
	}

};