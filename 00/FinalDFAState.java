public class FinalDFAState extends DFAstate{

	public String Token;

	public FinalDFAState( String name, String Token){
		super(true, name);
		this.Token = Token;
	}

};