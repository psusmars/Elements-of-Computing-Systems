import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class JackTokenizer {
	private static enum TokenTypes { KEYWORD, SYMBOL, INTEGER_CONSTANT, 
		STRING_CONSTANT, IDENTIFIER, NULL} 
	private static enum Keywords { CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, 
		BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, BRACKET,
		WHILE, RETURN, TRUE, FALSE, NULL, THIS}
	private String outputFileName;
	private HashSet<String> symbols;
	private HashMap<String,Keywords> keywords;
	private Iterator<String> symbolIndex;
	private Token tks;
	private CompilationEngine ce;
	public JackTokenizer()
	{
		keywords = new HashMap<String,Keywords>();
		initializeKeywordsMap();
		symbols = new HashSet<String>();
		initializeSymbolsSet();
		symbolIndex=symbols.iterator();
	}
	public JackTokenizer(String fileName, String fullFileAsString)
	{
		keywords = new HashMap<String,Keywords>();
		initializeKeywordsMap();
		symbols = new HashSet<String>();
		initializeSymbolsSet();
		outputFileName = fileName;
		tks = new Token(tokenizeTheJackFile(fullFileAsString));
		symbolIndex=symbols.iterator();	
		ce = new CompilationEngine(outputFileName, tks);
	}
	public void startCompilationEngine()
	{
		ce.startOutput();
	}
	/**Initializes the hash set for the symbols*/
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

	/**Generates a tokenized version of the file returns it*/
	public String[] tokenizeTheJackFile(String jackFileAsString)
	{
		Pattern p = Pattern.compile("\\s");
		String [] returnTokens = p.split(jackFileAsString);
		return clean(returnTokens);
	}

	private String[] clean(String[] returnString) {
		int numOfNullTokens = 0;
		for(int i = 0; i<returnString.length; ++i)
		{
			if(returnString[i].equals(""))
				numOfNullTokens++;
			returnString[i] = returnString[i].replace('`', ' ');
		}
		String [] cleanedTokens = new String [returnString.length-numOfNullTokens];
		int counter = 0;
		for(int i =0; i<returnString.length; ++i)
			if(!returnString[i].equals(""))
				cleanedTokens[counter++] = returnString[i];
		return cleanedTokens;
	}

	public String getASymbol()
	{
		if(symbolIndex.hasNext())
		{
			return symbolIndex.next();
		}
		else
			return "done";
	}



	public void runThroughInput ()
	{
		try{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			System.out.println("Generating: " + outputFileName);

			Element rootElement = doc.createElement("tokens");
			doc.appendChild(rootElement);

			while(tks.hasMoreTokens())
			{
				tks.advance();
				String currToken = tks.getCurrToken();
				switch(tokenType()){
				case INTEGER_CONSTANT:
					Element integerConstant = doc.createElement("integerConstant");
					integerConstant.appendChild(doc.createTextNode(" " + Integer.toString(intValue())+" "));
					rootElement.appendChild(integerConstant);
					break;
				case KEYWORD:
					Element keyword = doc.createElement("keyword");
					keyword.appendChild(doc.createTextNode(" " + currToken + " "));
					rootElement.appendChild(keyword);
					break;
				case SYMBOL:
					Element symbol = doc.createElement("symbol");
					symbol.appendChild(doc.createTextNode(" " + symbol() + " "));
					rootElement.appendChild(symbol);
					break;
				case IDENTIFIER:
					Element identifier = doc.createElement("identifier");
					identifier.appendChild(doc.createTextNode(" " + currToken + " "));
					rootElement.appendChild(identifier);
					break;
				case STRING_CONSTANT:
					Element stringConstant = doc.createElement("stringConstant");
					stringConstant.appendChild(doc.createTextNode(" " + stringValue() + " "));
					rootElement.appendChild(stringConstant);
					break;
				default:
					break;
				}	
			}

			//write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File(outputFileName));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);

			System.out.println("Done");
		}catch(ParserConfigurationException pce){
			pce.getMessage();
		}catch(TransformerException tfe){
			tfe.getMessage();
		}
	}

	private TokenTypes tokenType ()
	{
		try{
			Integer.parseInt(tks.getCurrToken());
			return TokenTypes.INTEGER_CONSTANT;
		}catch(Exception e){
			if(tks.getCurrToken().contains("\""))
				return TokenTypes.STRING_CONSTANT;
			if(keywords.containsKey(tks.getCurrToken()))
				return TokenTypes.KEYWORD;
			if(symbols.contains(tks.getCurrToken()))
				return TokenTypes.SYMBOL;
			if(tks.getCurrToken().equals(""))
				return TokenTypes.NULL;
			return TokenTypes.IDENTIFIER;
		}
	}

	//only call when the token type is an integer constant
	public int intValue ()
	{
		return Integer.parseInt(tks.getCurrToken());
	}

	/** Will return the value of the String without the double quotes, only on String_Constants*/
	public String stringValue ()
	{
		return tks.getCurrToken().substring(1, tks.getCurrToken().length()-1);
	}

	//only call when the token type is a keyword
	public Keywords keyWord()
	{
		return keywords.get(tks.getCurrToken());
	}

	//Only call when the token type is a symbol
	public String symbol ()
	{
		return tks.getCurrToken().substring(0,1);
	}
}