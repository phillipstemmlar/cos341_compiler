import java.util.*;
public class CompositeSyntaxNode extends SyntaxNode{

	List<SyntaxNode> children;

	private static Token.eToken[] pruneTokens = {
		Token.eToken.PROC_DEFS,
		Token.eToken.PROC_DEFS_PART,
		Token.eToken.PROC_DEFS_PART2,
		Token.eToken.CODE_PART,
		Token.eToken.DECL_PART, 
		Token.eToken.DECL,
		Token.eToken.ELSE_PART,
		Token.eToken.VALUE_PART,
		Token.eToken.BOOL2,
	};

	public CompositeSyntaxNode(Token.eToken tok, String err){
		super(tok, false, err);
		children = new ArrayList<>();
	}

	public String treeIndexString(){
		String tree = index + ":";
		for(int i = 0; i < children.size(); ++i){
			SyntaxNode child = children.get(i);
			tree += child.index + ( (i < children.size()-1)? "," : "");
		}
		tree += "\n";

		for(int i = 0; i < children.size(); ++i){
			SyntaxNode child = children.get(i);
			if(!child.isLeaf()) tree += child.treeIndexString();
		}

		return tree;
	}

	protected SortedMap<Integer, String> symbolTable( SortedMap<Integer, String> table){
		if(table != null && index >= 0){
			table.put(index, name2());
			for(int i = 0; i < children.size(); ++i){
				children.get(i).symbolTable(table);
			}
		}
		return table;
	}

	public void prune(){
		if(children.size() == 0){
			if(parent != null) parent.removeChild(this);
			return;
		}
		pruneChildren();
		Boolean shouldPrune = false;
		for(int i = 0; i < pruneTokens.length; ++i) shouldPrune |= (token == pruneTokens[i]); 
		if(shouldPrune){
			if(token == Token.eToken.DECL){
				if(children.size() == 1 && getChild(0).get() == Token.eToken.CODE){
					if(parent != null) parent.appendChild(children.get(0));
					removeChild(children.get(0));
					if(parent != null) parent.removeChild(this);
				}
			}else{
				if(children.size() > 0){
					SyntaxNode[] ChildrenArray = new SyntaxNode[children.size()];
					for(int i = 0; i < children.size(); ++i){
						ChildrenArray[i] = children.get(i);
					}
					for(SyntaxNode child : ChildrenArray){
						if(parent != null) parent.appendChild(child);
						removeChild(child);
					}
				}
				if(parent != null) parent.removeChild(this);
			}
		}
	}

	private void pruneChildren(){
		if(children == null) return;

		if(children.size() > 0){
			SyntaxNode[] ChildrenArray = new SyntaxNode[children.size()];
			for(int i = 0; i < children.size(); ++i){
				ChildrenArray[i] = children.get(i);
			}
			for(SyntaxNode child : ChildrenArray){
				child.prune();
			}
		}
	}

	public void addChild(SyntaxNode child){
		child.parent = this;
		children.add(0,child);
	}

	private void appendChild(SyntaxNode child){
		child.parent = this;
		children.add(child);
	}


	public void removeChild(SyntaxNode child){
		child.parent = null;
		children.remove(child);
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
		tree += name() + "\n";

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

	public void genIndex(){
		if(parent == null) indexCount = 0;

		index = indexCount;
		indexCount++;
		for(SyntaxNode child : children){
			child.genIndex();
		}
	}

}