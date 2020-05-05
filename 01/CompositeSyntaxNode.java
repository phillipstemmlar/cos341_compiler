import java.util.*;
public class CompositeSyntaxNode extends SyntaxNode{

	List<SyntaxNode> children;

	public CompositeSyntaxNode(Token.eToken tok, String err){
		super(tok, false, err);
		children = new ArrayList<>();
	}

	public void addChild(SyntaxNode child){
		// children.add(child);
		children.add(0,child);
	}

	public SyntaxNode getChild(int index){
		return children.get(index);
	}

	public SyntaxNode[] childrenArray(){
		if(children.size() == 0) return null;

		SyntaxNode[] array = new SyntaxNode[children.size()];
		for(int i = 0; i < children.size(); ++i){
			array[children.size() -1 -i] = children.get(i);
		}
		return array;
	}

	public String treeString(String prefix) {
		String tree = "";
		tree += token + "\n";

		for(int i = 0; i < children.size(); ++i){
			SyntaxNode child = children.get(i);
			tree += prefix;
			tree += (i < children.size()-1)? "├──" : "└──" ;
			if(child.isLeaf()){
				tree += ((LeafSyntaxNode)child).treeString(prefix + ((i < children.size()-1)? "|  " : "   "));
				tree += "\n";
			}else{
				tree += ((CompositeSyntaxNode)child).treeString(prefix + ((i < children.size()-1)? "|  " : "   "));
			}
		}
		return tree;
	}

}