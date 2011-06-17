public class Token {
	private String [] theTokenizedFile;
	private int currIndex;
	private String currToken;
	public Token(String[] tokenizeTheJackFile) {
		currIndex = 0;
		theTokenizedFile = tokenizeTheJackFile;
		currToken = theTokenizedFile[currIndex];
	}

	public boolean hasMoreTokens()
	{
		if(currIndex < theTokenizedFile.length )
			return true;
		else
			return false;
	}

	public void advance() {
		currToken = theTokenizedFile[currIndex];
		currIndex++;	
	}

	public String getCurrToken() 
	{
		return theTokenizedFile[currIndex];
	}

	//Precondition: check that the next index is valid
	public String peek()
	{
		return theTokenizedFile[currIndex+1];
	}

	public void backTrack()
	{
		currIndex--;
	}
}
