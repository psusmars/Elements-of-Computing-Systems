import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class CompilationEngine {
	private static enum TokenTypes { KEYWORD, SYMBOL, INTEGER_CONSTANT, 
		STRING_CONSTANT, IDENTIFIER, NULL} 
	private static enum Keywords { CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, 
		BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, BRACKET,
		WHILE, RETURN, TRUE, FALSE, NULL, THIS, DNE}
	private static enum Symbols { OPENING_PARANTHESE, OPENING_BRACKET, NOT_A_SYMBOL,
		PERIOD, CLOSING_PARANTHESE }
	private HashSet<String> symbols, ops;
	private HashMap<String,Keywords> keywords;
	//TODO Create an op set that stores all operations that are possible for expressions
	//TODO Create a set that checks for keyWord constants
	private Document doc;
	/**Might take the tokenized input as a parameter.
	 * @param tks 
	 * @param outputFileName */
	private Token tokens;
	private String outputName;

	public CompilationEngine(String outputFileName, Token tks)
	{
		outputName = outputFileName.replace("T.ours.xml", ".ours.xml");
		tokens = tks;
		keywords = new HashMap<String,Keywords>();
		initializeKeywordsMap();
		symbols = new HashSet<String>();
		initializeSymbolsSet();
		ops = new HashSet<String>();
		initializeOps();
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

	public void startOutput() {
		//this.jt = jt;
		compileClass();
	}

	/**Compile a complete class*/
	public void compileClass()
	{

		try{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();
		System.out.println("Generating: " + outputName);
		if(tokens.getCurrToken().equals("class"))
		{
			Element rootElement = doc.createElement(tokens.getCurrToken());
			doc.appendChild(rootElement);

			createNode(rootElement, "keyword");

			createNode(rootElement, "identifier");

			createNode(rootElement, "symbol");

			
			
			while(tokens.getCurrToken().equals("static")||tokens.getCurrToken().equals("field"))
			{
				Element classVarDec = createRootNode(rootElement, "classVarDec");
				compileVarDec(classVarDec);
			}
			
			boolean firstTime = true;
			
			Element subRoutDec = createRootNode(rootElement, "subroutineDec");
			while(isASubroutineDec())
			{
				if(firstTime)
				{
					compileSubroutineDec(subRoutDec);
					firstTime = false;
				}
				else
				{
					Element subRoutDec1 = createRootNode(rootElement, "subroutineDec");
					compileSubroutineDec(subRoutDec1);
				}
			}

			//closing bracket
			createNode(rootElement, "symbol");

			//write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File(outputName));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);

			System.out.println("Done");
		}
		else
			return;
		}catch (ParserConfigurationException e){
			System.out.println(e.getMessage());
		}catch (TransformerConfigurationException e){
			System.out.println(e.getMessage());
		}catch (TransformerException e) {
			System.out.println(e.getMessage());
		}
	}

	private Element createRootNode(Element rootElement, String type) {
		Element node = doc.createElement(type);
		rootElement.appendChild(node);
		return node;
	}

	/**Compiles a static declaration or a field declaration*/
	public void compileClassVarDec(Element rootElement)
	{		
		createNode(rootElement, "keyword");

		if(tokenType() == TokenTypes.KEYWORD)
			createNode(rootElement, "keyword");
		if(tokenType() == TokenTypes.IDENTIFIER)
			createNode(rootElement, "identifier");

		createNode(rootElement, "identifier");

		while(tokens.getCurrToken().equals(","))
		{
			createNode(rootElement, "symbol");
			createNode(rootElement, "identifier");
		}

		createNode(rootElement, "symbol");

		return;
	}

	private void createNode(Element rootElement, String type) {
		Element node = doc.createElement(type);
		node.appendChild(doc.createTextNode(" " + tokens.getCurrToken() +" "));
		tokens.advance();
		rootElement.appendChild(node);
	}

	/**Compiles a complete method, function, or constructor*/
	public void compileSubroutineDec(Element rootElement)
	{
			createNode(rootElement, "keyword");
			
			if(keyWord() == Keywords.VOID)
			{
				createNode(rootElement, "keyword");
			}
			else
			{
				//These are types, either a keyword  (int char boolean) or className
				if(tokenType() == TokenTypes.KEYWORD)
					createNode(rootElement, "keyword");
				else
					createNode(rootElement, "identifier");
			}
			createNode(rootElement, "identifier");
			createNode(rootElement, "symbol");

			Element parameterList = createRootNode(rootElement, "parameterList");
			compileParameterList(parameterList);

			createNode(rootElement, "symbol");

			//Subroutine Body generation
			Element subRoutineBody = createRootNode(rootElement, "subroutineBody");
			compileSubroutineBody(subRoutineBody);

			//compileParameterList(subRoutineBody);
		
	}

	private void compileSubroutineBody(Element rootElement) {
		createNode(rootElement, "symbol");
		boolean firstTime = true;
		Element varDec = createRootNode(rootElement, "varDec");
		
		while(keyWord()==Keywords.VAR)
		{
			if(firstTime)
			{
				compileVarDec(varDec);
				firstTime =false;
			}
			else
			{
				Element varDec1 = createRootNode(rootElement, "varDec");
				compileVarDec(varDec1);
			}
		}
		Element statements = createRootNode(rootElement, "statements");
		compileStatements(statements);


		createNode(rootElement, "symbol");
	}

	private boolean isASubroutineDec() {
		return ((keyWord() == Keywords.CONSTRUCTOR) ||
				(keyWord() == Keywords.METHOD) 
				|| (keyWord() == Keywords.FUNCTION));
	}

	/**Compiles a (possibly empty) parameter list, not including the enclosing "()"*/
	public void compileParameterList(Element rootElement)
	{
		//createNode(rootElement, "symbol");
		if(tokens.getCurrToken().equals(")"))
		{
			//createNode(rootElement, "symbol");
			return;
		}
		if(tokenType() == TokenTypes.KEYWORD ) 
			createNode(rootElement, "keyword" );
		if(tokenType() == TokenTypes.IDENTIFIER)
			createNode(rootElement, "identifier");
		while(tokens.getCurrToken().equals(","))
		{
			createNode(rootElement, "symbol");
			if(tokenType() == TokenTypes.KEYWORD ) 
				createNode(rootElement, "keyword" );
			else if(tokenType() == TokenTypes.IDENTIFIER)
				createNode(rootElement, "identifier");
			createNode(rootElement, "identifier");
		}
		//createNode(rootElement, "symbol");
	}

	/**Compiles a var declaration
	 * @param rootElement */
	public void compileVarDec(Element rootElement)
	{
		createNode(rootElement, "keyword");

		if(tokenType() == TokenTypes.KEYWORD)
			createNode(rootElement, "keyword");
		else if(tokenType() == TokenTypes.IDENTIFIER)
			createNode(rootElement, "identifier");

		createNode(rootElement, "identifier");
		
  		while(tokens.getCurrToken().equals(","))
		{
			createNode(rootElement, "symbol");
			createNode(rootElement, "identifier");
		}

		createNode(rootElement, "symbol");

		return;
	}

	/**Compiles a sequence of statements, not including the enclosing "{}"*/
	public void compileStatements(Element rootElement)
	{
		while(keyWord()==Keywords.LET || keyWord()==Keywords.IF || keyWord()==Keywords.WHILE ||
				keyWord()==Keywords.DO || keyWord()==Keywords.RETURN )
		{
			switch(keyWord()){
			case LET:
				Element letStatement = createRootNode(rootElement, "letStatement");
				compileLet(letStatement);
				break;
			case WHILE:
				Element whileStatement = createRootNode(rootElement, "whileStatement");
				compileWhile(whileStatement);
				break;
			case IF:
				Element ifStatement = createRootNode(rootElement, "ifStatement");
				compileIf(ifStatement);
				break;
			case DO:
				Element doStatement = createRootNode(rootElement, "doStatement");
				compileDo(doStatement);
				break;
			case RETURN:
				Element returnStatement = createRootNode(rootElement, "returnStatement");
				compileReturn(returnStatement);
				break;
			}
		}
	}

	/**Compiles a do statement*/
	public void compileDo(Element rootElement)
	{
		createNode(rootElement, "keyword");
		//Element subRoutin = createRootNode(rootElement, "expression");
		compileSubroutineCall(rootElement);
		createNode(rootElement, "symbol");

	}

	/**Compiles a let statement*/
	public void compileLet( Element rootElement )
	{
		createNode(rootElement, "keyword");
		createNode(rootElement, "identifier");
		if(tokens.getCurrToken().equals("["))
		{
			createNode(rootElement, "symbol");
			Element expression = createRootNode(rootElement, "expression");
			compileExpression(expression);
			createNode(rootElement, "symbol");
		}
		createNode(rootElement, "symbol");
		
		Element expression = createRootNode(rootElement, "expression");
		compileExpression(expression);

		createNode(rootElement, "symbol");
	}

	/**Compiles a while statment*/
	public void compileWhile(Element rootElement)
	{
		createNode(rootElement, "keyword");
		createNode(rootElement, "symbol");

		Element expression = createRootNode(rootElement, "expression");
		compileExpression(expression);

		createNode(rootElement, "symbol");
		createNode(rootElement, "symbol");
		
		Element statements = createRootNode(rootElement, "statements");
		compileStatements(statements);

		createNode(rootElement, "symbol");
	}

	/**Compiles a return statement*/
	public void compileReturn(Element rootElement)
	{
		createNode(rootElement, "keyword");
		
		if(tokenType() != TokenTypes.SYMBOL)
		{
			Element expression = createRootNode(rootElement, "expression");
			compileExpression(expression);
		}

		createNode(rootElement, "symbol");
	}

	/**Compiles an if statement, possibly with a trailing else clause*/
	public void compileIf(Element rootElement)
	{
		createNode(rootElement, "keyword");
		createNode(rootElement, "symbol");
		
		Element expression = createRootNode(rootElement, "expression");
		compileExpression(expression);
		
		createNode(rootElement, "symbol");
		createNode(rootElement, "symbol");

		Element statements = createRootNode(rootElement, "statements");
		compileStatements(statements);
		createNode(rootElement, "symbol");
		if(keyWord() == Keywords.ELSE)
		{
			createNode(rootElement, "keyword");
			createNode(rootElement, "symbol");
			Element statements2 = createRootNode(rootElement, "statements");
			compileStatements(statements2);
			createNode(rootElement, "symbol");
		}
	}

	/**Compiles an expression*/
	public void compileExpression(Element rootElement)
	{
		Element term = createRootNode(rootElement, "term");
		compileTerm(term);
		/*TODO this might be done and ready to go*/
		while(anOp())
		{
			//op term
			createNode(rootElement, "symbol");
			Element term1 = createRootNode(rootElement, "term");
			compileTerm(term1);
		}
	}

	private boolean anOp() {
		return ops.contains(tokens.getCurrToken());
	}

	/**Compiles a subroutine call*/
	public void compileSubroutineCall(Element rootElement)
	{
		if(tokens.peek().equals("("))
		{
			createNode(rootElement, "identifier");
			createNode(rootElement, "symbol");
			
			Element expressionList = createRootNode(rootElement, "expressionList");
			compileExpressionList(expressionList);
			
			createNode(rootElement, "symbol");
		}
		else
		{
			//Could be class name or var name
			createNode(rootElement, "identifier");
			
			createNode(rootElement, "symbol");
			
			createNode(rootElement, "identifier");
			
			createNode(rootElement, "symbol");
			
			Element expressionList1 = createRootNode(rootElement, "expressionList");
			compileExpressionList(expressionList1);
			
			createNode(rootElement, "symbol");
		}
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
	public void compileTerm(Element rootElement)
	{
		//createNode(rootElement, "identifier");
		//TODO This might be done.
		switch(tokenType())
		{
		case INTEGER_CONSTANT:
			//INTEGER
			createNode(rootElement, "integerConstant");
			break;
		case STRING_CONSTANT:
			// " STRING "
			//createNode(rootElement, "symbol");
			tokens.advance();
			createNode(rootElement, "stringConstant");
			tokens.advance();
			//createNode(rootElement, "symbol");
			break;
		case KEYWORD:
			// keyword Constant
			createNode(rootElement, "keyword");
			break;
		case IDENTIFIER:
			switch(lookAheadForSymbol()){
			case PERIOD:
			case OPENING_PARANTHESE:
				compileSubroutineCall(rootElement);
				break;
			case OPENING_BRACKET:
				// varName [ expression ]
				createNode(rootElement, "identifier");
				createNode(rootElement, "symbol");
				Element expression = createRootNode(rootElement, "expression");
				compileExpression(expression);
				createNode(rootElement, "symbol");
				break;
			case NOT_A_SYMBOL:
				// varname
				createNode(rootElement, "identifier");
				break;
			}
			break;
		case SYMBOL:
			if(unirayOp())
			{
				// 'unirayOp' term
				createNode(rootElement, "symbol");
				Element term = createRootNode(rootElement, "term");
				compileExpression(term);
			}
			else if(tokens.getCurrToken().equals("("))
			{
				// '(' expression ')'
				createNode(rootElement, "symbol");
				Element expression = createRootNode(rootElement, "expression");
				compileExpression(expression);
				createNode(rootElement, "symbol");
			}
			break;
		}
		
	}

	/**Compiles a (possibly empty) comma-separated list of expressions*/
	public void compileExpressionList(Element rootElement)
	{
		//TODO Generate function, this might be correct
		if(tokens.getCurrToken().equals(")"))
			return;
		else
		{
			Element expression = createRootNode(rootElement, "expression");
			compileExpression(expression);
			while( tokens.getCurrToken().equals(",") )
			{
				createNode(rootElement, "symbol");
				Element expression1 = createRootNode(rootElement, "expression");
				compileExpression(expression1);
			}
		}
	}
	
	private boolean unirayOp()
	{
		String temp = tokens.getCurrToken();
		return (temp.equals("-") || temp.equals("~"));
	}
	private boolean keywordConstant()
	{
		Keywords temp = keyWord();
		return ((temp == Keywords.TRUE) || (temp == Keywords.FALSE) 
				|| (temp == Keywords.NULL) || (temp == Keywords.THIS));
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

}