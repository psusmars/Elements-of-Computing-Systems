import java.io.PrintStream;

//Class that translates VM commands into Hack assembly
//Might be using template files
public class CodeWriter {
	private static int labelIndex=0;
	private String labelForStaticCommands;
	
	private static enum ObjectType {
		CONSTANT, TEMP, LOCAL, THIS, THAT, ARGUMENT, POINTER, STATIC,
		FAIL
	}
	
	
	public void setLabelForStaticCommands(String labelForStaticCommands) {
		this.labelForStaticCommands = labelForStaticCommands;
	}

	private String labelsForFunctions;
	
	//For short hand oF refers to output file
	private PrintStream outputFile;
	
	//Constructor that may open the output file/stream and gets ready to write to it
	public CodeWriter(String outputFileName)
	{
		int indexOfPeriod = outputFileName.indexOf('.');
		int indexOfLastSlash = outputFileName.lastIndexOf('\\');
		labelForStaticCommands = outputFileName.substring(indexOfLastSlash+1, indexOfPeriod);
		labelsForFunctions = "main";
	}
	
	//Writes the assembly code that is the translation of the given commands, 
	//where command is either C_Push or C_pop
	
	public void writePushPop(VMTranslator.CommandType command, String [] tokenizedCommand)
	{
		ObjectType typeOfObject = getTypeOfObjectFromCommand(tokenizedCommand[1]);
		switch (command){
		case C_PUSH:
			switch (typeOfObject)
			{
			case CONSTANT:
				appendConstantPushCode(tokenizedCommand[2]);
				break;
			case TEMP:
				appendTempPushCode(Integer.parseInt(tokenizedCommand[2]));
				break;
			case LOCAL:
				appendLocalPushCode(tokenizedCommand[2]);
				break;
			case THIS:
				appendThisPushCode(tokenizedCommand[2]);
				break;
			case THAT:
				appendThatPushCode(tokenizedCommand[2]);
				break;
			case ARGUMENT:
				appendArgPushCode(tokenizedCommand[2]);
				break;
			case POINTER:
				appendPointerPushCode(Integer.parseInt(tokenizedCommand[2]));
				break;
			case STATIC:
				appendStaticPushCode(tokenizedCommand[2]);
				break;
			}
			break;
		case C_POP:
			switch (typeOfObject)
			{
			case TEMP:
				appendTempPopCode(Integer.parseInt(tokenizedCommand[2]));
				break;
			case LOCAL:
				appendLocalPopCode(tokenizedCommand[2]);
				break;
			case THIS:
				appendThisPopCode(tokenizedCommand[2]);
				break;
			case THAT:
				appendThatPopCode(tokenizedCommand[2]);
				break;
			case ARGUMENT:
				appendArgPopCode(tokenizedCommand[2]);
				break;
			case POINTER:
				appendPointerPopCode(Integer.parseInt(tokenizedCommand[2]));
				break;
			case STATIC:
				appendStaticPopCode(tokenizedCommand[2]);
				break;
			}
			break;
		default:
			break;
		}
	}
	

	private ObjectType getTypeOfObjectFromCommand(String object) {
		if(object.equals("constant"))
			return ObjectType.CONSTANT;
		if(object.equals("temp"))
			return ObjectType.TEMP;
		if(object.equals("local"))
			return ObjectType.LOCAL;
		if(object.equals("this"))
			return ObjectType.THIS;
		if(object.equals("that"))
			return ObjectType.THAT;
		if(object.equals("argument"))
			return ObjectType.ARGUMENT;
		if(object.equals("pointer"))
			return ObjectType.POINTER;
		if(object.equals("static"))
			return ObjectType.STATIC;
		return ObjectType.FAIL;
	}

	//Writes the assembly code that is the translation of the given arithmetic command
	public void writeArithmetic (String command)
	{
		if(command.equals("eq"))
			appendEqualsCode();
		if(command.equals("lt"))
			appendLessThanCode();
		if(command.equals("gt"))
			appendGreaterThanCode();
		if(command.equals("sub"))
			appendSubCode();
		if(command.equals("add"))
			appendAddCode();
		if(command.equals("and"))
			appendANDCode();
		if(command.equals("or"))
			appendORCode();
		if(command.equals("neg"))
			appendNegCode();
		if(command.equals("not"))
			appendNotCode();
		
	}
	

