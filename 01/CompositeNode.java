import java.util.*;
public class CompositeNode extends Node{

	List<Node> children;

	public CompositeNode(Token.eToken tok, String err){
		super(tok, false, err);
		children = new ArrayList<>();
	}

	public void addChild(Node child){
		// children.add(child);
		children.add(0,child);
	}

	public Node getChild(int index){
		return children.get(index);
	}

	public Node[] childrenArray(){
		if(children.size() == 0) return null;

		Node[] array = new Node[children.size()];
		for(int i = 0; i < children.size(); ++i){
			array[children.size() -1 -i] = children.get(i);
		}
		return array;
	}

	public String treeString(String prefix) {
		String tree = "";
		tree += token + "\n";

		for(int i = 0; i < children.size(); ++i){
			Node child = children.get(i);
			tree += prefix;
			tree += (i < children.size()-1)? "├──" : "└──" ;
			if(child.isLeaf()){
				tree += ((LeafNode)child).treeString(prefix + ((i < children.size()-1)? "|  " : "   "));
				tree += "\n";
			}else{
				tree += ((CompositeNode)child).treeString(prefix + ((i < children.size()-1)? "|  " : "   "));
			}
		}
		return tree;
	}

}