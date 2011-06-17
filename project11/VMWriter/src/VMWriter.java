import java.io.FileNotFoundException;
import java.io.PrintWriter;



public class VMWriter {
	private PrintWriter outputFile;
	public static int labelCounter;
	public VMWriter (String outputFileName) throws FileNotFoundException
	{
		outputFile = new PrintWriter(outputFileName);
		labelCounter = 0;
	}
	public void writePush (CompilationEngine.Segment seg, int index)
	{
		switch(seg)
		{
			case CONST:
				outputFile.println("push constant " + index);
				break;
			case ARG:
				outputFile.println("push argument " + index);
				break;
			case LOCAL:
				outputFile.println("push local " + index);
				break;
			case STATIC:
				outputFile.println("push static " + index);
				break;
			case THIS:
				outputFile.println("push this " + index);
				break;
			case THAT:
				outputFile.println("push that " + index);
				break;
			case POINTER:
				outputFile.println("push pointer " + index);
				break;
			case TEMP:
				outputFile.println("push temp " + index);
				break;
		}
	}
	public void writePop (CompilationEngine.Segment seg, int index)
	{
		switch(seg)
		{
			case ARG:
				outputFile.println("pop argument " + index);
				break;
			case LOCAL:
				outputFile.println("pop local " + index);
				break;
			case STATIC:
				outputFile.println("pop static " + index);
				break;
			case THIS:
				outputFile.println("pop this " + index);
				break;
			case THAT:
				outputFile.println("pop that " + index);
				break;
			case POINTER:
				outputFile.println("pop pointer " + index);
				break;
			case TEMP:
				outputFile.println("pop temp " + index);
				break;
		}
	}
	public void writeArithmetic (CompilationEngine.Command command)
	{
		switch(command)
		{
		case ADD:
			outputFile.println("add");
			break;
		case SUB:
			outputFile.println("sub");
			break;
		case NEG:
			outputFile.println("neg");
			break;
		case EQ:
			outputFile.println("eq");
			break;
		case GT:
			outputFile.println("gt");
			break;
		case LT:
			outputFile.println("lt");
			break;
		case AND:
			outputFile.println("and");
			break;
		case OR:
			outputFile.println("or");
			break;
		case NOT:
			outputFile.println("not");
			break;
		}
	}
	public void writeLabel (String label)
	{
		outputFile.println("label "+label);
	}
	public void writeFuntion (String name, int nArgs)
	{
		outputFile.println("function " + name + " " + nArgs);
	}
	public void writeGoto( String label)
	{
		outputFile.println("goto "+label);
	}
	public void writeIf (String label)
	{
		outputFile.println("if-goto "+label);
	}
	public void writeCall (String name, int nArgs)
	{
		outputFile.println("call " + name + " " + nArgs);
	}
	public void writeReturn ()
	{
		outputFile.println("return");
	}
	public void close()
	{
		outputFile.close();
	}
}
