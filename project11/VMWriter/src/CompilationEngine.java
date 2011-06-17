import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;




public class CompilationEngine {
	public enum Command { ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT }
	public enum Keywords { CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT,
		BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, BRACKET,
		WHILE, RETURN, TRUE, FALSE, NULL, THIS, DNE}
	public enum Kind { STATIC, FIELD, ARG, VAR }
	public enum Segment { CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP}
	private static enum Symbols { OPENING_PARANTHESE, OPENING_BRACKET, NOT_A_SYMBOL,
		PERIOD, CLOSING_PARANTHESE }
	private static enum TokenTypes { KEYWORD, SYMBOL, INTEGER_CONSTANT,
		STRING_CONSTANT, IDENTIFIER, NULL}
	private HashSet<String> symbols, ops;
	private HashMap<String,Keywords> keywords;
	private HashMap<String,Command> unOpTypes, binOpTypes;
	private static String SubRoutineType="";
	private Stack<String> operatorStack, bracketStack;
	private VMWriter VMW;
	private Token tokens;
	private String outputName;
	private int nArgs;
	public SymbolTable methodSymbolTable, classSymbolTable;

	public CompilationEngine(String outputFileName, Token tks)
	{
		operatorStack = new Stack<String>();
		bracketStack = new Stack<String>();
		outputName = outputFileName;
		tokens = tks;
		keywords = new HashMap<String,Keywords>();
		initializeKeywordsMap();
		symbols = new HashSet<String>();
		initializeSymbolsSet();
		ops = new HashSet<String>();
		binOpTypes = new HashMap<String,Command>();
		initializeBinOps();
		unOpTypes = new HashMap<String,Command>();
		initializeUnOps();
		initializeOps();
		try {
			VMW = new VMWriter(outputFileName);
		}catch (FileNotFoundException e)
		{
			System.out.print(e.getMessage());
		}
	}

	private boolean anOp() {
		return ops.contains(tokens.getCurrToken());
	}
	private boolean checkSymbolTablesAndWritePushCommandForArguments(String variable) 
	{
		if(methodSymbolTable.hasVariableName(variable))
		{
			nArgs++;
			Kind theKind = methodSymbolTable.kindOf(variable);
			VMW.writePush(convertKindToSegment(theKind), methodSymbolTable.indexOf(variable));
			return true;
		}
		else if(classSymbolTable.hasVariableName(variable))
		{
			nArgs++;
			Kind theKind = classSymbolTable.kindOf(variable);
			VMW.writePush(convertKindToSegment(theKind), classSymbolTable.indexOf(variable));
			return true;
		}
		return false;
	}

	private void checkSymbolTablesForVariableWritePushCode(String varName) {
		if(methodSymbolTable.hasVariableName(varName))
		{
			int index=methodSymbolTable.indexOf(varName);
			switch(methodSymbolTable.kindOf(varName))
			{
			case ARG:
				VMW.writePush(Segment.ARG, index);
				break;
			case VAR:
				VMW.writePush(Segment.LOCAL, index);
				break;
			}
		}
		else
		{
			int index=classSymbolTable.indexOf(varName);
			switch(classSymbolTable.kindOf(varName))
			{
			case FIELD:
				VMW.writePush(Segment.THIS, index);
				break;
			case STATIC:
				VMW.writePush(Segment.STATIC, index);
				break;
			}
		}
	}
	/**Compile a complete class*/
	public void compileClass()
	{
		System.out.println("Generating: " + outputName);
		if(tokens.getCurrToken().equals("class"))
		{
			getStringAndAdvance("class"); //Grabs 'class' keyword

			classSymbolTable = new SymbolTable(getStringAndAdvance("identifier"));

			getStringAndAdvance("{");

			while(tokens.getCurrToken().equals("static")||tokens.getCurrToken().equals("field"))
			{
				compileClassVarDec();
			}

			while(isASubroutineDec())
			{
				compileSubroutineDec();
			}

			//closing bracket
			getStringAndAdvance("}");
			System.out.println("Done");
			VMW.close();
		}
		else
			return;
	}

