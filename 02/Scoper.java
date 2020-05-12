import java.util.*;

public class Scoper {
	private static String PREFIX;

	HashMap<Integer, Scope> scopes = null;


	public Scoper(String prefix){
		PREFIX = prefix;
		scopes = new HashMap<>();
	}
	
	public HashMap<Integer, Scope> execute(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			extractScopes(table);
			checkScopes(table);
			// printScopes();
			return scopes;
		}
		return null;
	}

	public HashMap<Integer, Scope> executeToFile(HashMap<Integer, SyntaxNode> table, String treeTable_file, String symbolTable_file, String vis_tree_file){
		scopes = execute(table);
		if(scopes != null){
			String treeIndexString = "";
			String treeSymbolString = "";
			for(Integer key : table.keySet()){
				SyntaxNode node = table.get(key);

				treeSymbolString += node.index + ":" + node.name2() + "\n";
				if(!node.isLeaf()){
					CompositeSyntaxNode comp = (CompositeSyntaxNode)node;
					String childIndexList = "";
					for(SyntaxNode child : comp.children){
						childIndexList += "," + child.index;
					}
					treeIndexString += comp.index + ":" + childIndexList.substring(1) + "\n";
				}
			}
			Helper.writeToFile(treeTable_file, treeIndexString);
			Helper.writeToFile(symbolTable_file, treeSymbolString);
			Helper.writeToFile(vis_tree_file, table.get(0).treeString());
		}
		return scopes;
	}


	public void extractScopes(HashMap<Integer, SyntaxNode> table){
		extractScopes(table.get(0), 0, null);
	}

	public void extractScopes(SyntaxNode node, int scopeLevel, Scope parent){
		// String tabs = "";
		// for(int i = 0; i < scopeLevel; ++i) tabs += "\t";

		if(node.token == Token.eToken.PROG){
			// System.out.println(tabs + "======NEW SCOPE=====");
			Scope newScope = new Scope(scopeLevel,node);
			if(parent != null){
				parent.addScope(newScope);
				newScope.parent = parent;
			}
			scopes.put(newScope.ID(), newScope);
			parent = newScope;
		}
		else if(node.token == Token.eToken.DECL){
			CompositeSyntaxNode DECL = (CompositeSyntaxNode)node;
			LeafSyntaxNode NAME_val  = (LeafSyntaxNode)(((CompositeSyntaxNode)DECL.children.get(1)).children.get(0));
			DECL.line(NAME_val.line());
			DECL.col(NAME_val.col());
			parent.DECL_Lines.add(node.index);
		}
		else if(node.token == Token.eToken.PROC){
			CompositeSyntaxNode PROC = (CompositeSyntaxNode)node;
			LeafSyntaxNode NAME_val  = (LeafSyntaxNode)PROC.children.get(1);
			PROC.line(NAME_val.line());
			PROC.col(NAME_val.col());
			parent.PROC_Lines.add(node.index);
		}
		else if(node.token == Token.eToken.VAR) parent.VAR_Lines.add(node.index);
		else if(node.token == Token.eToken.CALL) parent.CALL_Lines.add(node.index);

		// System.out.println(tabs + node.name());

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				if(child.token == Token.eToken.PROG) extractScopes(child, scopeLevel + 1, parent);
				else extractScopes(child, scopeLevel, parent);
			}
		}
	}

	public void checkScopes(HashMap<Integer, SyntaxNode> table){
		if(scopes != null) checkScopes(table, scopes.get(0));
	}

	public void checkScopes(HashMap<Integer, SyntaxNode> table, Scope scope){
		if(scope != null){
			checkPROC(table, scope);
			checkDECL(table, scope);
			checkCALL(table, scope);
			ckeckVAR(table, scope);

			for(Integer id: scope.sub_Scopes.keySet()){
				checkScopes(table, scope.sub_Scopes.get(id));
			}
		}
	}

	private void checkPROC(HashMap<Integer, SyntaxNode> table, Scope scope){
		if(scope != null){
			for(Integer i : scope.PROC_Lines){
				SyntaxNode node = table.get(i);

				CompositeSyntaxNode PROC = (CompositeSyntaxNode)node;
				LeafSyntaxNode NAME_val  = (LeafSyntaxNode)PROC.children.get(1);
				String name = NAME_val.val();

				Procedure findProc = scope.findProcedure(name);

				if(findProc == null ){
					Procedure proc = new Procedure(name, i, scope.lvl());
					scope.addProcedure(proc);
					NAME_val.val(proc.name());
					NAME_val.scope(proc.scopeLevel);
				}else{
					NAME_val.val(Procedure.noName());
					Helper.error(PREFIX,PROC.line(),PROC.col(),errorRedeclaredProcedure(name,findProc.declLine(table)));
				}

			}
		}
	}

	private void checkDECL(HashMap<Integer, SyntaxNode> table, Scope scope){
		if(scope != null){
			for(Integer i : scope.DECL_Lines){
				SyntaxNode node = table.get(i);

				CompositeSyntaxNode DECL = (CompositeSyntaxNode)node;
				CompositeSyntaxNode TYPE = (CompositeSyntaxNode)DECL.children.get(0);
				CompositeSyntaxNode NAME = (CompositeSyntaxNode)DECL.children.get(1);
				LeafSyntaxNode NAME_val  = (LeafSyntaxNode)NAME.children.get(0);
				String name = NAME_val.val();

				Variable findVar = scope.findVariable(name);

				if(findVar == null || findVar.lvl() != scope.lvl()){
					Variable var = new Variable(Helper.tokenToType(TYPE.children.get(0).token),  name, i, scope.lvl());
					scope.addVariable(var);
					NAME_val.val(var.name());
					NAME_val.scope(var.lvl());
				}else{
					NAME_val.val(Variable.noName());
					Helper.error(PREFIX,DECL.line(),DECL.col(),errorRedeclaredInSameScope(name,findVar.declLine(table)));
				}
			}
			
		}
	}

	private void checkCALL(HashMap<Integer, SyntaxNode> table, Scope scope){
		if(scope != null){
			for(Integer i : scope.CALL_Lines){
				SyntaxNode node = table.get(i);

				CompositeSyntaxNode CALL = (CompositeSyntaxNode)node;
				LeafSyntaxNode PROC_name  = (LeafSyntaxNode)CALL.children.get(0);
				String name = PROC_name.val();
				Procedure proc = scope.findProcedure(name);
				scope.addProcedure(proc);
				
				if(proc != null){
					PROC_name.val(proc.name());
					PROC_name.scope(proc.scopeLevel);
				 }
				else PROC_name.val(Procedure.noName());
			}
		}
	}

	private void ckeckVAR(HashMap<Integer, SyntaxNode> table, Scope scope){
		if(scope != null){
			for(Integer i : scope.VAR_Lines){
				SyntaxNode node = table.get(i);

				CompositeSyntaxNode VAR = (CompositeSyntaxNode)node;
				LeafSyntaxNode VAR_name  = (LeafSyntaxNode)VAR.children.get(0);
				String name = VAR_name.val();
				Variable var = scope.findVariable(name);

				if(var != null && var.index <= i){
					VAR_name.val(var.name());
					scope.addVariable(var);
					VAR_name.scope(var.scopeLevel);
				}
				else {
					VAR_name.val(Variable.noName());
					Helper.error(PREFIX,VAR_name.line(),VAR_name.col(),errorUndefinedVariable(name));
				}
			}
		}
	}
	private String errorRedeclaredInSameScope(String name, Integer declLine){
		return "Variable \""+name+"\" has already been declared on line: "+declLine+" "
		+"\n\tMultiple variables with the same name cannot be declared in the same scope.";
	}

	private String errorUndefinedVariable(String name){
		return "Assigning a value to an undefined variable \""+name+"\"";
	}

	private String errorRedeclaredProcedure(String name, Integer declLine){
		return "Procedure \""+name+"\" has already been declared in an outer scope on line: "+declLine+" "
		+"\n\tProcedures cannot have the same name as any procedure in an outer scope that wraps around the scope that it is declared in";
	}

	public Scope getScope(Integer scopeID){
		if(scopes != null) return scopes.get(scopeID);
		return null;
	}

	public void printScopes(){
		if(scopes != null){
			for(Integer key : scopes.keySet()) scopes.get(key).print();
		}else{
			System.out.println("scopes is null");
		}
	}

}