import java.util.ArrayList;


public class SymbolTable {
	private int staticSymbolCount = 0;
	private int fieldSymbolCount = 0, argSymbolCount = 0, varSymbolCount = 0;
	private ArrayList<String> name;
	private ArrayList<String> type;
	private ArrayList<CompilationEngine.Kind> kind;
	private ArrayList<Integer> number;
	private String symbolTableName;

	public String getSymbolTableName() {
		return symbolTableName;
	}
	public boolean hasVariableName(String theName)
	{
		for(String i : name)
		{
			if(i.equals(theName))
				return true;
		}
		return false;
	}
	public SymbolTable(String newClassName) {
		symbolTableName = newClassName;
		name = new ArrayList<String>();
		type = new ArrayList<String>();
		kind = new ArrayList<CompilationEngine.Kind>();
		number = new ArrayList<Integer>();
	}

	public void define ( String name, String type, CompilationEngine.Kind kind) {
		this.name.add(name);
		this.type.add(type);
		this.kind.add(kind);
		checkKindAndAddToNumberTable(kind);
	}

	private void checkKindAndAddToNumberTable(CompilationEngine.Kind kind2) {
		switch(kind2){
		case ARG:
			number.add(argSymbolCount);
			argSymbolCount++;
			break;
		case STATIC:
			number.add(staticSymbolCount);
			staticSymbolCount++;
			break;
		case FIELD:
			number.add(fieldSymbolCount);
			fieldSymbolCount++;
			break;
		case VAR:
			number.add(varSymbolCount);
			varSymbolCount++;
			break;
		}
	}

	public String typeOf(String name2) {
		int typeIndex = name.indexOf(name2);
		return type.get(typeIndex);
	}

	public int indexOf(String name3) {
		int typeIndex = name.indexOf(name3);
		return number.get(typeIndex);
	}

	public CompilationEngine.Kind kindOf(String name4) {
		int kindIndex = name.indexOf(name4);
		return kind.get(kindIndex);
	}

	public void output() {
		System.out.println(symbolTableName);
		for(int i = 0; i < name.size(); i++)
		{
			System.out.print(name.get(i) + " ");
			System.out.print(type.get(i) + " ");
			System.out.print(kind.get(i) + " ");
			System.out.println(number.get(i) + " ");
		}
	}
	public int getTotalCount() {
		return fieldSymbolCount+varSymbolCount+staticSymbolCount;
	}
	public int getFieldCount() {
		return fieldSymbolCount;
	}

}