	/**Compiles a static declaration or a field declaration*/
	public void compileClassVarDec()
	{
		String kindOfVariable = getStringAndAdvance("keyword");
		Kind kindOfVariableEnum = convertToEnumerated(kindOfVariable);
		String variableType = "";
		if(tokenType() == TokenTypes.KEYWORD)
			variableType = getStringAndAdvance("primitive");
		else if(tokenType() == TokenTypes.IDENTIFIER)
			variableType = getStringAndAdvance("own class");

		String nameOfVariable = getStringAndAdvance("identifier");

		classSymbolTable.define(nameOfVariable, variableType, kindOfVariableEnum);

		while(tokens.getCurrToken().equals(","))
		{
			getStringAndAdvance(",");
			nameOfVariable = getStringAndAdvance("identifier");
			classSymbolTable.define(nameOfVariable, variableType, kindOfVariableEnum);
		}

		getStringAndAdvance(";");

		return;
	}

	/**Compiles a do statement*/
	public void compileDo()
	{
		getStringAndAdvance("do keyword");
		compileSubroutineCall();
		VMW.writePop(Segment.TEMP, 0);
		getStringAndAdvance(";");

	}

	/**Compiles an expression*/
	public void compileExpression()
	{
		compileTerm();
		while(anOp())
		{
			//op term
			String binOperation = getStringAndAdvance("binary operation");
			int pre = operatorStack.size();
			if(binOperation.equals("="))
				compileOperations(pre);
			operatorStack.push(binOperation);
			compileTerm();
			if(!operatorStack.isEmpty())
				compileOneOperation();
		}
	}

	/**Compiles a (possibly empty) comma-separated list of expressions*/
	public void compileExpressionList()
	{
		if(tokens.getCurrToken().equals(")"))
			return;
		else
		{
			int precondition = operatorStack.size();
			compileExpression();
			compileOperations(precondition);
			nArgs++;
			while( tokens.getCurrToken().equals(",") )
			{
				getStringAndAdvance(",");
				compileExpression();
				compileOperations(precondition);
				nArgs++;
			}
		}
	}

	/**Compiles an if statement, possibly with a trailing else clause*/
	public void compileIf()
	{
		int pre = operatorStack.size();
		String labelTrue = "IF_TRUE" + VMWriter.labelCounter;
		String labelFalse = "IF_FALSE" + VMWriter.labelCounter;
		String labelEnd = "IF_END" + VMWriter.labelCounter;
		VMWriter.labelCounter++;
		getStringAndAdvance("if keyword");
		getStringAndAdvance("(");
		compileExpression();
		compileOperations(pre);
		getStringAndAdvance(")");
		VMW.writeIf(labelTrue);
		VMW.writeGoto(labelFalse);
		VMW.writeLabel(labelTrue);
		getStringAndAdvance("{");
		compileStatements();
		getStringAndAdvance("}");
		VMW.writeGoto(labelEnd);
		VMW.writeLabel(labelFalse);
		if(keyWord() == Keywords.ELSE)
		{
			getStringAndAdvance("else");
			getStringAndAdvance("{");
			compileStatements();
			getStringAndAdvance("}");
		}
		VMW.writeLabel(labelEnd);
	}

	/**Compiles a let statement*/
	public void compileLet( )
	{
		boolean anArrayOnLeft = false;
		getStringAndAdvance("let");
		String nameOfVariable = getStringAndAdvance("identifier");

		if(tokens.getCurrToken().equals("["))
		{
			anArrayOnLeft=true;
			getStringAndAdvance("[");
			compileExpression();
			checkSymbolTablesForVariableWritePushCode(nameOfVariable);
			VMW.writeArithmetic(Command.ADD);
			getStringAndAdvance("]");
		}

		getStringAndAdvance("=");
		int pre = operatorStack.size();
		compileExpression();
		compileOperations(pre);
		if(anArrayOnLeft)
		{
			VMW.writePop(Segment.TEMP, 0);
			VMW.writePop(Segment.POINTER, 1);
			VMW.writePush(Segment.TEMP, 0);
			VMW.writePop(Segment.THAT, 0);
		}
		else
		{
			if(methodSymbolTable.hasVariableName(nameOfVariable))
			{
				int index=methodSymbolTable.indexOf(nameOfVariable);
				switch(methodSymbolTable.kindOf(nameOfVariable))
				{
				case ARG:
					VMW.writePop(Segment.ARG, index);
					break;
				case VAR:
					VMW.writePop(Segment.LOCAL, index);
					break;
				}
			}
			else if(classSymbolTable.hasVariableName(nameOfVariable))
			{
				int index=classSymbolTable.indexOf(nameOfVariable);
				switch(classSymbolTable.kindOf(nameOfVariable))
				{
				case FIELD:
					VMW.writePop(Segment.THIS, index);
					break;
				case STATIC:
					VMW.writePop(Segment.STATIC, index);
					break;
				}
				classSymbolTable.getTotalCount();
			}
		}
		getStringAndAdvance(";");
	}