	public void writeInitializationCode() {
		appendToOutputFile("@256");
		appendToOutputFile("D=A");
		appendToOutputFile("@SP");
		appendToOutputFile("M=D");
		appendToOutputFile("//call " + "Sys.init" + " with " + "0 args" );
		appendToOutputFile("@returnAddress" + labelIndex);
		appendToOutputFile("D=A");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
		
		//Push lcl onto the stack
		appendToOutputFile("@LCL");
		appendBasicPushCode();
		
		//Push Arg onto the stack
		appendToOutputFile("@ARG");
		appendBasicPushCode();
		
		//Push this and that onto the stack
		appendToOutputFile("@THIS");
		appendBasicPushCode();
		
		appendToOutputFile("@THAT");
		appendBasicPushCode();
		
		appendToOutputFile("@SP");
		appendToOutputFile("D=M");
		appendToOutputFile("@0");
		appendToOutputFile("D=D-A");
		appendToOutputFile("@5");
		appendToOutputFile("D=D-A");
		appendToOutputFile("@ARG");
		appendToOutputFile("M=D");
		
		appendToOutputFile("@SP");
		appendToOutputFile("D=M");
		appendToOutputFile("@LCL");
		appendToOutputFile("M=D");
		
		//TransferControl
		appendToOutputFile("@Sys.init");
		appendToOutputFile("0; JMP");
		
		appendToOutputFile("(returnAddress" + labelIndex + ")");
		labelIndex++;
		
	}
	
	public void writeLabelCode (String label)
	{
		appendToOutputFile("//label " + label );
		appendToOutputFile("(" + labelsForFunctions + "$" + label + ")");
	}
	
	public void writeGoToCode (String label)
	{
		appendToOutputFile("// goto");
		appendToOutputFile("@" + labelsForFunctions + "$" + label);
		appendToOutputFile("0; JMP");
	}
	
	public void writeIfGoToCode(String label) {
		appendToOutputFile("//ifgoto " + label);
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@" + labelsForFunctions + "$" + label);
		appendToOutputFile("D;JNE");
	}
	
	public void writeCallCode(String functionName, String numArgs)
	{
		appendToOutputFile("//call " + functionName + " with " + numArgs);
		appendToOutputFile("@returnAddress" + labelIndex);
		appendToOutputFile("D=A");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
		
		//Push lcl onto the stack
		appendToOutputFile("@LCL");
		appendBasicPushCode();
		
		//Push Arg onto the stack
		appendToOutputFile("@ARG");
		appendBasicPushCode();
		
		//Push this and that onto the stack
		appendToOutputFile("@THIS");
		appendBasicPushCode();
		
		appendToOutputFile("@THAT");
		appendBasicPushCode();
		
		appendToOutputFile("@SP");
		appendToOutputFile("D=M");
		appendToOutputFile("@" + numArgs);
		appendToOutputFile("D=D-A");
		appendToOutputFile("@5");
		appendToOutputFile("D=D-A");
		appendToOutputFile("@ARG");
		appendToOutputFile("M=D");
		
		appendToOutputFile("@SP");
		appendToOutputFile("D=M");
		appendToOutputFile("@LCL");
		appendToOutputFile("M=D");
		
		//TransferControl
		appendToOutputFile("@" + functionName);
		appendToOutputFile("0; JMP");
		
		appendToOutputFile("(returnAddress" + labelIndex + ")");
		labelIndex++;
	}
	
