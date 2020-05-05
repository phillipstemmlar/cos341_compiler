import java.util.*;
public class LeafSyntaxNode extends SyntaxNode{

	private String value = "";

	public LeafSyntaxNode(Token.eToken tok, String err){
		super(tok, true, err);
		value = "";
	}

	public LeafSyntaxNode(Token.eToken tok, String err, String val){
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