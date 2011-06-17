import java.util.HashMap;
import java.util.Map;

/*
 * The code module class simply stores the binary strings
 * of the appropiate mnemonics for the various instructions
 * that are predefined
 */

public class CodeModule {
	private Map<String, String> destCodes, compCodes, jumpCodes;
	
	//This constructor properly initializes all the hash tables
	//with the correspond mnemonics and codes
	public CodeModule()
	{
		destCodes = new HashMap<String, String>();
		compCodes = new HashMap<String, String>();
		jumpCodes = new HashMap<String, String>();
		
		//Add all the dest codes and the appropiate mnemonics
		destCodes.put("null", "000");
		destCodes.put("M", "001");
		destCodes.put("D", "010");
		destCodes.put("MD", "011");
		destCodes.put("A", "100");
		destCodes.put("AM", "101");
		destCodes.put("AD", "110");
		destCodes.put("AMD", "111");
		
		//Add all the jump codes and the appropiate mnemonics
		jumpCodes.put("null", "000");
		jumpCodes.put("JGT", "001");
		jumpCodes.put("JEQ", "010");
		jumpCodes.put("JGE", "011");
		jumpCodes.put("JLT", "100");
		jumpCodes.put("JNE", "101");
		jumpCodes.put("JLE", "110");
		jumpCodes.put("JMP", "111");
		
		//Add all the comp codes and the appropiate mnemonics
		compCodes.put("0", "101010");
		compCodes.put("1", "111111");
		compCodes.put("-1", "111010");
		compCodes.put("D", "001100");
		compCodes.put("A", "110000");
		compCodes.put("M", "110000");
		compCodes.put("!D", "001101");
		compCodes.put("!A", "110001");
		compCodes.put("!M", "110001");
		compCodes.put("-D", "001111");
		compCodes.put("-A", "110011");
		compCodes.put("-M", "110011");
		compCodes.put("D+1", "011111");
		compCodes.put("A+1", "110111");
		compCodes.put("M+1", "110111");
		compCodes.put("D-1", "001110");
		compCodes.put("A-1", "110010");
		compCodes.put("M-1", "110010");
		compCodes.put("D+A", "000010");
		compCodes.put("D+M", "000010");
		compCodes.put("D-A", "010011");
		compCodes.put("D-M", "010011");
		compCodes.put("A-D", "000111");
		compCodes.put("M-D", "000111");
		compCodes.put("D&A", "000000");
		compCodes.put("D&M", "000000");
		compCodes.put("D|A", "010101");
		compCodes.put("D|M", "010101");
		
	}
	
	/*
	 * METHOD: dest
	 * RETURN: string
	 * INPUT: string
	 * Takes the given input dest mnemonic and returns the appropiate 
	 * binary code as a string
	 */
	public String destCode(String mn)
	{
		return destCodes.get(mn);
	}
	
	/*
	 * METHOD: comp
	 * RETURN: string
	 * INPUT: string
	 * Takes the given input comp mnemonic and returns the appropiate 
	 * binary code as a string
	 */
	public String compCode(String mn)
	{
		return compCodes.get(mn);
	}
	
	/*
	 * METHOD: jump
	 * RETURN: string
	 * INPUT: string
	 * Takes the given input jump mnemonic and returns the appropiate 
	 * binary code as a string
	 */
	public String jumpCode(String mn)
	{
		return jumpCodes.get(mn);
	}

}
