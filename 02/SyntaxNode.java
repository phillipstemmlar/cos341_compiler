import java.util.*;
public class SyntaxNode{

	protected Token.eToken token;
	protected Boolean isLeafNode;
	public Boolean incFound = true;

	protected String errorString;
	protected CompositeSyntaxNode parent;

	protected Integer index;
	protected static Integer indexCount = 0;

	protected Integer line = -1;
	protected Integer col = -1;

	public SyntaxNode(Token.eToken tok, Boolean leaf, String err){
		token = tok;
		isLeafNode = leaf;
		errorString = err;
		parent = null;
		index = -1;
	}

	public HashMap<Integer, SyntaxNode> symbolTree(){
		HashMap<Integer, SyntaxNode> table = symbolTree(new HashMap<>());
		return table;
	}

	public HashMap<Integer, SyntaxNode> symbolTree(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			table.put(index, this);
		}
		return table;
	}

	public void genIndex(){
		if(parent == null) indexCount = 0;
		index = indexCount;
		indexCount++;
	}

	public void prune(){
	}

	public String name(){
		return index + " " + token + "";
	}

	public String name2(){
		return token + "";
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
		return "└──" + treeString("   ");
	}

	public String treeString(String prefix) {
		return name();
	}


	public void  line(Integer ln){
		line = ln;
	}

	public void col(Integer cl){
		col = cl;
	}

	public Integer line(){
		return line;
	}

	public Integer col(){
		return col;
	}

	public String error(){
		return errorString;
	}

	public void error(String err){
		errorString = err;
	}

	public String toString(){
		return name();
	}

	public SyntaxNode setIncludeFound(Boolean inc){
		incFound = inc;
		return this;
	}

	public Boolean includeFound(){
		return incFound;
	}

}