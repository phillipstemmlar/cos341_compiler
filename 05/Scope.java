import java.util.*;

public class Scope{

	protected static int count = 0;
	private int ID;
	private int scopeLevel;

	public HashMap<Integer, Variable> variableTable = null;
	public HashMap<String, Variable> variableNameTable = null;
	public HashMap<Integer, Procedure> procedureTable = null;
	public HashMap<String, Procedure> procedureNameTable = null;

	public HashMap<Integer, Scope> sub_Scopes = null;
	public HashMap<Integer, Scope> sub_Scopes_index = null;

	public HashMap<Integer, Scope> semi_Scopes_index = null;

	public Scope parent = null;
	public Procedure declProc = null; 
	public SyntaxNode scopeRoot = null;

	public List<Integer> DECL_Lines;
	public List<Integer> VAR_Lines;
	public List<Integer> PROC_Lines;
	public List<Integer> CALL_Lines;

	public Boolean isSemi = false;
	public Boolean is_THEN_BRANCH = true;

	public Scope(int scopelvl, SyntaxNode root){
		ID = count++;
		scopeLevel = scopelvl;
		variableTable = new HashMap<>();
		variableNameTable = new HashMap<>();
		procedureTable = new HashMap<>();
		procedureNameTable = new HashMap<>();

		sub_Scopes = new HashMap<>();
		sub_Scopes_index = new HashMap<>();
		semi_Scopes_index = new HashMap<>();
		scopeRoot = root;

		DECL_Lines = new ArrayList<>();
		VAR_Lines = new ArrayList<>();
		PROC_Lines = new ArrayList<>();
		CALL_Lines = new ArrayList<>();
	}

	//==============variableTable table======================

	public void addVariable(Variable var){
		if(var != null){
			if(variableTable != null) variableTable.put(var.ID, var);
			if(variableNameTable != null) variableNameTable.put(var.ogname, var);
		}
	}

	public Variable getVariableByID(int ID){
		if(variableTable != null) return variableTable.get(ID);
		return  null;
	}

	public Variable getVariableByName(String name){
		return getVariableByID(Variable.IDfromName(name));
	}

	public Variable getVariableByOriginalName(String ogname){
		if(variableNameTable != null) 	return variableNameTable.get(ogname);
		return  null;
	}

	public Variable findVariableByName(String name){
		Integer id;
		try{
			id = Variable.IDfromName(name);
		}catch(Exception e){
			return null;
		}
		Scope cur = this;
		Variable var = null;
		while(cur != null){
			var = cur.getVariableByID(id);
			if(var != null) return var;
			cur = cur.parent;
		}
		return var;
	}

	public Variable findVariable(String ogname){
		Scope cur = this;
		Variable var = null;
		while(cur != null){
			var = cur.getVariableByOriginalName(ogname);
			if(var != null) return var;
			cur = cur.parent;
		}
		return var;
	}

	//==============procedureTable table======================

	public void addProcedure(Procedure proc){
		if(proc != null){
			if(procedureTable != null) procedureTable.put(proc.ID, proc);
			if(procedureNameTable != null) procedureNameTable.put(proc.ogname, proc);
		}
	}

	public Procedure getProcedureByID(int ID){
		if(procedureTable != null) return procedureTable.get(ID);
		return  null;
	}

	public Procedure getProcedureByName(String name){
		return getProcedureByID(Procedure.IDfromName(name));
	}

	public Procedure getProcedureByOriginalName(String ogname){
		if(procedureNameTable != null) return procedureNameTable.get(ogname);
		return  null;
	}

	public Procedure findProcedureByName(String name){
		Integer id;
		try{
			id = Procedure.IDfromName(name);
		}catch(Exception e){
			return null;
		}
		Scope cur = this;
		Procedure proc = null;
		while(cur != null){
			proc = cur.getProcedureByID(id);
			if(proc != null) return proc;
			cur = cur.parent;
		}
		return proc;
	}

	public Procedure findProcedure(String ogname){
		Scope cur = this;
		Procedure proc = null;
		while(cur != null){
			proc = cur.getProcedureByOriginalName(ogname);
			if(proc != null) return proc;
			cur = cur.parent;
		}
		return proc;
	}

	public Procedure checkProcedure(String ogname){
		Scope cur = this;
		Procedure proc = cur.getProcedureByOriginalName(ogname);
		if(proc != null) return proc;

		while(cur != null){
			proc = cur.declProc;
			if(proc != null && Helper.compstr(proc.ogname,ogname)) return proc;
			cur = cur.parent;
		}
		return proc;
	}

	//==============sub scopes======================


	public void addScope(Scope scope){
		if(sub_Scopes != null){
			sub_Scopes.put(scope.ID(), scope);
			if(scope.index() >= 0) sub_Scopes_index.put(scope.index(), scope);
		}
	}

	public Scope getScope(int ID){
		if(sub_Scopes != null){
			return sub_Scopes.get(ID);
		}
		return  null;
	}

	public Scope getScopeByIndex(int index){
		if(sub_Scopes_index != null){
			return sub_Scopes_index.get(index);
		}
		return  null;
	}

	public void addSemiScope(Scope scope){
		if(semi_Scopes_index != null){
			if(scope.index() >= 0) semi_Scopes_index.put(scope.index(), scope);
		}
	}


	public Scope getSemiScopeByIndex(int index){
		if(semi_Scopes_index != null){
			return semi_Scopes_index.get(index);
		}
		return  null;
	}

	//==============print======================

	public Integer ID(){
		return ID;
	}

	public Integer lvl(){
		return scopeLevel;
	}

	public Integer index(){
		if(scopeRoot != null) return scopeRoot.index;
		return -1;
	}

	public void print(){
		System.out.println("Scope[" + ID+"]:");
		System.out.println("\tlevel:" + lvl());
		System.out.println("->\t" + Arrays.toString(sub_Scopes.keySet().toArray()) );
		System.out.println("=>\t" + Arrays.toString(variableNameTable.keySet().toArray()) + " => " + Arrays.toString(variableNameTable.values().toArray()) );
		System.out.println("=>\t" + Arrays.toString(procedureNameTable.keySet().toArray()) + " => " + Arrays.toString(procedureNameTable.values().toArray()) );
	}

}