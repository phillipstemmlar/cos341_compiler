import java.util.*;
public class LeafNode extends Node{

	private String value = "";

	public LeafNode(Token.eToken tok, String err){
		super(tok, true, err);
		value = "";
	}

	public LeafNode(Token.eToken tok, String err, String val){
		super(tok, true, err);
		value = val;
	}

	public String val(){
		return value;
	}

	public void val(String v){
		value = v;
	}

	public String treeString(String prefix) {
		return token + " '"+ value +"'";
	}

}