import java.io.*;
import java.util.*;
//This is the parser module
public class VMCompiler {
	private ArrayList<String> inputFileNames;
	private String outputFileName;
	private CodeWriter cWriter;
	private String[] commandString;
	public static enum  CommandType {C_ARITHMETIC, C_PUSH, C_POP, C_LABEL,
		C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL, NULL
	}

	VMCompiler ( ArrayList<String> fileNames)
	{
		inputFileNames = fileNames;
		if(inputFileNames.size() > 1 )
		{
			int indexOfLastSlash = inputFileNames.get(0).lastIndexOf('\\');
			String fileNameRemoved = inputFileNames.get(0).substring(0, indexOfLastSlash);
			indexOfLastSlash = fileNameRemoved.lastIndexOf('\\');
			String nameForFile = fileNameRemoved.substring(indexOfLastSlash+1, fileNameRemoved.length());
			if(fileNameRemoved.indexOf('\\')!=fileNameRemoved.length()-1)
				fileNameRemoved = fileNameRemoved +'\\';
			outputFileName = fileNameRemoved + nameForFile + ".asm";
		}
		else
		{
			outputFileName = inputFileNames.get(0).replace(".vm", ".asm");
		}
		cWriter = new CodeWriter(outputFileName);
	}
	//Opens the files for coding, and gets ready to parse it
	void openFilesAndExecute ()
	{
		String readLine;


		try
		{
			FileOutputStream asmFile;
			PrintStream output;
			asmFile = new FileOutputStream(outputFileName);
			output = new PrintStream( asmFile );
			cWriter.setOutputFile(output);
			for(int i=0; i<inputFileNames.size(); ++i)
			{				

				try {

					FileInputStream vmFile = new FileInputStream(inputFileNames.get(i));

					// Get the object of DataInputStream
					DataInputStream in = new DataInputStream(vmFile);
					BufferedReader input = new BufferedReader(new InputStreamReader (in));
					//Hasmorecommands and advance are executed by this while loop
					while ((readLine = input.readLine()) != null)
					{
						performComparisonsAndOutputToFile(readLine);
					}
					in.close();
				}catch (Exception e){
					System.out.println("Something went terribly wrong with opening the input file, I balem Yong!");
				}
			}
			asmFile.close();
		}catch (Exception e){
			System.out.println("Something went horribly wrong with opening the output file, I blame Yong.");
		}
	}

	private void performComparisonsAndOutputToFile(String readLine) {
		tokenizeAndStoreInCommandString(readLine);
		CommandType typeOfCommand = commandTypeOfTheInputString();
		switch (typeOfCommand){
		case C_ARITHMETIC:
			cWriter.writeArithmetic(commandString[0]);
			break;
		case C_PUSH:
			cWriter.writePushPop(typeOfCommand, commandString);
			break;
		case C_POP:
			cWriter.writePushPop(typeOfCommand, commandString);
			break;
		case C_LABEL:
			break;
		case C_GOTO:
			break;
		case C_IF:
			break;
		case C_FUNCTION:
			break;
		case C_RETURN:
			break;
		case C_CALL:
			break;
		case NULL:
			break;
		}
		
	}
	
	private void tokenizeAndStoreInCommandString(String readLine) {
		String trimmedLine = readLine.trim();
		String [] tokenizedString;
		tokenizedString = trimmedLine.split(" ");
		commandString = tokenizedString;
	}
	//Returns the type of the current VM command based on the string
	//C_ARITHMETIC will be returned for all arithmetic commands
	public CommandType commandTypeOfTheInputString()
	{
		if(commandString.length == 1 && !commandString[0].equals("return"))
			return CommandType.C_ARITHMETIC;
		if(commandString[0].equals("push"))
			return CommandType.C_PUSH;
		if(commandString[0].equals("pop"))
			return CommandType.C_POP;
		if(commandString[0].equals("if-goto"))
			return CommandType.C_IF;
		if(commandString[0].equals("goto"))
			return CommandType.C_GOTO;
		if(commandString[0].equals("function"))
			return CommandType.C_FUNCTION;
		if(commandString[0].equals("call"))
			return CommandType.C_CALL;
		if(commandString[0].equals("return"))
			return CommandType.C_RETURN;
		if(commandString[0].equals("label"))
			return CommandType.C_LABEL;
		return CommandType.NULL;
	}
	
	//Returns the first argument of the current command. for example
	//C_Arithmatic will return the command itself as a string
	public String getArg1 ()
	{
		return null;
	}
	
	//Returns the second argument of the current command
	//will only be called on the push, pop, function, and call commands
	public int getArg2 ()
	{
		return 0;
	}
	
	public static void main(String[] args) {
		if(args.length == 1)
		{
			ArrayList<String> files = new ArrayList<String>();
			if(args[0].endsWith(".vm"))
			{
				files.add(args[0]);
				VMCompiler compiler = new VMCompiler (files);
				compiler.openFilesAndExecute();
			}
			else
			{
				File folder = new File (args[0]);
				if(folder.exists())
				{
					File [] listOfFiles = folder.listFiles();
					for(int i = 0; i < listOfFiles.length ; ++i )
					{
						if(listOfFiles[i].toString().endsWith(".vm"))
							files.add(listOfFiles[i].toString());
					}
					VMCompiler compiler = new VMCompiler (files);
					compiler.openFilesAndExecute();
				}
				else
					System.out.println( args[0] + " is an invalid path name.");
				
			}
		}
		else
			System.out.println("Make sure you are passing in a path name, and only one path name.");
		

	}

}