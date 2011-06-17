import java.io.*;


public class Assembler {
	private SymbolTable sTable;
	private CodeModule cMod;
	private String inputLine, outputName, inputName;
	public enum CommandType{  A_COMMAND, C_COMMAND,L_COMMAND, NO_COMMAND}
	
	//This keeps track of the address to store user defined symbols
	public static int address = 16;
	
	/*
	 * Constructor for the assembler, also creates the SymbolTable
	 * and codemodule
	 */
	public Assembler(String in, String out)
	{
		inputName = in;
		outputName = out;
		sTable = new SymbolTable();
		cMod = new CodeModule();
			
	}
	
	/*FUNCTION: firstPass
	 * RETURN: VOID
	 * POSTCONDITION: instance variables properly set for second pass
	 * Collects and stores all symbols that refer to memory
	 *  into the symbol table -> anything of the form (Xxx)
	 * From here, the input file will need to be reset to be iterated
	 * through again.
	 */  
	private void firstPass()
	{
		String readLine;
		try {
			FileInputStream assemblyFile = new FileInputStream(inputName);
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(assemblyFile);
			BufferedReader input = new BufferedReader(new InputStreamReader (in));
			
			//Linecounter keeps track of the lines for the memory addresses defined
			//by user, i.e (Xxx) corresponds to say line 0
			int lineCounter= 0;
			//Passes through the input stream only finding L commands
			//If there is an L command, adds the symbol to the symbol table and 
			//stores the integer value of that address (which is tracked by the
			//Symbol table class
			while ((readLine = input.readLine()) != null)
			{
				if(readLine.contains("//"))
				{
					String temp = readLine.substring(0, readLine.indexOf('/'));
					inputLine = temp.trim();
				}
				else
					inputLine = readLine.trim();
				CommandType cType = getTheCommandTypeOfTheInputString();
				if(cType == CommandType.L_COMMAND  && !sTable.contains(symbol()))
					sTable.add(symbol(),lineCounter);
				if(cType != CommandType.NO_COMMAND  && cType != CommandType.L_COMMAND)
					lineCounter++;
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Error running through the first pass of the asm file.");
		}
	}
	/*FUNCTION: passThrough
	 * RETURN: VOID
	 * POSTCONDITION: .hack file filled with binary code
	 * Heart of the assembler.  This will properly convert the given code
	 * to binary and output it to the output file.
	 * A switch statement will be used in conjunction with the 
	 * command type function.  There are several functions
	 * that will need to be called within this function.  Java has easy
	 * handling for iterating through the input file.
	 * Needed: commandType, symbol, dest, comp, jump, translateComp,
	 * translateDest, translateJump
	 */
	public void secondPass()
	{
		String readLine;
		try
		{
			//Sets up the file for input taking in the proper file name
			FileInputStream assemblyFile = new FileInputStream(inputName);
			FileOutputStream hackFile;
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(assemblyFile);
			BufferedReader input = new BufferedReader(new InputStreamReader (in));
			PrintStream output;
			
			//First pass is used to collect L commands for memory location
			firstPass();
			
        try {
        	hackFile = new FileOutputStream(outputName);
			output = new PrintStream( hackFile );
			while ((readLine = input.readLine()) != null)
			{
				if(readLine.contains("//"))
				{
					String temp = readLine.substring(0, readLine.indexOf('/'));
					inputLine = temp.trim();
				}
				else
					inputLine = readLine.trim();
				CommandType cType = getTheCommandTypeOfTheInputString();
				switch (cType){
				case A_COMMAND:
					String symbolCode, symbolMn = symbol();
					output.append('0');
					if(sTable.contains(symbolMn))
						symbolCode = Integer.toBinaryString(sTable.getAddress(symbolMn));
					else
						if(notANumber(symbolMn))
						{
							sTable.add(symbolMn, address);
							address++;
							symbolCode = Integer.toBinaryString(sTable.getAddress(symbolMn));
						}
						else
							symbolCode = Integer.toBinaryString(Integer.parseInt(symbolMn));
					//if the symbol code is 16 binary digits long, just add it to the hack
					if(symbolCode.length() == 15)
						output.append(symbolCode);
					else
					{
						//adds 0s if the symbol code is not 16 binary digits long
						for(int i=1; i <= 15 - symbolCode.length(); ++i)
							output.append('0');
						output.append(symbolCode);
					}
					output.println();
					break;
				case C_COMMAND:
					output.append("111");
					//Occurs if a dest instruction
					String destMn, jumpMn, compMn;
					if(inputLine.contains("="))
					{
						destMn = dest();
						jumpMn = "null";
						compMn = comp();
						if(compMn.contains("M"))
							output.append('1');
						else
							output.append('0');
						output.append(cMod.compCode(compMn));
						output.append(cMod.destCode(destMn));
						output.append(cMod.jumpCode(jumpMn));
						output.println();
					}
					//This occurs if its a jump instruction
					if(inputLine.contains(";"))
					{
						destMn = "null";
						jumpMn = jump();
						compMn = comp();
						if(compMn.contains("M"))
							output.append('1');
						else
							output.append('0');
						output.append(cMod.compCode(compMn));
						output.append(cMod.destCode(destMn));
						output.append(cMod.jumpCode(jumpMn));
						output.println();
					}
					break;
				case L_COMMAND:
					//L_Commands are handled in the first pass.
					//But this still exists since they are still read during input reading
					break;
				
				case NO_COMMAND:
					//This occurs if there was no command to be read
					//for example a blank line or comments
					break;
				}
					
			}
			in.close();
			hackFile.close();
		} catch (IOException e) {
			System.out.println("IOException: " + e + '\n' + "This occured during output." );
		}
		} catch (Exception e) {
			System.err.println("File input error, assure name is correct.");
		}
		
	}
	
	/*
	 * Test whether or not a passed in symbol (generally for A instructions)
	 * is a number or not.  The catch statement returns true if it failed to be
	 * parsed to an integer
	 */
	private boolean notANumber(String symbol) {
		try{
			Integer.parseInt(symbol);
			return false;
		}catch (Exception e){
			return true;
		}
	}

	/*MEHTOD: commandType
	 * INPUT: string
	 * RETURN: Enumerated command type
	 * Takes in the input line and checks for the type of instruction
	 * returning the proper enumerated type defined as follows:
	 *  A_COMMAND for @Xxx where Xxx is either a symbol or a decimal number
	 *  C_COMMAND for dest=comp;jump
	 *  L_COMMAND (actually, pseudocommand) for (Xxx) where Xxx is a symbol
	 */
	public CommandType getTheCommandTypeOfTheInputString ()
	{
		if(inputLine.contains("@"))
			return CommandType.A_COMMAND;
		if(inputLine.contains("=") || inputLine.contains(";"))
			return CommandType.C_COMMAND;
		if(inputLine.contains("(") && inputLine.contains(")"))
			return CommandType.L_COMMAND;
		return CommandType.NO_COMMAND;
	}
	
	/*METHOD: dest
	 * RETURN: string
	 * Returns the dest mnemonic for the current C-command, this will only
	 * be called when the commandType is a c_Command
	 */
	public String dest()
	{
		return inputLine.substring(0, inputLine.indexOf('='));
	}
	
	/*
	 * METHOD: symbol
	 * RETURN: String
	 * When there is an A or L command at hand, this function will be called.
	 * It returns the symbol or decimal of Xxx when there is either @Xxx or (Xxx)
	 */
	public String symbol () 
	{
		if(inputLine.contains("@"))
			return inputLine.substring(inputLine.indexOf('@')+1, inputLine.length());
		if(inputLine.contains("(") && inputLine.contains(")"))
			return inputLine.substring(inputLine.indexOf('(') + 1, inputLine.indexOf(')'));
		return null;
	}
	
	/*
	 * METHOD: comp
	 * RETURN: string
	 * Returns the comp mnemonic in the current C-command (28 possibilites)
	 * Should only be called when command type is a C command
	 */
	public String comp ()
	{
		if(inputLine.contains("="))
			return inputLine.substring(inputLine.indexOf('=')+1, inputLine.length());
		else
			return inputLine.substring(0, inputLine.indexOf(';'));
	}
	
	/*
	 * METHOD: jump
	 * RETURN: string
	 * Returns the jump mnemonic in the current C-command (8 possibilties)
	 * Should be called only when command type is a c command.
	 */
	public String jump()
	{
		return inputLine.substring(inputLine.indexOf(';')+1, inputLine.length());
	}
	
	public static void main(String[] args) {
		if(args.length == 1 && args[0].endsWith(".asm"))
		{
			Assembler assembler = new Assembler(args[0], args[0].replace("asm", "hack"));
			assembler.secondPass();
		}
		else
			System.out.println("Make sure the input name ends with .asm");

	}

}