	private void compileOneOperation(){
		if(!operatorStack.isEmpty())
		{
			String curOp = operatorStack.pop();
			if(binOpTypes.containsKey(curOp))
				VMW.writeArithmetic(binOpTypes.get(curOp));
			else
			{
				if(curOp.equals("/"))
					VMW.writeCall("Math.divide", 2);
				else
					VMW.writeCall("Math.multiply", 2);
			}
		}
	}

	private void compileOperations(int precondition) {
		while((operatorStack.size()>precondition))
		{
			String curOp = operatorStack.pop();
			if(binOpTypes.containsKey(curOp))
				VMW.writeArithmetic(binOpTypes.get(curOp));
			else
			{
				if(curOp.equals("/"))
					VMW.writeCall("Math.divide", 2);
				else
					VMW.writeCall("Math.multiply", 2);
			}
		}
	}

	/**Compiles a (possibly empty) parameter list, not including the enclosing "()"*/
	public void compileParameterList()
	{
		if(tokens.getCurrToken().equals(")"))
		{
			return;
		}
		Kind kindOfParameter = Kind.ARG;
		String typeOfParameter = "";
		if(tokenType() == TokenTypes.KEYWORD )
			typeOfParameter = getStringAndAdvance("primitive" );
		else if(tokenType() == TokenTypes.IDENTIFIER)
			typeOfParameter = getStringAndAdvance("class Name");

		String nameOfParameter = getStringAndAdvance("name of parameter");
		methodSymbolTable.define(nameOfParameter, typeOfParameter, kindOfParameter);
		while(tokens.getCurrToken().equals(","))
		{
			getStringAndAdvance(",");
			if(tokenType() == TokenTypes.KEYWORD )
				typeOfParameter = getStringAndAdvance("primitive" );
			else if(tokenType() == TokenTypes.IDENTIFIER)
				typeOfParameter = getStringAndAdvance("class Name");
			nameOfParameter = getStringAndAdvance("name of parameter");
			methodSymbolTable.define(nameOfParameter, typeOfParameter, kindOfParameter);
		}
	}

	/**Compiles a return statement*/
	public void compileReturn()
	{
		getStringAndAdvance("return keyword");
		
		if(tokenType() != TokenTypes.SYMBOL)
		{
			int pre = operatorStack.size();
			compileExpression();
			compileOperations(pre);
		}
		else{
			VMW.writePush(Segment.CONST, 0);
		}
		VMW.writeReturn();
		getStringAndAdvance(";");
	}

	/**Compiles a sequence of statements, not including the enclosing "{}"*/
	public void compileStatements()
	{
		while(keyWord()==Keywords.LET || keyWord()==Keywords.IF || keyWord()==Keywords.WHILE ||
				keyWord()==Keywords.DO || keyWord()==Keywords.RETURN )
		{
			switch(keyWord()){
			case LET:
				compileLet();
				break;
			case WHILE:
				compileWhile();
				break;
			case IF:
				compileIf();
				break;
			case DO:
				compileDo();
				break;
			case RETURN:
				compileReturn();
				break;
			}
		}
	}

	private void compileSubroutineBody() {
		getStringAndAdvance("{");
		
		while(keyWord()==Keywords.VAR)
		{
			compileVarDec();
		}

		VMW.writeFuntion(generateFunctionTag(), methodSymbolTable.getTotalCount());
		if(getSubroutineType() == Keywords.METHOD)
		{
			VMW.writePush(Segment.ARG, 0);
			VMW.writePop(Segment.POINTER, 0);
		}
		switch(getSubroutineType())
		{
		case METHOD:
			break;
		case CONSTRUCTOR:
			VMW.writePush(Segment.CONST, classSymbolTable.getFieldCount());
			VMW.writeCall("Memory.alloc", 1);
			VMW.writePop(Segment.POINTER, 0);
			break;
		case FUNCTION:
			break;
		}

		compileStatements();

		getStringAndAdvance("}");
	}

