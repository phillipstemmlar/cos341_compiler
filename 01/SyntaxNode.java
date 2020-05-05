import java.util.*;
public class SyntaxNode{

	protected Token.eToken token;
	protected Boolean isLeafNode;
	public Boolean incFound = true;

	protected String errorString;

	public SyntaxNode(Token.eToken tok, Boolean leaf, String err){
		token = tok;
		isLeafNode = leaf;
		errorString = err;
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

	public String treeString() {
		return treeString("");
	}

	public String treeString(String prefix) {
		return "";
	}

	public String error(){
		return errorString;
	}

	public void error(String err){
		errorString = err;
	}

	public String toString(){
		return token + "";
	}

	public SyntaxNode setIncludeFound(Boolean inc){
		incFound = inc;
		return this;
	}

	public Boolean includeFound(){
		return incFound;
	}

}