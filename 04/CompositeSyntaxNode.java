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

	//=======================Symbol Tree Table==============================


	public HashMap<Integer, SyntaxNode> symbolTree(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			table.put(index, this);
			for(int i = 0; i < children.size(); ++i){
				table = children.get(i).symbolTree(table);
			}
		}
		return table;
	}


	//=======================Pruning==============================

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

	//=======================Children functions==============================
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


	//=======================Print TREE Structure==============================

	public String treeString(String prefix) {
		String tree = "";
		tree += name3() + "\n";

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



	//=======================GENERATE INDEX==============================

	public void genIndex(){
		if(parent == null) indexCount = 0;

		index = indexCount;
		indexCount++;
		for(SyntaxNode child : children){
			child.genIndex();
		}
	}

	//=======================EOF==========================================
}