	/**Compiles a subroutine call*/
	public void compileSubroutineCall()
	{
		String subRoutineName;
		if(tokens.peek().equals("("))
		{
			VMW.writePush(Segment.POINTER, 0);
			subRoutineName = getStringAndAdvance("identifier");
			getStringAndAdvance("(");
			nArgs=1;
			compileExpressionList();

			getStringAndAdvance(")");

			VMW.writeCall(classSymbolTable.getSymbolTableName()+"."+subRoutineName, nArgs);

		}
		else
		{
			//Could be class name or var name
			String name = getStringAndAdvance("identifier");
			nArgs=0;
			boolean variableFunctionCall = checkSymbolTablesAndWritePushCommandForArguments(name);
			getStringAndAdvance(".");

			subRoutineName = getStringAndAdvance("identifier");

			getStringAndAdvance("(");
			compileExpressionList();
			getStringAndAdvance(")");
			if(variableFunctionCall)
			{
				getTypeOfVariableAndWriteCallCode(name, subRoutineName);
			}
			else
				VMW.writeCall(name+"."+subRoutineName, nArgs);
		}
	}

	/**Compiles a complete method, function, or constructor*/
	public void compileSubroutineDec()
	{
		SubRoutineType = getStringAndAdvance("keyword");
		@SuppressWarnings("unused")
		String returnType = "";
		if(keyWord() == Keywords.VOID)
		{
			returnType = getStringAndAdvance("void");
		}
		else
		{
			//These are types, either a keyword (int char boolean) or className
			if(tokenType() == TokenTypes.KEYWORD)
				returnType = getStringAndAdvance("primitive");
			else
				returnType = getStringAndAdvance("own class");
		}
		String nameOfRoutine = getStringAndAdvance("identifier");
		methodSymbolTable = new SymbolTable(nameOfRoutine);
		if(SubRoutineType.equalsIgnoreCase("method"))
		{
			methodSymbolTable.define("this", classSymbolTable.getSymbolTableName(), Kind.ARG);
		}

		getStringAndAdvance("(");

		compileParameterList();

		getStringAndAdvance(")");

		//Subroutine Body generation
		compileSubroutineBody();

	}

	/**Compiles a term. This routine is faced with a
	 * slight difficulty when trying to decide
	 * between some of the alternative parsing rules.
	 * Specifically, if the current token is an
	 * identifier, the routine must distinguish
	 * between a variable, an array entry, and a
	 * subroutine call. A single look-ahead token,
	 * which may be one of [, (, or .
	 * suffices to distinguish between the three
	 * possibilities. Any other token is not part of
	 * this term and should not be advanced over
	 */
	public void compileTerm()
	{
		String varName;
		switch(tokenType())
		{
		case INTEGER_CONSTANT:
			//INTEGER
			String number = getStringAndAdvance("integerConstant");
			VMW.writePush(Segment.CONST, Integer.parseInt(number));
			break;
		case STRING_CONSTANT:
			// " STRING "
			String stringConstant = getStringAndAdvance("stringConstant");
			stringConstant = stringConstant.replace( " , " , ", ");
			stringConstant = stringConstant.substring(2,stringConstant.length()-1);
			VMW.writePush(Segment.CONST, stringConstant.length());
			VMW.writeCall("String.new", 1);
			for(int i=0; i<stringConstant.length();++i)
			{
				int curChar = stringConstant.charAt(i);
				VMW.writePush(Segment.CONST, curChar);
				VMW.writeCall("String.appendChar", 2);
			}
			break;
		case KEYWORD:
			// keyword Constant
			String keywordConstant = getStringAndAdvance("true false null this");
			switch(keywords.get(keywordConstant))
			{
			case THIS:
				VMW.writePush(Segment.POINTER, 0);
				break;
			case NULL:
				VMW.writePush(Segment.CONST, 0);
				break;
			case TRUE:
				VMW.writePush(Segment.CONST, 0);
				VMW.writeArithmetic(Command.NOT);
				break;
			case FALSE:
				VMW.writePush(Segment.CONST, 0);
				break;
			}
			break;
		case IDENTIFIER:
			switch(lookAheadForSymbol()){
			case PERIOD:
			case OPENING_PARANTHESE:
				compileSubroutineCall();
				break;
			case OPENING_BRACKET:
				int pre = operatorStack.size();
				// varName [ expression ]
				varName = getStringAndAdvance("name of variable");
				bracketStack.push(getStringAndAdvance("["));
				compileExpression();
				getStringAndAdvance("]");bracketStack.pop();
				compileOperations(pre);
				checkSymbolTablesForVariableWritePushCode(varName);
				VMW.writeArithmetic(Command.ADD);
				VMW.writePop(Segment.POINTER, 1);
				VMW.writePush(Segment.THAT, 0);
				break;
			case NOT_A_SYMBOL:
				// varname
				varName = getStringAndAdvance("name of variable");
				checkSymbolTablesForVariableWritePushCode(varName);
				break;
			}
			break;
		case SYMBOL:
			int pre = operatorStack.size();
			if(unaryOp())
			{
				// 'unirayOp' term
				String unaryOp = getStringAndAdvance("op");
				compileExpression();
				VMW.writeArithmetic(unOpTypes.get(unaryOp));
				compileOperations(pre);
			}
			else if(tokens.getCurrToken().equals("("))
			{
				// '(' expression ')'
				getStringAndAdvance("(");
				compileExpression();
				compileOperations(pre);
				getStringAndAdvance(")");
			}
			break;
		}

	}

