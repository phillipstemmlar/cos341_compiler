import java.util.*;

public class SemanticAnalyzer {
	private static String scope_PREFIX;
	private static String type_PREFIX;
	private static String value_PREFIX;
	private static String value_PREFIX_w;

	HashMap<Integer, Scope> scopes = null;
	HashMap<Integer, SyntaxNode> nodeTable = null;

	Boolean ERRORS = false;
	Boolean WARNINGS = false;
	
	public SemanticAnalyzer(String s_prefix, String t_prefix, String v_prefix, String v_prefix_w){
		scope_PREFIX = s_prefix;
		type_PREFIX = t_prefix;
		value_PREFIX = v_prefix;
		value_PREFIX_w = v_prefix_w;
		scopes = new HashMap<>();
	}

	//=============================================Scope Checking=================================================
	//region
	
	public HashMap<Integer, Scope> executeScope(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			ERRORS = false;
			extractScopes(table);
			checkScopes(table);
			return (ERRORS)? null : scopes;
		}
		return null;
	}
	public HashMap<Integer, Scope> executeScopeToFile(HashMap<Integer, SyntaxNode> table, String treeTable_file, String symbolTable_file, String vis_tree_file){
		scopes = executeScope(table);
		if(scopes != null){
			String treeIndexString = "", treeSymbolString = "";
			for(Integer key : table.keySet()){
				SyntaxNode node = table.get(key);
				treeSymbolString += node.index + ":" + node.name2() + "\n";
				if(!node.isLeaf()){
					CompositeSyntaxNode comp = (CompositeSyntaxNode)node;
					String childIndexList = "";
					for(SyntaxNode child : comp.children) childIndexList += "," + child.index;
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
			node.innerScopeID(newScope.ID());
			parent = newScope;
		}
		else if(node.token == Token.eToken.DECL){
			CompositeSyntaxNode DECL = (CompositeSyntaxNode)node;
			CompositeSyntaxNode TYPE = (CompositeSyntaxNode)DECL.children.get(0);
			CompositeSyntaxNode NAME = (CompositeSyntaxNode)DECL.children.get(1);
			LeafSyntaxNode NAME_val  = (LeafSyntaxNode)NAME.children.get(0);
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
		// else if(){
		// 	if(!node.isLeaf()){
		// 		for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
		// 			if(child.token == Token.eToken.CODE) child.branchParentIndex = node.index;
		// 		}
		// 	}
		// }

		// System.out.println(tabs + node.name());

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				if((node.token == Token.eToken.COND_BRANCH || node.token == Token.eToken.COND_LOOP) && child.token == Token.eToken.CODE){
					child.branchParentIndex = node.index;
				}else child.branchParentIndex = node.branchParentIndex;

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

				Procedure findProc = scope.checkProcedure(name);

				if(findProc == null){
					Procedure proc = new Procedure(name, i, scope, scopes.get(PROC.children.get(2).innerScopeID()) );
					scope.addProcedure(proc);
					NAME_val.val(proc.name());
					NAME_val.scope(proc.lvl());
				}else{
					NAME_val.val(Procedure.noName());		
					String errorString = errorRedeclaredParentProcedure(name,findProc.declLine(table));
					if(scope.ID() == findProc.scopeID()){
						errorString = errorRedeclaredProcedure(name,findProc.declLine(table));
					}
					Helper.error(scope_PREFIX,PROC.line(),PROC.col(),errorString);
					ERRORS = true;
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
					Helper.error(scope_PREFIX,DECL.line(),DECL.col(),errorRedeclaredInSameScope(name,findVar.declLine(table)));
					ERRORS = true;
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
					PROC_name.scope(proc.lvl());
				 }
				else {PROC_name.val(Procedure.noName());
					Helper.error(scope_PREFIX,PROC_name.line(),PROC_name.col(),errorUndefinedProcedure(name));
					ERRORS = true;
				}
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
					Helper.error(scope_PREFIX,VAR_name.line(),VAR_name.col(),errorUndefinedVariable(name));
					ERRORS = true;
				}
			}
		}
	}
	//endregion
	//=============================================Type Checking=================================================
	//region
	public HashMap<Integer, Scope> executeType(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			ERRORS = false;
			extractTypes(table);
			return (ERRORS)? null : scopes;
		}
		return null;
	}
	public HashMap<Integer, Scope> executeTypeToFile(HashMap<Integer, SyntaxNode> table, String treeTable_file, String symbolTable_file, String vis_tree_file){
		scopes = executeType(table);
		if(scopes != null){
			String treeIndexString = "", treeSymbolString = "", treeString = table.get(0).treeString();
			for(Integer key : table.keySet()){
				SyntaxNode node = table.get(key);
				treeSymbolString += node.index + ":" + node.name2() + "\n";
				if(!node.isLeaf()){
					CompositeSyntaxNode comp = (CompositeSyntaxNode)node;
					String childIndexList = "";
					for(SyntaxNode child : comp.children) childIndexList += "," + child.index;
					treeIndexString += comp.index + ":" + childIndexList.substring(1) + "\n";
				}
			}
			// System.out.println(treeString);
			Helper.writeToFile(treeTable_file, treeIndexString);
			Helper.writeToFile(symbolTable_file, treeSymbolString);
			Helper.writeToFile(vis_tree_file, treeString);
		}
		return scopes;
	}

	private void extractTypes(HashMap<Integer, SyntaxNode> table){
		if(scopes != null) extractTypes(table.get(0), scopes.get(0), true);
	}

	private void extractTypes(SyntaxNode node, Scope scope, Boolean first){
		if(!first && node.token == Token.eToken.PROG){
			scope = scope.getScopeByIndex(node.index);
			if(scope == null) return;
		}
		
		node.type = checkType(node,scope);

		// String tabs = ""; for(int i = 0; i < scope.lvl(); ++i) tabs += "\t";
		// System.out.println(tabs+node.name3());

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				extractTypes(child, scope, false);
			}
		}

	}

	public Variable.Type checkType(SyntaxNode node, Scope scope){
		if(node.type != Variable.Type.none){
			return node.type;
		}
		else if(node.token == Token.eToken.tok_string_literal || node.token == Token.eToken.tok_integer_literal
				|| node.token == Token.eToken.tok_T || node.token == Token.eToken.tok_F
				|| node.token == Token.eToken.tok_string || node.token == Token.eToken.tok_num || node.token == Token.eToken.tok_bool){
			return Helper.tokenToType(node.token);
		}
		else if(node.token == Token.eToken.PROC){
			CompositeSyntaxNode PROC = (CompositeSyntaxNode)node;
			SyntaxNode proc  = PROC.children.get(0);
			SyntaxNode PROC_name  = PROC.children.get(1);
			SyntaxNode prog  = PROC.children.get(2);
			proc.type = checkType(proc, scope);
			PROC_name.type = Procedure.Type;
			prog.type = checkType(prog, scope);
			return Variable.notype();
		}
		else if(node.token == Token.eToken.DECL){
			CompositeSyntaxNode DECL = (CompositeSyntaxNode)node;
			SyntaxNode TYPE = DECL.children.get(0);
			SyntaxNode NAME = DECL.children.get(1);	
			TYPE.type = checkType(TYPE,scope);
			NAME.type = checkType(NAME,scope);
			return Variable.notype();
		}
		else if(node.token == Token.eToken.TYPE){
			CompositeSyntaxNode TYPE = (CompositeSyntaxNode)node;
			SyntaxNode TYPE_val  = TYPE.children.get(0);
			TYPE_val.type = checkType(TYPE_val,scope);
			return TYPE_val.type;
		}
		else if(node.token == Token.eToken.NAME){
			CompositeSyntaxNode NAME = (CompositeSyntaxNode)node;	
			SyntaxNode NAME_val  = NAME.children.get(0);		
			Variable var = scope.findVariableByName(((LeafSyntaxNode)NAME_val).val());
			Variable.Type type = (var == null)? Variable.undefined() : var.type;
			NAME_val.type = type;
			return type;
		}
		else if(node.token == Token.eToken.IO){
			CompositeSyntaxNode IO = (CompositeSyntaxNode)node;
			SyntaxNode io = IO.children.get(0);
			SyntaxNode VAR = IO.children.get(1);	
			io.type = checkType(io,scope);
			VAR.type = checkType(VAR,scope);
			if(VAR.type != Variable.Type.string && VAR.type != Variable.Type.num && VAR.type != Variable.Type.bool){
				SyntaxNode leaf = getFirstLeaf(VAR);
				Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorInvalidIOtype(io.token,VAR.type));
				ERRORS = true;
			}
			return Variable.notype();
		}
		else if(node.token == Token.eToken.VAR){
			CompositeSyntaxNode VAR = (CompositeSyntaxNode)node;
			SyntaxNode VAR_val  = VAR.children.get(0);
			Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
			Variable.Type type = (var == null)? Variable.undefined() : var.type;
			VAR_val.type = type;
			return type;
		}
		else if(node.token == Token.eToken.CALL){
			CompositeSyntaxNode CALL = (CompositeSyntaxNode)node;
			SyntaxNode PROC_name  = CALL.children.get(0);
			Procedure proc = scope.findProcedureByName(((LeafSyntaxNode)PROC_name).val());
			PROC_name.type = (proc == null)? Variable.undefined() : Procedure.Type;
			return Variable.notype();
		}
		else if(node.token == Token.eToken.ASSIGN){
			CompositeSyntaxNode ASSIGN = (CompositeSyntaxNode)node;
			SyntaxNode VAR  = ASSIGN.children.get(0);
			SyntaxNode VALUE  = ASSIGN.children.get(1);
			VAR.type = checkType(VAR,scope);
			VALUE.type = checkType(VALUE,scope);
			if(VAR.type != VALUE.type){
				SyntaxNode VAR_name  = ((CompositeSyntaxNode)VAR).children.get(0);
				Variable var = scope.getVariableByName(((LeafSyntaxNode)VAR_name).val());
				SyntaxNode leaf = getFirstLeaf(VALUE);
				Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorAssigningInvalidTypes(var.ogname, VAR.type,VALUE.type));
				ERRORS = true;
			}
			return Variable.notype();
		}
		else if(node.token == Token.eToken.NUMEXPR){
			CompositeSyntaxNode NUMEXPR = (CompositeSyntaxNode)node;
			SyntaxNode NUM_val  = NUMEXPR.children.get(0);
			NUM_val.type = checkType(NUM_val, scope);
			if(NUM_val.type != Variable.Type.num){
				SyntaxNode leaf = getFirstLeaf(NUM_val);
				Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorNumExpr(NUM_val.type));
				ERRORS = true;
			}
			return NUM_val.type;
		}
		else if(node.token == Token.eToken.CALC){
			CompositeSyntaxNode CALC = (CompositeSyntaxNode)node;
			SyntaxNode OP  = CALC.children.get(0);
			SyntaxNode NUM_1  = CALC.children.get(1);
			SyntaxNode NUM_2  = CALC.children.get(2);
			OP.type = checkType(OP, scope);
			NUM_1.type = checkType(NUM_1, scope);
			NUM_2.type = checkType(NUM_2, scope);
			if(NUM_1.type != Variable.Type.num){
				SyntaxNode leaf = getFirstLeaf(NUM_1);
				Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_calc(OP.token,NUM_1.type));
				ERRORS = true;
			}
			if(NUM_2.type != Variable.Type.num){
				SyntaxNode leaf = getFirstLeaf(NUM_2);
				Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_calc(OP.token,NUM_2.type));
				ERRORS = true;
			}

			return Variable.Type.num;
		}
		else if(node.token == Token.eToken.BOOL){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);
			SyntaxNode SECOND = (((CompositeSyntaxNode)node).children.size() >= 2)?((CompositeSyntaxNode)node).children.get(1) : null;
			
			if(SECOND != null && (SECOND.token == Token.eToken.tok_greater_than || SECOND.token == Token.eToken.tok_less_than )){
				SyntaxNode VAR_1 = ((CompositeSyntaxNode)node).children.get(0);
				SyntaxNode VAR_2 = ((CompositeSyntaxNode)node).children.get(2);
				VAR_1.type = checkType(VAR_1,scope);
				VAR_2.type = checkType(VAR_2,scope);
				if(VAR_1.type != Variable.Type.num){
					SyntaxNode leaf = getFirstLeaf(VAR_1);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errBool_greater_less_than(SECOND.token, VAR_1.type));
					ERRORS = true;
				}
				if(VAR_2.type != Variable.Type.num){
					SyntaxNode leaf = getFirstLeaf(VAR_2);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errBool_greater_less_than(SECOND.token, VAR_2.type));
					ERRORS = true;
				}
				return Variable.Type.bool;
			}
			else if(FIRST.token == Token.eToken.VAR){
				Variable.Type type = checkType(FIRST,scope);
				if(type != Variable.Type.bool){
					SyntaxNode VAR_name  = ((CompositeSyntaxNode)FIRST).children.get(0);
					Variable var = scope.getVariableByName(((LeafSyntaxNode)VAR_name).val());
					SyntaxNode leaf = getFirstLeaf(FIRST);

					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_VAR(var.ogname, type));
					ERRORS = true;
				}	
			}
			else if(FIRST.token == Token.eToken.tok_eq){
				SyntaxNode VAR_1 = ((CompositeSyntaxNode)node).children.get(1);
				SyntaxNode VAR_2 = ((CompositeSyntaxNode)node).children.get(2);
				VAR_1.type = checkType(VAR_1,scope);
				VAR_2.type = checkType(VAR_2,scope);
				if(VAR_1.type != VAR_2.type){
					SyntaxNode leaf = getFirstLeaf(VAR_2);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_eq(VAR_1.type,VAR_2.type));
					ERRORS = true;
				}
				return Variable.Type.bool;
			}else if(FIRST.token == Token.eToken.tok_or || FIRST.token == Token.eToken.tok_and){
				SyntaxNode VAR_1 = ((CompositeSyntaxNode)node).children.get(1);
				SyntaxNode VAR_2 = ((CompositeSyntaxNode)node).children.get(2);
				VAR_1.type = checkType(VAR_1,scope);
				VAR_2.type = checkType(VAR_2,scope);
				if(VAR_1.type != Variable.Type.bool){
					SyntaxNode leaf = getFirstLeaf(VAR_1);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_or_and_not(FIRST.token, VAR_1.type));
					ERRORS = true;
				}
				if(VAR_2.type != Variable.Type.bool){
					SyntaxNode leaf = getFirstLeaf(VAR_2);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_or_and_not(FIRST.token, VAR_2.type));
					ERRORS = true;
				}
				return Variable.Type.bool;
			}
			else if(FIRST.token == Token.eToken.tok_not){
				SyntaxNode VAR = ((CompositeSyntaxNode)node).children.get(1);
				VAR.type = checkType(VAR,scope);
				if(VAR.type != Variable.Type.bool){
					SyntaxNode leaf = getFirstLeaf(VAR);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_or_and_not(FIRST.token, VAR.type));
					ERRORS = true;
				}
				return Variable.Type.bool;
			}
			return Variable.Type.bool;
		}
		else if (node.token == Token.eToken.COND_LOOP){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);

			if(FIRST.token == Token.eToken.tok_for){
				int[] indices = {1,3,5,6,8};
				for(int i = 0; i < indices.length; ++i){
					SyntaxNode VAR = ((CompositeSyntaxNode)node).children.get(indices[i]);
					VAR.type = checkType(VAR,scope);
					if(VAR.type != Variable.Type.num){
						SyntaxNode VAR_name  = ((CompositeSyntaxNode)VAR).children.get(0);
						Variable var = scope.getVariableByName(((LeafSyntaxNode)VAR_name).val());
						SyntaxNode leaf = getFirstLeaf(VAR);
						Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_for(var.ogname,VAR.type));
						ERRORS = true;
					}
				}
				return Variable.notype();
			}
			else if(FIRST.token == Token.eToken.tok_while){
				SyntaxNode VAR = ((CompositeSyntaxNode)node).children.get(1);
				VAR.type = checkType(VAR,scope);
				if(VAR.type != Variable.Type.bool){
					SyntaxNode leaf = getFirstLeaf(VAR);
					Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_while(VAR.type));
					ERRORS = true;
				}
				return Variable.notype();
			}
		}
		else if (node.token == Token.eToken.COND_BRANCH){
			SyntaxNode VAR = ((CompositeSyntaxNode)node).children.get(1);
			VAR.type = checkType(VAR,scope);
			if(VAR.type != Variable.Type.bool){
				SyntaxNode leaf = getFirstLeaf(VAR);
				Helper.error(type_PREFIX, leaf.line(), leaf.col(), errorBool_if(VAR.type));
				ERRORS = true;
			}
			return Variable.notype();
		}

		return Variable.notype();
	}
	//endregion
	//=============================================Value Checking=================================================
	//region
	public HashMap<Integer, Scope> executeValue(HashMap<Integer, SyntaxNode> table){
		if(table != null){
			SyntaxNode.printHasValue = true;
			ERRORS = false;
			WARNINGS = false;
			extractValue(table);
			// ERRORS = false;
			return (ERRORS)? null : scopes;
		}
		return null;
	}
	public HashMap<Integer, Scope> executeValueToFile(HashMap<Integer, SyntaxNode> table, String treeTable_file, String symbolTable_file, String vis_tree_file){
		scopes = executeValue(table);
		if(scopes != null){
			String treeIndexString = "", treeSymbolString = "", treeString = table.get(0).treeString();
			for(Integer key : table.keySet()){
				SyntaxNode node = table.get(key);
				treeSymbolString += node.index + ":" + node.name2() + "\n";
				if(!node.isLeaf()){
					CompositeSyntaxNode comp = (CompositeSyntaxNode)node;
					String childIndexList = "";
					for(SyntaxNode child : comp.children) childIndexList += "," + child.index;
					treeIndexString += comp.index + ":" + childIndexList.substring(1) + "\n";
				}
			}
			Helper.writeToFile(treeTable_file, treeIndexString);
			Helper.writeToFile(symbolTable_file, treeSymbolString);
			Helper.writeToFile(vis_tree_file, treeString);
		}
		return scopes;
	}

	private void extractValue(HashMap<Integer, SyntaxNode> table){
		if(scopes != null) extractValue(table.get(0), scopes.get(0));
	}

	private void extractValue(SyntaxNode node, Scope scope){
		if(node != null && scope != null){
			node.hasValue = checkValue(node,scope, true);
		}
	}

	public Variable.Confirmation checkValue(SyntaxNode node, Scope scope){
		return checkValue(node, scope, false);
	}

	public Variable.Confirmation checkValue(SyntaxNode node, Scope scope, Boolean first){
		return checkValue(node, scope, first, false);
	}

	public Variable.Confirmation checkValue(SyntaxNode node, Scope scope, Boolean first, Boolean condBlock){
		if(node.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(node.index);
			if(scope == null) return Variable.Confirmation.no;
		}
		else if(node.token == Token.eToken.CALL){
			SyntaxNode PROC_name  = ((CompositeSyntaxNode)node).children.get(0);

			Procedure proc = scope.findProcedureByName(((LeafSyntaxNode)PROC_name).val());
			if(proc != null){
				SyntaxNode PROG = proc.innerNode();
				PROG.hasValue = checkValue(PROG, scope);
			}		
			return Variable.Confirmation.no;
		}
		else if(node.token == Token.eToken.tok_string_literal || node.token == Token.eToken.tok_integer_literal
					|| node.token == Token.eToken.tok_T || node.token == Token.eToken.tok_F){
			return Variable.Confirmation.yes;
		}
		else if(node.token == Token.eToken.VAR){
			CompositeSyntaxNode VAR = (CompositeSyntaxNode)node;
			SyntaxNode VAR_val  = VAR.children.get(0);
			Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
			Variable.Confirmation hasVal = (var == null)? Variable.Confirmation.no : var.hasVal();
			VAR_val.hasValue = hasVal;
			return hasVal;
		}
		else if(node.token == Token.eToken.IO){
			CompositeSyntaxNode IO = (CompositeSyntaxNode)node;
			SyntaxNode io  = IO.children.get(0);
			SyntaxNode VAR  = IO.children.get(1);
			if(io.token == Token.eToken.tok_input){
				SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
				Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
				if(condBlock && var.hasVal() == Variable.Confirmation.no){
					var.hasVal(Variable.Confirmation.maybe);
				}
				else if(condBlock && var.hasVal() == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
					Boolean value = false;
					if(var.initValueIndex >= 0){
						SyntaxNode PARENT = nodeTable.get(node.branchParentIndex);
						SyntaxNode INIT = nodeTable.get(var.initValueIndex);

						SyntaxNode BOOL = ((CompositeSyntaxNode)PARENT).children.get(1);

						if(BOOL.hasValue == Variable.Confirmation.yes && ((CompositeSyntaxNode)PARENT).children.size() > 4 
							&& INIT.branchParentIndex == node.branchParentIndex){
							SyntaxNode THEN_CODE = ((CompositeSyntaxNode)PARENT).children.get(3);
							SyntaxNode ELSE_CODE = ((CompositeSyntaxNode)PARENT).children.get(5);
							value = (INIT != null && INIT.index < ELSE_CODE.index && node.index > ELSE_CODE.index);
						}	
					}
					if(value){		
						var.hasVal(Variable.Confirmation.yes);
					}
				}
				else{
					var.hasVal(Variable.Confirmation.yes);
				}
				var.initValueIndex = node.index;
				VAR.hasValue = checkValue(VAR, scope);
			}else if(io.token == Token.eToken.tok_output){
				VAR.hasValue = checkValue(VAR, scope);
				if(VAR.hasValue == Variable.Confirmation.no){
					SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
					Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
					SyntaxNode leaf = getFirstLeaf(VAR);
					Helper.error(value_PREFIX,leaf.line(),leaf.col(), errorNoValue(var.ogname));
					ERRORS = true;
				}
				else if(VAR.hasValue == Variable.Confirmation.maybe){
					SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
					Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					if(var.initValueIndex >= 0){
						SyntaxNode INIT = nodeTable.get(var.initValueIndex);
						// Helper.success("par:   " + INIT.branchParentIndex);
						// Helper.success("child: " + node.branchParentIndex);
						value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					}
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR);
						Helper.warn(value_PREFIX_w,leaf.line(),leaf.col(), warnNoValue(var.ogname));
						WARNINGS = true;
					}

				}
			}
			return Variable.Confirmation.no;
		}
		else if(node.token == Token.eToken.ASSIGN){
			CompositeSyntaxNode ASSIGN = (CompositeSyntaxNode)node;
			SyntaxNode VAR  = ASSIGN.children.get(0);
			SyntaxNode VALUE  = ASSIGN.children.get(1);
			VALUE.hasValue = checkValue(VALUE, scope);
			if(VALUE.hasValue == Variable.Confirmation.yes){
				SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
				Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
				if(condBlock && var.hasVal() == Variable.Confirmation.no){
					var.hasVal(Variable.Confirmation.maybe);
				}
				else if(condBlock && var.hasVal() == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
					Boolean value = false;
					if(var.initValueIndex >= 0){
						SyntaxNode PARENT = nodeTable.get(node.branchParentIndex);
						SyntaxNode INIT = nodeTable.get(var.initValueIndex);

						SyntaxNode BOOL = ((CompositeSyntaxNode)PARENT).children.get(1);

						if(BOOL.hasValue == Variable.Confirmation.yes && ((CompositeSyntaxNode)PARENT).children.size() > 4 && INIT.branchParentIndex == node.branchParentIndex){
							SyntaxNode THEN_CODE = ((CompositeSyntaxNode)PARENT).children.get(3);
							SyntaxNode ELSE_CODE = ((CompositeSyntaxNode)PARENT).children.get(5);
							value = (INIT != null && INIT.index < ELSE_CODE.index && node.index > ELSE_CODE.index);
						}		
					}
					if(value){		
						var.hasVal(Variable.Confirmation.yes);
					}
				}
				else{
					var.hasVal(Variable.Confirmation.yes);
				}
				var.initValueIndex = node.index;
				VAR.hasValue = checkValue(VAR, scope);
			}else if(VALUE.hasValue == Variable.Confirmation.no){
				SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
				Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
				SyntaxNode leaf = getFirstLeaf(VALUE);
				Helper.error(value_PREFIX,leaf.line(),leaf.col(), errorNoValue_ASSIGN(var.ogname));
				ERRORS = true;
			}
			else if(VALUE.hasValue == Variable.Confirmation.maybe){
				// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VALUE).children.get(0);
				// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

				Boolean value = false;
				// if(var.initValueIndex >= 0){
				// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
				// 	if(INIT != null && INIT.branchParentIndex == node.branchParentIndex){
				// 		value = true;
				// 	}
				// }
				if(!value){
					SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
					Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
					SyntaxNode leaf = getFirstLeaf(VALUE);
					Helper.warn(value_PREFIX_w,leaf.line(),leaf.col(), warnNoValue_ASSIGN(var.ogname));
					WARNINGS = true;
				}

				
			}
			return Variable.Confirmation.no;
		}
		else if(node.token == Token.eToken.NUMEXPR){
			CompositeSyntaxNode NUMEXPR = (CompositeSyntaxNode)node;
			SyntaxNode NUM_val  = NUMEXPR.children.get(0);
			NUM_val.hasValue = checkValue(NUM_val, scope);
			return NUM_val.hasValue;
		}
		else if(node.token == Token.eToken.CALC){
			CompositeSyntaxNode CALC = (CompositeSyntaxNode)node;
			SyntaxNode OP  = CALC.children.get(0);
			SyntaxNode NUM_1  = CALC.children.get(1);
			SyntaxNode NUM_2  = CALC.children.get(2);
			NUM_1.hasValue = checkValue(NUM_1, scope);
			NUM_2.hasValue = checkValue(NUM_2, scope);
			Variable.Confirmation isValue = Variable.Confirmation.yes;
			if(NUM_1.hasValue == Variable.Confirmation.no){
				SyntaxNode leaf = getFirstLeaf(NUM_1);
				Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValueCALC(OP.token));
				ERRORS = true;
				isValue = Variable.Confirmation.no;
			}
			else if(NUM_1.hasValue == Variable.Confirmation.maybe){
				// SyntaxNode VAR_val  = ((CompositeSyntaxNode)NUM_1).children.get(0);
				// if(VAR_val.token == Token.eToken.VAR){
				// 	SyntaxNode VAR_name  = ((CompositeSyntaxNode)VAR_val).children.get(0);
				// }
				// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_name).val());

				Boolean value = false;
				// if(var.initValueIndex >= 0){
				// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
				// 	if(INIT != null && INIT.branchParentIndex == node.branchParentIndex){
				// 		value = true;
				// 	}
				// }
				if(!value){
					SyntaxNode leaf = getFirstLeaf(NUM_1);
					Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValueCALC(OP.token));
					WARNINGS = true;
					if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
				}	
			}

			if(NUM_2.hasValue == Variable.Confirmation.no){
				SyntaxNode leaf = getFirstLeaf(NUM_2);
				Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValueCALC(OP.token));
				ERRORS = true;
				isValue = Variable.Confirmation.no;
			}
			else if(NUM_2.hasValue == Variable.Confirmation.maybe){
				// SyntaxNode VAR_val  = ((CompositeSyntaxNode)NUM_2).children.get(0);
				// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

				Boolean value = false;
				// if(var.initValueIndex >= 0){
				// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
				// 	if(INIT != null && INIT.branchParentIndex == node.branchParentIndex){
				// 		value = true;
				// 	}
				// }
				if(!value){
					SyntaxNode leaf = getFirstLeaf(NUM_2);
					Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValueCALC(OP.token));
					WARNINGS = true;
					if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
				}
				
			}

			return isValue;
		}
		else if(node.token == Token.eToken.BOOL){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);
			SyntaxNode SECOND = (((CompositeSyntaxNode)node).children.size() >= 2)?((CompositeSyntaxNode)node).children.get(1) : null;
			
			if(SECOND != null && (SECOND.token == Token.eToken.tok_greater_than || SECOND.token == Token.eToken.tok_less_than )){
				SyntaxNode VAR_1 = ((CompositeSyntaxNode)node).children.get(0);
				SyntaxNode VAR_2 = ((CompositeSyntaxNode)node).children.get(2);
				VAR_1.hasValue = checkValue(VAR_1,scope);
				VAR_2.hasValue = checkValue(VAR_2,scope);
				Variable.Confirmation isValue = Variable.Confirmation.yes;
				if(VAR_1.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(VAR_1);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_greater_less_than_eq(SECOND.token));
					ERRORS = true;
					isValue = Variable.Confirmation.no;
				}
				else if(VAR_1.hasValue == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR_1).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					// if(var.initValueIndex >= 0){
					// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
					// 	value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					// }
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR_1);
						Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValue_greater_less_than_eq(SECOND.token));
						WARNINGS = true;
						if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
					}
					
				}

				if(VAR_2.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(VAR_2);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_greater_less_than_eq(SECOND.token));
					ERRORS = true;
					isValue = Variable.Confirmation.no;
				}
				else if(VAR_2.hasValue == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR_2).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					// if(var.initValueIndex >= 0){
					// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
					// 	value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					// }
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR_2);
						Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValue_greater_less_than_eq(SECOND.token));
						WARNINGS = true;
						if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
					}	
				}

				return isValue;
			}
			else if(FIRST.token == Token.eToken.tok_eq){
				SyntaxNode VAR_1 = ((CompositeSyntaxNode)node).children.get(1);
				SyntaxNode VAR_2 = ((CompositeSyntaxNode)node).children.get(2);
				VAR_1.hasValue = checkValue(VAR_1,scope);
				VAR_2.hasValue = checkValue(VAR_2,scope);
				Variable.Confirmation isValue = Variable.Confirmation.yes;
				if(VAR_1.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(VAR_1);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_greater_less_than_eq(FIRST.token));
					ERRORS = true;
					isValue = Variable.Confirmation.no;
				}
				else if(VAR_1.hasValue == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR_1).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					// if(var.initValueIndex >= 0){
					// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
					// 	value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					// }
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR_1);
						Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(),  warnValue_greater_less_than_eq(FIRST.token));
						WARNINGS = true;
						if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
					}	

					
				}

				if(VAR_2.hasValue == Variable.Confirmation.no){
					
					SyntaxNode leaf = getFirstLeaf(VAR_2);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_greater_less_than_eq(FIRST.token));
					ERRORS = true;
					isValue = Variable.Confirmation.no;
				}
				else if(VAR_2.hasValue == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR_2).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					// if(var.initValueIndex >= 0){
					// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
					// 	value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					// }
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR_2);
						Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValue_greater_less_than_eq(FIRST.token));
						WARNINGS = true;
						if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
					}

					
				}

				return isValue;
			}else if(FIRST.token == Token.eToken.tok_or || FIRST.token == Token.eToken.tok_and){
				SyntaxNode VAR_1 = ((CompositeSyntaxNode)node).children.get(1);
				SyntaxNode VAR_2 = ((CompositeSyntaxNode)node).children.get(2);
				VAR_1.hasValue = checkValue(VAR_1,scope);
				VAR_2.hasValue = checkValue(VAR_2,scope);
				Variable.Confirmation isValue = Variable.Confirmation.yes;
				if(VAR_1.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(VAR_1);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_and_or_not(FIRST.token));
					ERRORS = true;
					isValue = Variable.Confirmation.no;
				}
				else if(VAR_1.hasValue == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR_1).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					// if(var.initValueIndex >= 0){
					// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
					// 	value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					// }
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR_1);
						Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValue_and_or_not(FIRST.token));
						WARNINGS = true;
						if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
					}
				}

				if(VAR_2.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(VAR_2);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_and_or_not(FIRST.token));
					ERRORS = true;
					isValue = Variable.Confirmation.no;
					
				}
				else if(VAR_2.hasValue == Variable.Confirmation.maybe){
					// SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR_2).children.get(0);
					// Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

					Boolean value = false;
					// if(var.initValueIndex >= 0){
					// 	SyntaxNode INIT = nodeTable.get(var.initValueIndex);
					// 	value = (INIT != null && INIT.branchParentIndex == node.branchParentIndex);
					// }
					if(!value){
						SyntaxNode leaf = getFirstLeaf(VAR_2);
						Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValue_and_or_not(FIRST.token));
						WARNINGS = true;
						if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
					}
					
				}

				return isValue;
			}
			else if(FIRST.token == Token.eToken.tok_not){
				SyntaxNode VAR = ((CompositeSyntaxNode)node).children.get(1);
				VAR.hasValue = checkValue(VAR,scope);
				if(VAR.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(VAR);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValue_and_or_not(FIRST.token));
					ERRORS = true;
					return Variable.Confirmation.no;
				}
				else if(VAR.hasValue == Variable.Confirmation.maybe){
					SyntaxNode leaf = getFirstLeaf(VAR);
					Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValue_and_or_not(FIRST.token));
					WARNINGS = true;
					return Variable.Confirmation.maybe;
				}
				return VAR.hasValue;
			}
			return checkValue(FIRST, scope);
		}
		else if (node.token == Token.eToken.COND_BRANCH){
			SyntaxNode BOOL = ((CompositeSyntaxNode)node).children.get(1);
			BOOL.hasValue = checkValue(BOOL,scope);
			if(BOOL.hasValue == Variable.Confirmation.no){
				SyntaxNode leaf = getFirstLeaf(BOOL);
				Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValueIF());
				ERRORS = true;
			}
			else if(BOOL.hasValue == Variable.Confirmation.maybe){
				SyntaxNode leaf = getFirstLeaf(BOOL);
				Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValueIF());
				WARNINGS = true;
			}
			if(!node.isLeaf()){
				for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
					if(child.token == Token.eToken.CODE)
						child.hasValue = checkValue(child, scope, false, true);
				}
			}
			return Variable.Confirmation.no;
		}
		else if (node.token == Token.eToken.COND_LOOP){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);

			if(FIRST.token == Token.eToken.tok_for){
				SyntaxNode dVAR = ((CompositeSyntaxNode)node).children.get(1);
				SyntaxNode dVALUE = ((CompositeSyntaxNode)node).children.get(2);
				dVALUE.hasValue = checkValue(dVALUE, scope);
				if(dVALUE.hasValue == Variable.Confirmation.yes){
					SyntaxNode VAR_val  = ((CompositeSyntaxNode)dVAR).children.get(0);
					Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());
					if(condBlock && var.hasVal() == Variable.Confirmation.no){
						var.hasVal(Variable.Confirmation.maybe);
					}
					else if(condBlock && var.hasVal() == Variable.Confirmation.maybe){
						Boolean value = false;
						if(var.initValueIndex >= 0){
							SyntaxNode PARENT = nodeTable.get(node.branchParentIndex);
							SyntaxNode INIT = nodeTable.get(var.initValueIndex);
						
							SyntaxNode BOOL = ((CompositeSyntaxNode)PARENT).children.get(1);

							if(BOOL.hasValue == Variable.Confirmation.yes &&((CompositeSyntaxNode)PARENT).children.size() > 4 && INIT.branchParentIndex == node.branchParentIndex){
								SyntaxNode THEN_CODE = ((CompositeSyntaxNode)PARENT).children.get(3);
								SyntaxNode ELSE_CODE = ((CompositeSyntaxNode)PARENT).children.get(5);
								value = (INIT != null && INIT.index < ELSE_CODE.index && node.index > ELSE_CODE.index);
							}		
						}
						if(value){		
							var.hasVal(Variable.Confirmation.yes);
						}
					}
					else{
						var.hasVal(Variable.Confirmation.yes);
					}
					var.initValueIndex = node.index;
					dVAR.hasValue = checkValue(dVAR, scope);
				}else if(dVALUE.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(dVALUE);
					Helper.error(value_PREFIX,leaf.line(),leaf.col(), errorValueFOR(((LeafSyntaxNode)leaf).val()));
					ERRORS = true;
				}

				int[] indices = {3,5,6,8};
				Variable.Confirmation isValue = Variable.Confirmation.yes;
				for(int i = 0; i < indices.length; ++i){
					SyntaxNode VAR = ((CompositeSyntaxNode)node).children.get(indices[i]);
					VAR.hasValue = checkValue(VAR,scope);
					if(VAR.hasValue == Variable.Confirmation.no){
						SyntaxNode VAR_name  = ((CompositeSyntaxNode)VAR).children.get(0);
						Variable var = scope.getVariableByName(((LeafSyntaxNode)VAR_name).val());
						SyntaxNode leaf = getFirstLeaf(VAR);
						Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValueFOR(var.ogname));
						ERRORS = true;
						isValue = Variable.Confirmation.no;
					}
					else if(VAR.hasValue == Variable.Confirmation.maybe){
						SyntaxNode VAR_val  = ((CompositeSyntaxNode)VAR).children.get(0);
						Variable var = scope.findVariableByName(((LeafSyntaxNode)VAR_val).val());

						Boolean value = false;
						if(var.initValueIndex >= 0){
							SyntaxNode INIT = nodeTable.get(var.initValueIndex);
							if(INIT != null && INIT.branchParentIndex == node.branchParentIndex){
								value = true;
							}
						}
						if(!value){
							// SyntaxNode VAR_name  = ((CompositeSyntaxNode)VAR).children.get(0);
							// Variable var = scope.getVariableByName(((LeafSyntaxNode)VAR_name).val());
							SyntaxNode leaf = getFirstLeaf(VAR);
							Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValueFOR(var.ogname));
							WARNINGS = true;
							if(isValue != Variable.Confirmation.no) isValue = Variable.Confirmation.maybe;
						}

					}
				}
				if(!node.isLeaf()){
					for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
						if(child.token == Token.eToken.CODE)
						child.hasValue = checkValue(child, scope, false, true);
					}
				}
				return Variable.Confirmation.no;
			}
			else if(FIRST.token == Token.eToken.tok_while){
				SyntaxNode BOOL = ((CompositeSyntaxNode)node).children.get(1);
				BOOL.hasValue = checkValue(BOOL,scope);
				if(BOOL.hasValue == Variable.Confirmation.no){
					SyntaxNode leaf = getFirstLeaf(BOOL);
					Helper.error(value_PREFIX, leaf.line(), leaf.col(), errorValueWHILE());
					ERRORS = true;
				}
				else if(BOOL.hasValue == Variable.Confirmation.maybe){
					SyntaxNode leaf = getFirstLeaf(BOOL);
					Helper.warn(value_PREFIX_w, leaf.line(), leaf.col(), warnValueWHILE());
					WARNINGS = true;
				}
				if(!node.isLeaf()){
					for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
						if(child.token == Token.eToken.CODE)
						child.hasValue = checkValue(child, scope, false, true);
					}
				}
				return Variable.Confirmation.no;
			}
		}

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				child.hasValue = checkValue(child, scope, false, condBlock);
			}
		}

		return Variable.Confirmation.no;
	}

	//endregion
	//=============================================Additional Methods=================================================

	/*
	private String warnNoValue(String varName){
		return "Variable \""+varName+"\" might not have been initialized with a value before this usage";
	}
	*/

	private String errorValueFOR(String varName){
		return "Variable \""+varName+"\" in for-loop's header does not have a value";
	}

	private String warnValueFOR(String varName){
		return "Variable \""+varName+"\" in for-loop's header might not have a value";
	}

	private String errorValueWHILE(){
		return "While-loop's condition does not have a value";
	}

	private String warnValueWHILE(){
		return "While-loop's condition might not have a value";
	}

	private String errorValueIF(){
		return "If-Then statement's condition does not have a value";
	}

	private String warnValueIF(){
		return "IF-THEN statement's condition might not have a value";
	}

	private String errorValue_and_or_not(Token.eToken token){
		return "Argument of boolean operation \"" + Helper.tokenStr(token) + "\" does not have a value";
	}

	private String warnValue_and_or_not(Token.eToken token){
		return "Argument of boolean operation \"" + Helper.tokenStr(token) + "\" might not have a value";
	}

	private String errorValue_greater_less_than_eq(Token.eToken token){
		return "Argument of comparison operation \"" + Helper.tokenStr(token) + "\" does not have a value";
	}

	private String warnValue_greater_less_than_eq(Token.eToken token){
		return "Argument of comparison operation \"" + Helper.tokenStr(token) + "\" might not have a value";
	}

	private String errorValueCALC(Token.eToken token){
		return "Argument of calculation operation \"" + Helper.tokenStr(token) + "\" does not have a value";
	}

	private String warnValueCALC(Token.eToken token){
		return "Argument of calculation operation \"" + Helper.tokenStr(token) + "\" might not have a value";
	}

	private String errorNoValue_ASSIGN(String varName){
		return "The expression being assigned to \""+varName+"\" does not have a value";
	}

	private String warnNoValue_ASSIGN(String varName){
		return "The expression being assigned to \""+varName+"\" might not have a value";
	}

	private String errorNoValue(String varName){
		return "Variable \""+varName+"\" does not have a value";
	}

	private String warnNoValue(String varName){
		return "Variable \""+varName+"\" might not have a valu";
	}

	private String errorRedeclaredInSameScope(String name, Integer declLine){
		return "Variable \""+name+"\" has already been declared on line: "+declLine+" "
		+"\n\tMultiple variables with the same name cannot be declared in the same scope.";
	}
	
	private String errorUndefinedVariable(String name){
		return "Assigning a value to an undefined variable \""+name+"\"";
	}

	private String errorUndefinedProcedure(String name){
		return "Cannot call undefined procedure \""+name+"\"";
	}

	private String errorRedeclaredProcedure(String name, Integer declLine){
		return "Procedure \""+name+"\" has already been declared in this scope on line: "+Helper.white+declLine +Helper.red;
	}

	private String errorRedeclaredParentProcedure(String name, Integer declLine){
		return "Procedure \""+name+"\" has already been declared in an outer scope on line: "+Helper.white+declLine +Helper.red+ ""
		+ "\n\tProcedures cannot have the same name as an ancestor procedure that wraps around it";
	}

	private String errorInvalidIOtype(Token.eToken token, Variable.Type type){
		return "Invalid argument of type \""+type+"\" for " + Helper.tokenStr(token) + " operation."
		+ "\n\tExpected an expression of type string, num or bool.";
	}

	private String errorAssigningInvalidTypes(String varName, Variable.Type varType, Variable.Type valueType){
		return "Cannot assign a value of type \"" + valueType + "\" to variable \""+varName+"\" of type \""+varType+"\"";
	}

	private String errorNumExpr(Variable.Type valueType){
		return "Expeced expression of type \"num\" but found an expression of type \""+valueType+"\"";
	}

	private String errorBool_calc(Token.eToken token, Variable.Type type){
		return "Invalid argument of type \""+type+"\" for calculation operation \"" + Helper.tokenStr(token) + "\""
		+ "\n\tExpected an expression of type num.";
	}

	private String errorBool_VAR(String varName, Variable.Type varType){
		return "Expeced an expression of type \"bool\" but found variable \"" + varName + "\" of type \"" + varType + "\"";
	}

	private String errorBool_eq(Variable.Type t1, Variable.Type t2){ 
		return "Cannot compare a variable of type \""+t2+"\" with a variable of time \""+t1+"\" "
			+	"\n\tThe comparison operation \"eq\" requires that both variables are of the same type";
	}

	private String errorBool_or_and_not(Token.eToken token, Variable.Type type){
		return "Invalid argument of type \""+type+"\" for boolean operation \"" + Helper.tokenStr(token) + "\""
		+ "\n\tExpected an expression of type bool.";
	}

	private String errBool_greater_less_than(Token.eToken token, Variable.Type type){
		return "Invalid argument of type \""+type+"\" for comparison operation \"" + Helper.tokenStr(token) + "\""
		+ "\n\tExpected a variable of type num.";
	}

	private String errorBool_for(String varName, Variable.Type type){
		return "Invalid variable \""+varName+"\" of type \""+type+"\" for conditional loop statement \"for\""
		+ "\n\tExpected a variable of type num.";
	}

	private String errorBool_while(Variable.Type type){
		return "Invalid argument of type \""+type+"\" for conditional loop statement \"while\""
		+ "\n\tExpected an expression of type bool.";
	}

	private String errorBool_if(Variable.Type type){
		return "Invalid argument of type \""+type+"\" for conditional branch statement \"if\""
		+ "\n\tExpected an expression of type bool.";
	}

	private SyntaxNode getFirstLeaf(SyntaxNode node){
		while(!node.isLeaf()) node = ((CompositeSyntaxNode)node).children.get(0);
		return node;
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