import java.util.*;
public class SyntaxNode{

	protected Token.eToken token;
	protected Boolean isLeafNode;
	public Boolean incFound = true;

	protected String errorString;
	protected CompositeSyntaxNode parent;

	protected Integer index;
	protected static Integer indexCount = 0;
	protected Integer innerScopeID_PROG;

	protected Integer line = -1;
	protected Integer col = -1;

	public Variable.Type type = Variable.Type.none;

	public SyntaxNode(Token.eToken tok, Boolean leaf, String err){
		token = tok;
		isLeafNode = leaf;
		errorString = err;
		parent = null;
		index = -1;
		innerScopeID_PROG = -1;
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
		return index + " " + token + "" ;
	}

	public String name3(){
		return token + "" + typeStr();
	}

	public String name2(){
		return name() + strType();
	}

	private Boolean validType(){
		return (type != Variable.Type.none && type != Variable.Type.undefined && type != Variable.Type.notype);
	}

	public String typeStr(){
		return (validType()?"\tType: " + Helper.typeStr(type) : "");
	}

	public String strType(){
		return (validType()?"\n\t└─" + typeStr() : "");
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

	public void innerScopeID(Integer id){
		innerScopeID_PROG = id;
	}

	public Integer innerScopeID(){
		return innerScopeID_PROG;
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