import java.util.*;

/*
 * This class holds the symbols for the A register to use
 * being either an address label, variable or predefined 
 * symbol
 */
public class SymbolTable {
/*This will need a member that holds all the symbols for
 * a given .asm file, likely a map that holds strings of the
 * symbol and the corresponding address, this will actually
 * keep track of addresses
 */
	private Map<String, Integer> symbols;
	
	//Default constructor to allocate space
	public SymbolTable ()
	{
		symbols = new HashMap<String, Integer>();
		//The following adds the predefined symbols that are permitted
		//for any asm file
		symbols.put("SP", 0);
		symbols.put("LCL", 1);
		symbols.put("ARG", 2);
		symbols.put("THIS", 3);
		symbols.put("THAT", 4);
		symbols.put("SCREEN", 16384);
		symbols.put("KBD", 24576);
		for(int i=0; i<=15; i++)
			symbols.put("R"+i, i);
	}
	
	/*
	 * METHOD: add
	 * RETURN: void
	 * INPUT: string, integer
	 * Adds the input string to the array list, this will
	 * be a symbol, with its address
	 */
	public void add(String symbolName, int address)
	{
		symbols.put(symbolName, address);
		return;
	}
	
	/*
	 * METHOD: contains
	 * RETURN: boolean
	 * INPUT: string
	 * Checks to see if the given symbol has been used, if
	 * it has, returns true, else returns false
	 */
	public boolean contains(String symbolName)
	{
		return symbols.containsKey(symbolName);
	}
	
	/*
	 * METHOD: getAddress
	 * RETURN: Integer
	 * INPUT: string
	 * Returns the address for the given symbol
	 */
	public int getAddress (String symbolName)
	{
		return symbols.get(symbolName);
	}
}
