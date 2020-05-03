public class Node{

	protected Token.eToken token;
	protected Boolean isLeafNode;

	public Node(Token.eToken tok, Boolean leaf){
		token = tok;
		isLeafNode = leaf;
	}

	public Boolean isLeaf(){
		return isLeafNode;
	}

	public Token.eToken get(){
		return token;
	}

	public void set(Token.eToken tok){
		token = tok;
	}

}