	/**Compiles a var declaration*/
	public void compileVarDec()
	{
		String kindOfVariable = getStringAndAdvance("keyword");
		Kind kindOfVariableEnum = convertToEnumerated(kindOfVariable);

		String typeOfVariable = "";
		if(tokenType() == TokenTypes.KEYWORD)
			typeOfVariable = getStringAndAdvance("primitive");
		else if(tokenType() == TokenTypes.IDENTIFIER)
			typeOfVariable = getStringAndAdvance("class name");

		String nameOfVariable = getStringAndAdvance("identifier");
		methodSymbolTable.define(nameOfVariable, typeOfVariable, kindOfVariableEnum);
		while(tokens.getCurrToken().equals(","))
		{
			getStringAndAdvance(",");
			nameOfVariable = getStringAndAdvance("identifier");
			methodSymbolTable.define(nameOfVariable, typeOfVariable, kindOfVariableEnum);
		}
		getStringAndAdvance(";");

		return;
	}

	/**Compiles a while statement*/
	public void compileWhile()
	{
		getStringAndAdvance("while keyword");
		String endLabel = "WHILE_END" + VMWriter.labelCounter;
		String startLabel = "WHILE_EXP" + VMWriter.labelCounter;
		VMWriter.labelCounter++;
		VMW.writeLabel(startLabel);

		getStringAndAdvance("(");
		int pre = operatorStack.size();
		compileExpression();

		compileOperations(pre);
		VMW.writeArithmetic(Command.NOT);
		VMW.writeIf(endLabel);
		getStringAndAdvance(")");
		getStringAndAdvance("{");

		compileStatements();

		getStringAndAdvance("}");
		VMW.writeGoto(startLabel);
		VMW.writeLabel(endLabel);
	}
	
	private Segment convertKindToSegment(Kind theKind)
	{
		switch (theKind)
		{
		case ARG:
			return Segment.ARG;
		case FIELD:
			return Segment.THIS;
		case STATIC:
			return Segment.STATIC;
		case VAR:
			return Segment.LOCAL;
		}
		return null;
	}
	
	private Kind convertToEnumerated(String kindOfVariable) {
		if(kindOfVariable.equalsIgnoreCase("static"))
			return Kind.STATIC;
		else if (kindOfVariable.equalsIgnoreCase("var"))
			return Kind.VAR;
		else if (kindOfVariable.equalsIgnoreCase("field"))
			return Kind.FIELD;
		else
			return Kind.ARG;
	}

	private String generateFunctionTag() {
		return classSymbolTable.getSymbolTableName() + "." + methodSymbolTable.getSymbolTableName();
	}

	private String getStringAndAdvance(String tokenType)
	{
		String returnString = tokens.getCurrToken();
		tokens.advance();
		return returnString;
	}

