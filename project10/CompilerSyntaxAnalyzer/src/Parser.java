import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;


public class Parser {
	private ArrayList<String> inputFileNames;
	private String outputFileName;
	private String jackLineOfCode, fullFileAsString;
	private boolean readingMultipleLinesOfComments;
	Parser ( ArrayList<String> fileNames)
	{
		inputFileNames = fileNames;
		readingMultipleLinesOfComments = false;
		fullFileAsString = "";
	}

	//Opens the files for coding, and gets ready to parse it
	void openFilesAndExecute ()
	{
		String readLine;

		for(int i=0; i<inputFileNames.size(); ++i)
		{				
			outputFileName = inputFileNames.get(i).replace(".jack", "T.ours.xml");


			try {
				//Opens the file reader and the scanner for the current file
				FileReader jackFile = new FileReader(inputFileNames.get(i));
				Scanner in = new Scanner(jackFile);
				while (in.hasNextLine())
				{

					readLine = in.nextLine();
					prepareInputStringAndStoreInJackLineOfCode(readLine);
					if(!jackLineOfCode.equals(""))
						fullFileAsString+=jackLineOfCode + " ";
				}
				insertWhiteSpaceIntoFullFileAsString();

				JackTokenizer jt = new JackTokenizer (outputFileName, fullFileAsString);

				jt.startCompilationEngine();
				fullFileAsString = "";
			}catch (Exception e){
				System.out.println("Something went terribly wrong with opening the input file, I blame Yong!");
			}
		}
	}

	private void insertWhiteSpaceIntoFullFileAsString() {
		JackTokenizer temp = new JackTokenizer();
		String i = temp.getASymbol();
		while(!(i.equals("done")))
		{
			if(fullFileAsString.contains(i))
			{
				fullFileAsString = fullFileAsString.replace(i, " " +i+ " " );
			}
			i = temp.getASymbol();
		}
		if(fullFileAsString.contains("\""))
			fullFileAsString = fullFileAsString.replace("\"", " " +"\""+ " " );
	}

	private void prepareInputStringAndStoreInJackLineOfCode(String readLine) {
		String trimmedLine = readLine.trim();
		if(trimmedLine.contains("//"))
		{
			int indexOfComments = trimmedLine.indexOf("//");
			trimmedLine = trimmedLine.substring(0, indexOfComments);
			trimmedLine = trimmedLine.trim();
		}
		if(trimmedLine.contains("/*") && trimmedLine.contains("*/"))
			trimmedLine = "";
		else{
			if(trimmedLine.contains("/*") && readingMultipleLinesOfComments == false)
			{
				trimmedLine = "";
				readingMultipleLinesOfComments = true;
			}
			if(trimmedLine.contains("*/") && readingMultipleLinesOfComments == true)
			{
				trimmedLine = "";
				readingMultipleLinesOfComments = false;
			}
			else
				if(readingMultipleLinesOfComments == true)
					trimmedLine = "";
		}
		if(trimmedLine.contains("\""))
		{
			int indexOfFirst = trimmedLine.indexOf('\"');
			int indexOfSecond = trimmedLine.indexOf('\"', indexOfFirst +1);
			String sub = trimmedLine.substring(indexOfFirst,indexOfSecond+1);
			String tempSub = sub.replace(' ', '`');
			trimmedLine = trimmedLine.replace(sub,tempSub);
		}
		jackLineOfCode = trimmedLine;
	}

	public static void main(String[] args) {
		if(args.length == 1)
		{
			ArrayList<String> files = new ArrayList<String>();
			if(args[0].endsWith(".jack"))
			{
				files.add(args[0]);
				Parser compiler = new Parser (files);
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
						if(listOfFiles[i].toString().endsWith(".jack"))
							files.add(listOfFiles[i].toString());
					}
					Parser compiler = new Parser (files);
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