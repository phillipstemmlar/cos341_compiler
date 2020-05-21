import java.util.*;

public class Translator{

	HashMap<Integer, SyntaxNode> symbolTree = null;
	HashMap<Integer, Scope> scopeTable = null;
	int lineNumber = 0;
	int varCount = 0;

	List<BasicNode> basicNodes = null;
	HashMap<String, BasicNode_LABEL> labels = null;

	public Translator(){}

	public void execute(HashMap<Integer, SyntaxNode> symTree, HashMap<Integer, Scope> scopeTbl, String BASIC_file, String abstract_file){
		if(symTree != null && scopeTbl != null && BASIC_file.length() > 0){
			symbolTree = symTree;
			scopeTable = scopeTbl;

			System.out.println(symbolTree.get(0).treeString());
			deconstructTree();
			System.out.println(symbolTree.get(0).treeString());

			setProcLineNumbers();

			translateAbstract();
			String basicAbstractString = "";
			for(BasicNode node : basicNodes){
				basicAbstractString += node.str();
			}
			Helper.writeToFile(abstract_file,basicAbstractString);

			String output_code = translate();
			Helper.writeToFile(BASIC_file, output_code);
		}
	}

	private String translate(){
		lineNumber = 0;
		String basicString = "";
		BasicNode prevLabel = null;
		for(BasicNode node : basicNodes){
			if(node.label && !node.end){
				prevLabel = node;
			}else{
				incLine();
				node.line(lineNumber);
				if(prevLabel != null) prevLabel.line(lineNumber);
				prevLabel = null;
			}
		}
		for(BasicNode node : basicNodes){
			if(node.goto_){
				String label = ((BasicNode_GOTO)node).JumpTo();
				Integer line = labels.get(label).line();
				((BasicNode_GOTO)node).JumpTo(line + "");
			}else if(node.gosub){
				String label = ((BasicNode_GOSUB)node).JumpTo();
				Integer line = labels.get(label).line();
				((BasicNode_GOSUB)node).JumpTo(line + "");
			}
		}
		for(BasicNode node : basicNodes){
			if(node.end && node.label || !node.label){
				basicString += node.basic();
			}
		}

		return basicString;
	}

	private void setProcLineNumbers(){
		lineNumber = 0;
		Procedure.foundFirst = false;
		labels = new HashMap<>();
		setProcLineNumbers(symbolTree.get(0), scopeTable.get(0), true);
	}

