import java.util.*;
public class CompositeNode extends Node{

	List<Node> children;

	public CompositeNode(Token.eToken tok){
		super(tok, false);
		children = new ArrayList<>();
	}

	public void addChild(Node child){
		children.add(child);
	}

	public Node getChild(int index){
		return children.get(index);
	}

	public Node[] childrenArray(){
		if(children.size() == 0) return null;

		Node[] array = new Node[children.size()];
		for(int i = 0; i < children.size(); ++i){
			array[i] = children.get(i);
		}
		return array;
	}

}