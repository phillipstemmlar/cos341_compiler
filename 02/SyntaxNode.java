import java.util.*;
public class SyntaxNode{

	protected Token.eToken token;
	protected Boolean isLeafNode;
	public Boolean incFound = true;

	protected String errorString;
	protected CompositeSyntaxNode parent;

	protected Integer index;
	protected static Integer indexCount = 0;

	public SyntaxNode(Token.eToken tok, Boolean leaf, String err){
		token = tok;
		isLeafNode = leaf;
		errorString = err;
		parent = null;
		index = -1;
	}

	public void genIndex(){
		if(parent == null) indexCount = 0;
		index = indexCount;
		indexCount++;
	}

	public String treeIndexString(){
		return "";
	}
	public String symbolTableString(){
		SortedMap<Integer, String> table = symbolTable(new TreeMap<>());
		String output = "";
		for(Integer key: table.keySet()){
			String value = table.get(key);
			if(value != null)output += key + ":" + value + "\n";
		}
		return output;
	}

	protected SortedMap<Integer, String> symbolTable( SortedMap<Integer, String> table){
		if(table != null && index >= 0){
			table.put(index, name2());
		}
		return table;
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