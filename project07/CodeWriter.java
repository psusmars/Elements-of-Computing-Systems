import java.io.*;

//Class that translates VM commands into Hack assembly
//Might be using template files
public class CodeWriter {
	private static int labelIndex=0;
	private String labelForCode;
	
	//For short hand oF refers to output file
	private PrintStream outputFile;
	
	//Constructor that may open the output file/stream and gets ready to write to it
	public CodeWriter(String outputFileName)
	{
		int indexOfPeriod = outputFileName.indexOf('.');
		int indexOfLastSlash = outputFileName.lastIndexOf('\\');
		labelForCode = outputFileName.substring(indexOfLastSlash+1, indexOfPeriod); 
	}
	
	//Writes the assembly code that is the translation of the given commands, 
	//where command is either C_Push or C_pop
	
	public void writePushPop(VMCompiler.CommandType command, String [] segment)
	{
		switch (command){
		case C_PUSH:
			if(segment[1].equals("constant"))
				appendConstantPushCode(segment[2]);
			if(segment[1].equals("temp"))
				appendTempPushCode(Integer.parseInt(segment[2]));
			if(segment[1].equals("local"))
				appendLocalPushCode(segment[2]);
			if(segment[1].equals("this"))
				appendThisPushCode(segment[2]);
			if(segment[1].equals("that"))
				appendThatPushCode(segment[2]);
			if(segment[1].equals("argument"))
				appendArgPushCode(segment[2]);
			if(segment[1].equals("pointer"))
				appendPointerPushCode(Integer.parseInt(segment[2]));
			if(segment[1].equals("static"))
				appendStaticPushCode(segment[2]);
			break;
		case C_POP:
			if(segment[1].equals("temp"))
				appendTempPopCode(Integer.parseInt(segment[2]));
			if(segment[1].equals("that"))
				appendThatPopCode(segment[2]);
			if(segment[1].equals("this"))
				appendThisPopCode(segment[2]);
			if(segment[1].equals("local"))
				appendLocalPopCode(segment[2]);
			if(segment[1].equals("argument"))
				appendArgPopCode(segment[2]);
			if(segment[1].equals("pointer"))
				appendPointerPopCode(Integer.parseInt(segment[2]));
			if(segment[1].equals("static"))
				appendStaticPopCode(segment[2]);
			break;
		default:
			break;
		}
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
			appendToOutputFile("@R10");
			appendToOutputFile("M=D");
			appendToOutputFile("@SP");
			appendToOutputFile("A=M-1");
			appendToOutputFile("D=M");
			appendToOutputFile("@SP");
			appendToOutputFile("M=M-1");
			appendToOutputFile("@R10");
			appendToOutputFile("A=M");
			appendToOutputFile("M=D");
		
	}

	private void appendThatPopCode(String value) {
		appendToOutputFile("//pop that " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@THAT");
		appendToOutputFile("D=D+M");
		appendToOutputFile("@R10");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@R10");
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
		appendToOutputFile("@" + labelForCode + '.' + value);
		appendToOutputFile("M=D");
		
	}

	private void appendStaticPushCode(String value) {
		appendToOutputFile("//push static " + value);
		appendToOutputFile("@" + labelForCode + '.' + value);
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
		appendToOutputFile("@R10");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@R10");
		appendToOutputFile("A=M");
		appendToOutputFile("M=D");
	}
	
	private void appendArgPopCode(String value) {
		appendToOutputFile("//pop argument " + value);
		appendToOutputFile("@" + value);
		appendToOutputFile("D=A");
		appendToOutputFile("@ARG");
		appendToOutputFile("D=D+M");
		appendToOutputFile("@R10");
		appendToOutputFile("M=D");
		appendToOutputFile("@SP");
		appendToOutputFile("A=M-1");
		appendToOutputFile("D=M");
		appendToOutputFile("@SP");
		appendToOutputFile("M=M-1");
		appendToOutputFile("@R10");
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