	public void writeReturnCode()
	{
		appendToOutputFile("//Return code");
		appendToOutputFile("@LCL");
		appendToOutputFile("D=M");
		//Store the LCL value in R13 (FRAME)
		appendToOutputFile("@R13");
		appendToOutputFile("M=D");
		appendToOutputFile("@5");
		appendToOutputFile("A=D-A");
		appendToOutputFile("D=M");
		
		//Store the return address in R14 (RET)
		appendToOutputFile("@R14");
		appendToOutputFile("M=D");
		
		//Reposition the return value for the caller
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@ARG");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		
		//Restore the SP of the caller
		appendToOutputFile("@ARG");
		appendToOutputFile("D=M+1");
		appendToOutputFile("@SP");
		appendToOutputFile("M=D");
		
		//Restore the appropriate variables of the caller
		appendToOutputFile("@R13");
		appendToOutputFile("D=M");
		appendToOutputFile("@1");
		appendToOutputFile("A=D-A");
		appendToOutputFile("D=M");
		appendToOutputFile("@THAT");
		appendToOutputFile("M=D");
		
		appendToOutputFile("@R13");
		appendToOutputFile("D=M");
		appendToOutputFile("@2");
		appendToOutputFile("A=D-A");
		appendToOutputFile("D=M");
		appendToOutputFile("@THIS");
		appendToOutputFile("M=D");
		
		appendToOutputFile("@R13");
		appendToOutputFile("D=M");
		appendToOutputFile("@3");
		appendToOutputFile("A=D-A");
		appendToOutputFile("D=M");
		appendToOutputFile("@ARG");
		appendToOutputFile("M=D");
		
		appendToOutputFile("@R13");
		appendToOutputFile("D=M");
		appendToOutputFile("@4");
		appendToOutputFile("A=D-A");
		appendToOutputFile("D=M");
		appendToOutputFile("@LCL");
		appendToOutputFile("M=D");
		
		//Goto the returnaddress (RET) int he caller's code
		appendToOutputFile("@R14");
		appendToOutputFile("A=M");
		appendToOutputFile("0;JMP");
		
	}
	
