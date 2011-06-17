	@KBD
	M=0
	@8191
	D=A
	@counter  //Set the counter to the max value the screen pixels are
	M=D
	@SCREEN
	D=A
	@addrofPixels  //Sets the variable to the first pixel address
	M=D
(WAITING)
	@KBD
	D=M
	@FILL
	D;JNE 
	//If a key isnt pressed, then just fall into reseting back to white pixels
	@addrofPixels
	A=M
	M=0 //Sets the pixel at the current address of pixels to 0 for white
	@addrofPixels
	M=M+1 //increments to the next address of pixel

	@counter
	D=M  //Sets D to the current counter of the pixels
	M=D-1  //Decreases the counter
	@WAITING
	D; JGE //Jumps back to waiting if the counter still needs to decrement

	@8191 //Resets the counter variable if done decrementing
	D=A //Reset the counter variable
	@counter
	M=D

	@SCREEN
	D=A
	@addrofPixels  //Sets the variable to the first pixel address
	M=D

	@WAITING
	0;JMP  //After reseting the counter variable jump back unconditionally to waiting
(FILL)
	@addrofPixels
	A=M
	M=-1 //Sets the pixel at the current address of pixels to -1
	@addrofPixels
	M=M+1 //increments to the next address of pixel

	@counter
	D=M  //Sets D to the counter of the pixels
	M=D-1  //Decreases the counter
	@WAITING
	D; JGE //Jumps back to waiting if the counter still needs to decrement

	@8191 
	D=A //Reset the counter variable
	@counter
	M=D

	@SCREEN
	D=A
	@addrofPixels  //Sets the variable to the first pixel address
	M=D

	@WAITING
	0;JMP  //After reseting the counter variable jump back unconditionally to waiting
(END)
	@END
	0;JMP
