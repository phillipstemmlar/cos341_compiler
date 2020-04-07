public class FinalState extends DFAstate{

	public String Token;

	public FinalState( String name, String Token){
		super(true, name);
		this.Token = Token;
	}

};