	private void setProcLineNumbers(SyntaxNode node, Scope scope, Boolean first){
		if(node == null) return;
		else if(node.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(node.index);
			if(scope == null) return;
		}
		else if(node.token == Token.eToken.PROC){
			LeafSyntaxNode proc_name = (LeafSyntaxNode)(((CompositeSyntaxNode)node).children.get(1));
			Procedure proc = scope.getProcedureByName(proc_name.val());

			if(Procedure.foundFirst == false)	proc.first = true;
			if(proc.first) incLine();
			Procedure.foundFirst = true;

			labels.put(proc.name(), new BasicNode_LABEL(proc.name()));

			incLine();
			proc.basic_line = lineNumber;
			setProcLineNumbers(((CompositeSyntaxNode)node).children.get(2), scope, false);
			return;
		}else if(node.token == Token.eToken.INSTR){
			SyntaxNode INS = ((CompositeSyntaxNode)node).children.get(0);
			if(INS.token != Token.eToken.DECL){
				incLine();
			}
			return;
		}

		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				setProcLineNumbers(child,scope, false);
			}
		}
	}

	private void translateAbstract(){
		basicNodes = new ArrayList<>();
		labels.put("H",new BasicNode_EOF());
		translateAbstract(symbolTree.get(0), scopeTable.get(0), true);
		basicNodes.add(labels.get("H"));
	}

	private void translateAbstract(SyntaxNode sNode, Scope scope, Boolean first){
		if(sNode.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(sNode.index);
			if(scope == null) return;
		}
		if(sNode.token == Token.eToken.INSTR){
			SyntaxNode INS = ((CompositeSyntaxNode)sNode).children.get(0);
			if(INS.token == Token.eToken.CALL){
				LeafSyntaxNode proc_name = (LeafSyntaxNode)(((CompositeSyntaxNode)INS).children.get(0));
				basicNodes.add(new BasicNode_GOSUB(proc_name.val()));
			}
			if(INS.token == Token.eToken.tok_halt){
				basicNodes.add(new BasicNode_END());
			}
			else if(INS.token == Token.eToken.COND_LOOP){
				SyntaxNode FIRST = ((CompositeSyntaxNode)INS).children.get(0);
				if(FIRST.token == Token.eToken.tok_for){
					//FOR-loop

					// V1 = 5,


					int id = BasicNode_LABEL.countINC();
					String COND = "FOR_COND" + id;
					String FOR = "FOR" + id;

					// V0 = 0
					String init = nodeToString( ((CompositeSyntaxNode)INS).children.get(1), scope);
					init += " = " + nodeToString( ((CompositeSyntaxNode)INS).children.get(2), scope);
					basicNodes.add(new BasicNode(init));

					// GOTO FOR_COND
					basicNodes.add(new BasicNode_GOTO(COND));				//JUMP to CONDITION

					// FOR:
					labels.put(FOR, new BasicNode_LABEL(FOR));			//WHILE-BRANCH
					basicNodes.add(labels.get(FOR));

					// PRINT V0
					translateAbstract(((CompositeSyntaxNode)INS).children.get(10), scope, false);

					// V0 = V0 + 1
					String increment = nodeToString( ((CompositeSyntaxNode)INS).children.get(6), scope);
					increment += " = " + nodeToString( ((CompositeSyntaxNode)INS).children.get(8), scope);
					increment += " " + nodeToString( ((CompositeSyntaxNode)INS).children.get(7), scope) + " ";
					increment += nodeToString( ((CompositeSyntaxNode)INS).children.get(9), scope);
					basicNodes.add(new BasicNode(increment));

					// FOR_COND:
					labels.put(COND, new BasicNode_LABEL(COND));			//CONDITION-BRANCH
					basicNodes.add(labels.get(COND));

					// V2 = V0 < V1
					String varName = "V" + varCount++;
					String contition = varName + " = ";
					contition += nodeToString( ((CompositeSyntaxNode)INS).children.get(3), scope);
					contition += " " + nodeToString( ((CompositeSyntaxNode)INS).children.get(4), scope) + " ";
					contition += nodeToString( ((CompositeSyntaxNode)INS).children.get(5), scope);
					basicNodes.add(new BasicNode(contition));

					// IF V2 THEN GOTO FOR
					basicNodes.add(new BasicNode_IFTHEN(varName, FOR));



				}else{
					//WHILE-loop
					int id = BasicNode_LABEL.countINC();
					String COND = "WHILE_COND" + id;
					String WHILE = "WHILE" + id;
					// GOTO while_cond
					basicNodes.add(new BasicNode_GOTO(COND));				//JUMP to CONDITION

					// while:	
					labels.put(WHILE, new BasicNode_LABEL(WHILE));			//WHILE-BRANCH
					basicNodes.add(labels.get(WHILE));
					// 	V0 = V0 + 1
					// 	PRINT V0
					translateAbstract(((CompositeSyntaxNode)INS).children.get(2), scope, false);
					
					// while_cond:
					labels.put(COND, new BasicNode_LABEL(COND));			//CONDITION-BRANCH
					basicNodes.add(labels.get(COND));

					// 	V2 = V0 < V1
					translateAbstract(((CompositeSyntaxNode)INS).children.get(3), scope, false);
					String Bool = nodeToString(((CompositeSyntaxNode)INS).children.get(1), scope);
					// 	IF V2 THEN GOTO while
					basicNodes.add(new BasicNode_IFTHEN(Bool, WHILE));

				}
			}
			else if(INS.token == Token.eToken.COND_BRANCH){
				int id = BasicNode_LABEL.countINC();
				String Bool = nodeToString(((CompositeSyntaxNode)INS).children.get(1), scope);
				String THEN = "THEN"+id;  String EndIF = "ENDIF"+id;
				basicNodes.add(new BasicNode_IFTHEN(Bool, THEN));
						
				if(((CompositeSyntaxNode)INS).children.size() > 4){				//ELSE-BRANCH
					translateAbstract(((CompositeSyntaxNode)INS).children.get(5), scope, false);
				}
				basicNodes.add(new BasicNode_GOTO(EndIF));				//GOTO EndIF

				labels.put(THEN, new BasicNode_LABEL(THEN));			//THEN-BRANCH
				basicNodes.add(labels.get(THEN));
				translateAbstract(((CompositeSyntaxNode)INS).children.get(3), scope, false);

				labels.put(EndIF, new BasicNode_LABEL(EndIF));
				basicNodes.add(labels.get(EndIF));
			}
			else if(INS.token != Token.eToken.DECL){
				String instr = nodeToString(INS, scope);
				basicNodes.add(new BasicNode(instr));
			}
		}
		else if(sNode.token == Token.eToken.PROC){
			LeafSyntaxNode proc_name = (LeafSyntaxNode)(((CompositeSyntaxNode)sNode).children.get(1));
			Procedure proc = scope.getProcedureByName(proc_name.val());
			basicNodes.add(labels.get(proc.name()));
			translateAbstract(((CompositeSyntaxNode)sNode).children.get(2),scope, false);
		}
		else if(sNode.token == Token.eToken.CODE){
			Boolean isLast = true, nextIsProc = false;
			for(SyntaxNode node : ((CompositeSyntaxNode)sNode).children){
				if(node.token == Token.eToken.CODE) isLast = false;
				else if(node.token == Token.eToken.PROC) nextIsProc = true;
			}

			Boolean canParent = true;
			SyntaxNode parent = sNode;
			while(canParent && parent != null && parent.token != Token.eToken.PROG){
					if(isLoop(parent.token) ||  isBranch(parent.token)) canParent = false;
					parent = parent.parent;
			}

			// Helper.success("canP: " + canParent + "\t index: " + sNode.index);

			if(nextIsProc){
				if(isLast && canParent){
					if(scope.lvl() > 0) basicNodes.add(new BasicNode("RETURN"));
					else basicNodes.add(new BasicNode_END());
				}
				if(!sNode.isLeaf()){
					for(SyntaxNode child : ((CompositeSyntaxNode)sNode).children){
						translateAbstract(child,scope, false);
					}
				}
			}else{
				if(!sNode.isLeaf()){
					for(SyntaxNode child : ((CompositeSyntaxNode)sNode).children){
						translateAbstract(child,scope, false);
					}
				}
				if(isLast && canParent){
					if(scope.lvl() > 0) basicNodes.add(new BasicNode("RETURN"));
					else basicNodes.add(new BasicNode_END());
				}
			}

			
			return;
		}
		else if(!sNode.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)sNode).children){
				translateAbstract(child,scope, false);
			}
		}
	}

	private String nodeToString(SyntaxNode node, Scope scope){
		if(isPrimitive(node.token)){
			return ((LeafSyntaxNode)node).val();
		}
		else if(isIO(node.token) || isOP(node.token) || isPrimitiveBOOL(node.token)){
			return TOKENtoBASIC(node.token);
		}
		else if(node.token == Token.eToken.VAR){
			return nodeToString(((CompositeSyntaxNode)node).children.get(0),scope) + typeSuffix(node.type);
		}
		else if(node.token == Token.eToken.ASSIGN){			//ASSIGN
			String var = nodeToString(((CompositeSyntaxNode)node).children.get(0),scope);
			String value = nodeToString(((CompositeSyntaxNode)node).children.get(1),scope);
			return var + " = " + value;
		}
		else if(node.token == Token.eToken.IO){			//OUTPUT	
			String io = nodeToString(((CompositeSyntaxNode)node).children.get(0),scope);
			String var = nodeToString(((CompositeSyntaxNode)node).children.get(1),scope);
			return io + " " + var;
		}
		else if(node.token == Token.eToken.NUMEXPR){
			return nodeToString(((CompositeSyntaxNode)node).children.get(0),scope);
		}
		else if(node.token == Token.eToken.CALC){
			String op = nodeToString(((CompositeSyntaxNode)node).children.get(0),scope);
			String n1 = nodeToString(((CompositeSyntaxNode)node).children.get(1),scope);
			String n2 = nodeToString(((CompositeSyntaxNode)node).children.get(2),scope);
			return n1 + " " + op + " " + n2;
		}
		else if(node.token == Token.eToken.BOOL){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);
			SyntaxNode SECOND = (((CompositeSyntaxNode)node).children.size() >= 2)?((CompositeSyntaxNode)node).children.get(1) : null;

			if(isEq(FIRST.token)){
				String op = nodeToString(FIRST,scope);
				String n1 = nodeToString(SECOND,scope);
				String n2 = nodeToString(((CompositeSyntaxNode)node).children.get(2),scope);
				return n1 + " " + op + " " + n2;
			}
			else if(SECOND != null && isOP(SECOND.token)){
				String n1 = nodeToString(FIRST,scope);
				String op = nodeToString(SECOND,scope);
				String n2 = nodeToString(((CompositeSyntaxNode)node).children.get(2),scope);
				return n1 + " " + op + " " + n2;
			}else if(isPrimitiveBOOL(FIRST.token) || FIRST.token == Token.eToken.VAR){
				return nodeToString(FIRST,scope);
			}
			else if(isNot(FIRST.token)){
				String not = nodeToString(FIRST,scope);
				String bool = nodeToString(SECOND,scope);
				return not + " " + bool;
			}

		}
		return "##--basicString err("+node.token+")--##";
	}


	private void deconstructTree(){
		varCount = Variable.count();
		deconstructTree(symbolTree.get(0), scopeTable.get(0), true);
	}

	private void deconstructTree(SyntaxNode node, Scope scope, Boolean first){
		if(node == null) return;
		else if(node.token == Token.eToken.PROG){
			scope = first? scope : scope.getScopeByIndex(node.index);
			if(scope == null) return;
		}
		else if(node.token == Token.eToken.CALC){
			//TEST if valid case
			SyntaxNode OP = ((CompositeSyntaxNode)node).children.get(0);
			SyntaxNode N1 = ((CompositeSyntaxNode)node).children.get(1);
			SyntaxNode N2 = ((CompositeSyntaxNode)node).children.get(2);

			SyntaxNode n1 = N1.isLeaf() ? null : ((CompositeSyntaxNode)N1).children.get(0);
			SyntaxNode n2 = N2.isLeaf() ? null : ((CompositeSyntaxNode)N2).children.get(0);

			if(n1 != null && n1.token == Token.eToken.CALC){
				SyntaxNode parCODE = n1.parent;
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
				}
				if(parCODE != null){
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					CompositeSyntaxNode newNUM_EXPR = new CompositeSyntaxNode(Token.eToken.NUMEXPR, "");

					CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					varCount++;
					new_var.val("V" + varCount);
					new_var2.val("V" + varCount);

					newNUM_EXPR.children = ((CompositeSyntaxNode)N1).children;
					((CompositeSyntaxNode)N1).resetChildren();

					//SETUP INSTR
					newASSIGN.addChild(newNUM_EXPR);
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.num;
					newASSIGN.addChild(newVAR);
					newINSTR.addChild(newASSIGN);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					newVAR2.addChild(new_var2);
					newVAR2.type = Variable.Type.num;
					((CompositeSyntaxNode)node).resetChildren();
					((CompositeSyntaxNode)node).addChild(N2);
					((CompositeSyntaxNode)node).addChild(new_var2);
					((CompositeSyntaxNode)node).addChild(OP);

				}
			}

			if(n2 != null && n2.token == Token.eToken.CALC){
				SyntaxNode parCODE = n2.parent;
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
				}
				if(parCODE != null){
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					CompositeSyntaxNode newNUM_EXPR = new CompositeSyntaxNode(Token.eToken.NUMEXPR, "");

					CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					varCount++;
					new_var.val("V" + varCount);
					new_var2.val("V" + varCount);

					newNUM_EXPR.children = ((CompositeSyntaxNode)N2).children;
					((CompositeSyntaxNode)N2).resetChildren();

					//SETUP INSTR
					newASSIGN.addChild(newNUM_EXPR);
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.num;
					newASSIGN.addChild(newVAR);
					newINSTR.addChild(newASSIGN);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					newVAR2.addChild(new_var2);
					newVAR2.type = Variable.Type.num;
					((CompositeSyntaxNode)node).resetChildren();
					((CompositeSyntaxNode)node).addChild(new_var2);
					((CompositeSyntaxNode)node).addChild(N1);
					((CompositeSyntaxNode)node).addChild(OP);

				}
			}

			deconstructTree(n1,scope, false);
			deconstructTree(n2,scope, false);
			return;
		}
		else if(node.token == Token.eToken.BOOL){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);
			SyntaxNode SECOND = (((CompositeSyntaxNode)node).children.size() >= 2)?((CompositeSyntaxNode)node).children.get(1) : null;
			SyntaxNode THIRD = (((CompositeSyntaxNode)node).children.size() >= 3)?((CompositeSyntaxNode)node).children.get(2) : null;

			if(isAndOr(FIRST.token)){
				Token.eToken opToken = FIRST.token;
	
				// deconstructTree(THIRD,scope, false);

				Integer temp = varCount++;
				/*
				cond = or(b1,b2);

				temp = not b1;
				if(temp) then{ temp = not b2 }
				cond = not temp
				*/

				/*
				cond = and(b1,b2);
				
				temp = b1;
				if(temp) then{ temp = b2 };
				cond = temp;
				*/
				//if(temp) then{ temp = not b2 }
				SyntaxNode parCODE = FIRST.parent;
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
				}
				if(parCODE != null){
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					//IF - BOOL - THEN - CODE
					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newCOND_BRANCH = new CompositeSyntaxNode(Token.eToken.COND_BRANCH, "");

					LeafSyntaxNode new_if = new LeafSyntaxNode(Token.eToken.tok_if, "");
					CompositeSyntaxNode newBOOL = new CompositeSyntaxNode(Token.eToken.BOOL, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					LeafSyntaxNode new_then = new LeafSyntaxNode(Token.eToken.tok_then, "");
					CompositeSyntaxNode newCODE_i = new CompositeSyntaxNode(Token.eToken.BOOL, "");
					new_var.val("V" + temp);
					new_var.type = Variable.Type.bool;
					
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.bool;
					newBOOL.addChild(newVAR);

						//temp = not b2
						CompositeSyntaxNode newINSTR_i = new CompositeSyntaxNode(Token.eToken.INSTR, "");
						CompositeSyntaxNode newASSIGN_i = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
						CompositeSyntaxNode newVAR_i = new CompositeSyntaxNode(Token.eToken.VAR, "");
						LeafSyntaxNode new_var_i = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
						CompositeSyntaxNode newBOOL_i = new CompositeSyntaxNode(Token.eToken.BOOL, "");

						newBOOL_i.children = ((CompositeSyntaxNode)THIRD).children;
						((CompositeSyntaxNode)THIRD).resetChildren();

						new_var_i.val("V" + temp);
						new_var_i.type = Variable.Type.bool;

						if(opToken == Token.eToken.tok_or){
							CompositeSyntaxNode newNOT = new CompositeSyntaxNode(Token.eToken.BOOL, "");
							LeafSyntaxNode new_not = new LeafSyntaxNode(Token.eToken.tok_not, "");
							newNOT.addChild(newBOOL_i);
							newNOT.addChild(new_not);
							newBOOL_i = newNOT;
						}

						//SETUP INSTR
						newASSIGN_i.addChild(newBOOL_i);
						newVAR_i.addChild(new_var_i);
						newVAR_i.type = Variable.Type.bool;
						newASSIGN_i.addChild(newVAR_i);
						newINSTR_i.addChild(newASSIGN_i);

						newCODE_i.addChild(newINSTR_i);


					//SETUP INSTR
					newCOND_BRANCH.addChild(newCODE_i);
					newCOND_BRANCH.addChild(new_then);
					newCOND_BRANCH.addChild(newBOOL);
					newCOND_BRANCH.addChild(new_if);
					newINSTR.addChild(newCOND_BRANCH);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					deconstructTree(newBOOL_i,scope, false);
				}
				//	temp = not b1
				parCODE = FIRST.parent;
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
				}
				if(parCODE != null){
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					CompositeSyntaxNode newBOOL = new CompositeSyntaxNode(Token.eToken.BOOL, "");

					newBOOL.children = ((CompositeSyntaxNode)SECOND).children;
					((CompositeSyntaxNode)SECOND).resetChildren();

					new_var.val("V" + temp);

					if(opToken == Token.eToken.tok_or){
						CompositeSyntaxNode newNOT = new CompositeSyntaxNode(Token.eToken.BOOL, "");
						LeafSyntaxNode new_not = new LeafSyntaxNode(Token.eToken.tok_not, "");
						newNOT.addChild(newBOOL);
						newNOT.addChild(new_not);
						newBOOL = newNOT;
					}

					//SETUP INSTR
					newASSIGN.addChild(newBOOL);
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.bool;
					newASSIGN.addChild(newVAR);
					newINSTR.addChild(newASSIGN);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					new_var2.val("V" + temp);

					newVAR2.addChild(new_var2);
					newVAR2.type = Variable.Type.bool;
					((CompositeSyntaxNode)SECOND).addChild(newVAR2);
				
					deconstructTree(newBOOL,scope, false);
				}

				//	cond = not temp
				CompositeSyntaxNode nodeParent = (CompositeSyntaxNode)node;
				if(nodeParent != null){
					nodeParent.resetChildren();

					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					new_var.val("V" + temp);

					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.bool;

					if(opToken == Token.eToken.tok_or){
						CompositeSyntaxNode newBOOL = new CompositeSyntaxNode(Token.eToken.BOOL, "");
						LeafSyntaxNode new_not = new LeafSyntaxNode(Token.eToken.tok_not, "");
						newBOOL.addChild(newVAR);
						nodeParent.addChild(newBOOL);
						nodeParent.addChild(new_not);
					}else{
						nodeParent.addChild(newVAR);
					}

				}
			} else if(isNot(FIRST.token)){
				SyntaxNode parCODE = FIRST.parent;
				while(parCODE != null && parCODE.token != Token.eToken.CODE){
					parCODE = parCODE.parent; 
				}
				if(parCODE != null){
					CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
					newCODE.children = ((CompositeSyntaxNode)parCODE).children;
					((CompositeSyntaxNode)parCODE).resetChildren();

					CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
					CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
					CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					CompositeSyntaxNode newBOOL = new CompositeSyntaxNode(Token.eToken.BOOL, "");

					CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
					LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
					varCount++;
					new_var.val("V" + varCount);
					new_var2.val("V" + varCount);

					newBOOL.children = ((CompositeSyntaxNode)SECOND).children;
					((CompositeSyntaxNode)SECOND).resetChildren();

					//SETUP INSTR
					newASSIGN.addChild(newBOOL);
					newVAR.addChild(new_var);
					newVAR.type = Variable.Type.bool;
					newASSIGN.addChild(newVAR);
					newINSTR.addChild(newASSIGN);

					((CompositeSyntaxNode)parCODE).addChild(newCODE);
					((CompositeSyntaxNode)parCODE).addChild(newINSTR);

					newVAR2.addChild(new_var2);
					newVAR2.type = Variable.Type.bool;

					((CompositeSyntaxNode)SECOND).addChild(newVAR2);

				}
			}
		}
		else if(node.token == Token.eToken.COND_BRANCH){
			SyntaxNode BOOL = ((CompositeSyntaxNode)node).children.get(1);
			SyntaxNode FIRST = ((CompositeSyntaxNode)BOOL).children.get(0);

			SyntaxNode parCODE = node.parent;
			while(parCODE != null && parCODE.token != Token.eToken.CODE){
				parCODE = parCODE.parent; 
			}
			if(parCODE != null){
				CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
				newCODE.children = ((CompositeSyntaxNode)parCODE).children;
				((CompositeSyntaxNode)parCODE).resetChildren();

				CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
				CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
				CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
				LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
				CompositeSyntaxNode newBOOL = new CompositeSyntaxNode(Token.eToken.BOOL, "");

				CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
				LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
				varCount++;
				new_var.val("V" + varCount);
				new_var2.val("V" + varCount);

				newBOOL.children = ((CompositeSyntaxNode)BOOL).children;
				((CompositeSyntaxNode)BOOL).resetChildren();

				//SETUP INSTR
				newASSIGN.addChild(newBOOL);
				newVAR.addChild(new_var);
				newVAR.type = Variable.Type.bool;
				newASSIGN.addChild(newVAR);
				newINSTR.addChild(newASSIGN);

				((CompositeSyntaxNode)parCODE).addChild(newCODE);
				((CompositeSyntaxNode)parCODE).addChild(newINSTR);

				newVAR2.addChild(new_var2);
				newVAR2.type = Variable.Type.bool;
				((CompositeSyntaxNode)BOOL).addChild(newVAR2);
				deconstructTree(newBOOL,scope, false);
			}
			
		}
		else if(node.token == Token.eToken.COND_LOOP){
			SyntaxNode FIRST = ((CompositeSyntaxNode)node).children.get(0);
			if(FIRST.token == Token.eToken.tok_for){
				//FOR-loop
			}else{
				//WHILE-loop
				SyntaxNode BOOL = ((CompositeSyntaxNode)node).children.get(1);
	
				CompositeSyntaxNode newCODE = new CompositeSyntaxNode(Token.eToken.CODE, "");
				CompositeSyntaxNode newINSTR = new CompositeSyntaxNode(Token.eToken.INSTR, "");
				CompositeSyntaxNode newASSIGN = new CompositeSyntaxNode(Token.eToken.ASSIGN, "");
				CompositeSyntaxNode newVAR = new CompositeSyntaxNode(Token.eToken.VAR, "");
				LeafSyntaxNode new_var = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
				CompositeSyntaxNode newBOOL = new CompositeSyntaxNode(Token.eToken.BOOL, "");

				CompositeSyntaxNode newVAR2 = new CompositeSyntaxNode(Token.eToken.VAR, "");
				LeafSyntaxNode new_var2 = new LeafSyntaxNode(Token.eToken.tok_user_defined_identifier, "");
				varCount++;
				new_var.val("V" + varCount);
				new_var2.val("V" + varCount);

				newBOOL.children = ((CompositeSyntaxNode)BOOL).children;
				((CompositeSyntaxNode)BOOL).resetChildren();

				//SETUP INSTR
				newASSIGN.addChild(newBOOL);
				newVAR.addChild(new_var);
				newVAR.type = Variable.Type.bool;
				newASSIGN.addChild(newVAR);
				newINSTR.addChild(newASSIGN);
				newCODE.addChild(newINSTR);
				
				((CompositeSyntaxNode)node).appendChild(newCODE);

				newVAR2.addChild(new_var2);
				newVAR2.type = Variable.Type.bool;
				((CompositeSyntaxNode)BOOL).addChild(newVAR2);
				deconstructTree(newBOOL,scope, false);
			}
		}		
		if(!node.isLeaf()){
			for(SyntaxNode child : ((CompositeSyntaxNode)node).children){
				deconstructTree(child,scope,false);
			}
		}

	}


	private void incLine(){
		lineNumber += 10;
	}

	//================TOKEN CHECKERS============

	private Boolean isIO(Token.eToken token){
		return token == Token.eToken.tok_input || token == Token.eToken.tok_output;
	}

	private Boolean isOP(Token.eToken token){
		return isNot(token) || isEq(token) || token == Token.eToken.tok_add || token == Token.eToken.tok_sub || token == Token.eToken.tok_mult
		|| token == Token.eToken.tok_greater_than || token == Token.eToken.tok_less_than ;
	}

	private Boolean isAndOr(Token.eToken token){
		return token == Token.eToken.tok_and || token == Token.eToken.tok_or;
	}

	private Boolean isNot(Token.eToken token){
		return token == Token.eToken.tok_not;
	}

	private Boolean isEq(Token.eToken token){
		return token == Token.eToken.tok_eq;
	}

	private Boolean isLoop(Token.eToken token){
		return token == Token.eToken.COND_LOOP;
	}

	private Boolean isBranch(Token.eToken token){
		return token == Token.eToken.COND_BRANCH;
	}

	private Boolean isPrimitive(Token.eToken token){
		return token == Token.eToken.tok_string_literal || token == Token.eToken.tok_integer_literal 
		|| token == Token.eToken.tok_user_defined_identifier;
	}

	private Boolean isPrimitiveBOOL(Token.eToken token){
		return token == Token.eToken.tok_T || token == Token.eToken.tok_F;
	}

	//================TOKEN TRANSLATION============

	private String TOKENtoBASIC(Token.eToken token){
		if(token == Token.eToken.tok_input) return "INPUT";
		if(token == Token.eToken.tok_output) return "PRINT";
		if(token == Token.eToken.tok_add) return "+";
		if(token == Token.eToken.tok_sub) return "-";
		if(token == Token.eToken.tok_mult) return "*";
		if(token == Token.eToken.tok_T) return "1";
		if(token == Token.eToken.tok_F) return "0";
		if(token == Token.eToken.tok_eq) return "=";
		if(token == Token.eToken.tok_less_than) return "<";
		if(token == Token.eToken.tok_greater_than) return ">";
		if(token == Token.eToken.tok_not) return "NOT";
		// if(token == Token.eToken.tok_) return "PRINT";
		// if(token == Token.eToken.tok_) return "PRINT";
		return "##-TOKENtoBASIC-##";
	}

	private String typeSuffix(Variable.Type type){
		if(type == Variable.Type.num) return "";
		if(type == Variable.Type.string) return "$";
		if(type == Variable.Type.bool) return "";
		return "##-typeSuffix-##";
	}

}

/*
num,string,bool,procedure,undefined,none,notype

tok_and,
tok_or,
tok_not,
tok_add,
tok_sub,
tok_mult,
tok_if,
tok_then,
tok_else,
tok_while,
tok_for,
tok_eq,
tok_input,
tok_output,
tok_halt,
tok_num,
tok_bool,
tok_string,
tok_proc,
tok_T,
tok_F,
tok_user_defined_identifier,
tok_string_literal,
tok_integer_literal,
tok_less_than,
tok_greater_than,
tok_space,
tok_tab,
tok_newline,
tok_open_parenth,
tok_close_parenth,
tok_open_brace,
tok_close_brace,
tok_assignment,
tok_comma,
tok_semi_colon,
tok_none,
//NON-TERMINALS
END,			// $
PROG,
PROC_DEFS,
PROC,
CODE,
INSTR,
IO,
CALL,
DECL,
TYPE,
NAME,
VAR,
ASSIGN,
NUMEXPR,
CALC,
COND_BRANCH,
BOOL,
COND_LOOP,
//Left-Factorization NON-TERMINALS
PROC_DEFS_PART,
PROC_DEFS_PART2,
CODE_PART,
DECL_PART,
VALUE_PART,
ELSE_PART,
BOOL2
*/