	private void appendBasicPushCode ()
	{
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	public void writeFunctionCode(String functionName, String numVariables){
		labelsForFunctions = functionName;
		int k = Integer.parseInt(numVariables);
		appendToOutputFile("// Function declartion for " + functionName);
		appendToOutputFile("(" + functionName + ")");
		appendToOutputFile("@0");
		appendToOutputFile("D=A");
		appendToOutputFile("@R13");
		appendToOutputFile("M=D");
		appendToOutputFile("(LoopForArgs" + labelIndex + ")");
		appendToOutputFile("@" + k);
		appendToOutputFile("D=A-D");
		appendToOutputFile("@continue" + labelIndex);
		appendToOutputFile("D; JEQ");
		
		appendToOutputFile("@0");
		appendToOutputFile("D=A");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
		
		appendToOutputFile("@R13");
		appendToOutputFile("MD=M+1");
		appendToOutputFile("@LoopForArgs" + labelIndex);
		appendToOutputFile("0; JMP");
		appendToOutputFile("(continue" + labelIndex + ")");
		labelIndex++;
	}
	
	private void appendNotCode() {
		appendToOutputFile("// neg");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=!M");
	}

	private void appendNegCode() {
		appendToOutputFile("// neg");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=-M");
	}

	private void appendORCode() {
		appendToOutputFile("// and");
		appendToOutputFile("@SP");
		appendToOutputFile("AM=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=D|M");
	}

	private void appendANDCode() {
		appendToOutputFile("// and");
		appendToOutputFile("@SP");
		appendToOutputFile("AM=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=D&M");
	}

	private void appendAddCode() {
		appendToOutputFile("// add");
		appendToOutputFile("@SP");
		appendToOutputFile("AM=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=D+M");
	}

	private void appendEqualsCode() {
		appendToOutputFile("//eq");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("A=M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M-D");
		appendToOutputFile("@setTrue" + labelIndex);
		appendToOutputFile("D; JEQ");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=0");
		appendToOutputFile("@continue" + labelIndex);
		appendToOutputFile("0; JMP");
		appendToOutputFile("(setTrue" + labelIndex + ")");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=-1");
		appendToOutputFile("(continue" + labelIndex + ")");
		labelIndex++;
	}

	private void appendSubCode() {
		appendToOutputFile("// sub");
		appendToOutputFile("@SP");
		appendToOutputFile("AM=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=M-D");
	}

	private void appendGreaterThanCode() {
		appendToOutputFile("// gt");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("A=M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M-D");
		appendToOutputFile("@setTrue" + labelIndex);
		appendToOutputFile("D; JGT");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=0");
		appendToOutputFile("@continue" + labelIndex);
		appendToOutputFile("0; JMP");
		appendToOutputFile("(setTrue" + labelIndex + ")");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=-1");
		appendToOutputFile("(continue" + labelIndex + ")");
		labelIndex++;
		
	}

	private void appendLessThanCode() {
		appendToOutputFile("// lt");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("A=M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M-D");
		appendToOutputFile("@setTrue" + labelIndex);
		appendToOutputFile("D; JLT");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=0");
		appendToOutputFile("@continue" + labelIndex);
		appendToOutputFile("0; JMP");
		appendToOutputFile("(setTrue" + labelIndex + ")");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("M=-1");
		appendToOutputFile("(continue" + labelIndex + ")");
		labelIndex++;
		
	}

	private void appendThisPopCode(String value) {
			appendToOutputFile("//pop this " + value);
			appendToOutputFile("@" + value);
			appendToOutputFile("D=A");
			appendToOutputFile("@THIS");
			appendToOutputFile("D=D+M");
			appendToOutputFile("@R13");
			appendToOutputFile("M=D");
			appendToOutputFile("@SP");
			appendToOutputFile("A=M-1");
			appendToOutputFile("D=M");
			appendToOutputFile("@SP");
			appendToOutputFile("M=M-1");
			appendToOutputFile("@R13");
			appendToOutputFile("A=M");
			appendToOutputFile("M=D");
		
	}

	private void appendThatPopCode(String value) {
		appendToOutputFile("//pop that " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@THAT");
		appendToOutputFile("D=D+M");
		appendToOutputFile("@R13");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@R13");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
	}

	private void appendConstantPushCode(String value) {
		appendToOutputFile("//Push constant " + value);
		appendToOutputFile('@' + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
		
	}
	
	private void appendTempPopCode(int value ) {
		appendToOutputFile("//pop temp " + (value + 5));
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@" + (value + 5));
		appendToOutputFile("M=D");
	}
	
	private void appendTempPushCode(int value) {
		appendToOutputFile("//push temp " + (value+5));
		appendToOutputFile("@" + (value+5));
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	private void appendStaticPopCode(String value) {
		appendToOutputFile("//pop static " + value);
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@" + labelForStaticCommands + '.' + value);
		appendToOutputFile("M=D");
		
	}

	private void appendStaticPushCode(String value) {
		appendToOutputFile("//push static " + value);
		appendToOutputFile("@" + labelForStaticCommands + '.' + value);
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	private void appendPointerPopCode(int value) {
		appendToOutputFile("//pop pointer " + (value + 3));
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@" + (value + 3));
		appendToOutputFile("M=D");
		
	}

	private void appendPointerPushCode(int value) {
		appendToOutputFile("//push pointer " + (value+3));
		appendToOutputFile("@" + (value+3));
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
		
	}
	
	private void appendLocalPushCode(String value) {
		appendToOutputFile("//push local " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@LCL");
		appendToOutputFile("A=D+M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	private void appendArgPushCode(String value) {
		appendToOutputFile("//push argument " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@ARG");
		appendToOutputFile("A=D+M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	private void appendThatPushCode(String value) {
		appendToOutputFile("//push that " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@THAT");
		appendToOutputFile("A=D+M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	private void appendThisPushCode(String value) {
		appendToOutputFile("//push this " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@THIS");
		appendToOutputFile("A=D+M");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M+1");
	}
	
	private void appendLocalPopCode(String value) {
		appendToOutputFile("//pop Local " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@LCL");
		appendToOutputFile("D=D+M");
		appendToOutputFile("@R13");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@R13");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
	}
	
	private void appendArgPopCode(String value) {
		appendToOutputFile("//pop argument " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@ARG");
		appendToOutputFile("D=D+M");
		appendToOutputFile("@R13");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@R13");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
	}
	
	//shorthand for print line
	private void appendToOutputFile (String line)
	{
		outputFile.println(line);
	}
	
	public void setOutputFile(PrintStream outputFile) {
		this.outputFile = outputFile;
	}
	//Closing the output file will occur in the VMCompiler



}