	private Keywords getSubroutineType()
	{
		return keywords.get(SubRoutineType);
	}
	private void getTypeOfVariableAndWriteCallCode (String variable, String subRoutineName)
	{
		if(methodSymbolTable.hasVariableName(variable))
			VMW.writeCall(methodSymbolTable.typeOf(variable)+"."+subRoutineName, nArgs);
		else
			VMW.writeCall(classSymbolTable.typeOf(variable)+"."+subRoutineName, nArgs);
	}
	private void initializeBinOps() {
		binOpTypes.put("+", Command.ADD);
		binOpTypes.put("&", Command.AND);
		binOpTypes.put("-", Command.SUB);
		binOpTypes.put("|", Command.OR);
		binOpTypes.put(">", Command.GT);
		binOpTypes.put("<", Command.LT);
		binOpTypes.put("=", Command.EQ);
	}
	private void initializeKeywordsMap() {
		keywords.put("class", Keywords.CLASS);
		keywords.put("constructor", Keywords.CONSTRUCTOR);
		keywords.put("function", Keywords.FUNCTION);
		keywords.put("method", Keywords.METHOD);
		keywords.put("field", Keywords.FIELD);
		keywords.put("static", Keywords.STATIC);
		keywords.put("var", Keywords.VAR);
		keywords.put("int", Keywords.INT);
		keywords.put("char", Keywords.CHAR);
		keywords.put("boolean", Keywords.BOOLEAN);
		keywords.put("void", Keywords.VOID);
		keywords.put("true", Keywords.TRUE);
		keywords.put("false", Keywords.FALSE);
		keywords.put("null", Keywords.NULL);
		keywords.put("this", Keywords.THIS);
		keywords.put("let", Keywords.LET);
		keywords.put("do", Keywords.DO);
		keywords.put("if", Keywords.IF);
		keywords.put("else", Keywords.ELSE);
		keywords.put("while", Keywords.WHILE);
		keywords.put("return", Keywords.RETURN);
	}
	private void initializeOps() {
		ops.add("+");
		ops.add("-");
		ops.add("*");
		ops.add("/");
		ops.add("|");
		ops.add("&");
		ops.add("<");
		ops.add(">");
		ops.add("=");
	}
	private void initializeSymbolsSet() {
		symbols.add(",");
		symbols.add("{");
		symbols.add("}");
		symbols.add(")");
		symbols.add("(");
		symbols.add("[");
		symbols.add("]");
		symbols.add(".");
		symbols.add(";");
		symbols.add("+");
		symbols.add("-");
		symbols.add("*");
		symbols.add("/");
		symbols.add("&");
		symbols.add("|");
		symbols.add("<");
		symbols.add(">");
		symbols.add("=");
		symbols.add("~");
	}
	private void initializeUnOps()
	{
		unOpTypes.put("~", Command.NOT);
		unOpTypes.put("-", Command.NEG);
	}

	private boolean isASubroutineDec() {
		return ((keyWord() == Keywords.CONSTRUCTOR) ||
				(keyWord() == Keywords.METHOD)
				|| (keyWord() == Keywords.FUNCTION));
	}



	private Keywords keyWord()
	{
		if(keywords.get(tokens.getCurrToken())!=null)
			return keywords.get(tokens.getCurrToken());
		else
			return Keywords.DNE;
	}

	private Symbols lookAheadForSymbol()
	{
		String nextToken = tokens.peek();
		if(nextToken.equals("("))
			return Symbols.OPENING_PARANTHESE;
		if(nextToken.equals("["))
			return Symbols.OPENING_BRACKET;
		if(nextToken.equals("."))
			return Symbols.PERIOD;
		return Symbols.NOT_A_SYMBOL;
	}
	public void startOutput() {
		compileClass();
	}
	private TokenTypes tokenType ()
	{
		try{
			Integer.parseInt(tokens.getCurrToken());
			return TokenTypes.INTEGER_CONSTANT;
		}catch(Exception e){
			if(tokens.getCurrToken().contains("\""))
				return TokenTypes.STRING_CONSTANT;
			if(keywords.containsKey(tokens.getCurrToken()))
				return TokenTypes.KEYWORD;
			if(symbols.contains(tokens.getCurrToken()))
				return TokenTypes.SYMBOL;
			if(tokens.getCurrToken().equals(""))
				return TokenTypes.NULL;
			return TokenTypes.IDENTIFIER;
		}
	}

	private boolean unaryOp()
	{
		String temp = tokens.getCurrToken();
		return (temp.equals("-") || temp.equals("~"));
	}

}