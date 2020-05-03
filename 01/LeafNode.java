public class LeafNode extends Node{

	private String value = "";

	public LeafNode(Token.eToken tok){
		super(tok, true);
		value = "";
	}

	public LeafNode(Token.eToken tok, String val){
		super(tok, true);
		value = val;
	}

	public String val(){
		return value;
	}

	public void val(String v){
		value = v;
	}

}