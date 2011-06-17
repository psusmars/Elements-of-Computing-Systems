	@i //Sets the loop iterator to 0
	M=0
	@R2  //Loads R2
	M=0 //This starts the product to 0
(Mult)
	@i
	M=M+1
	@i
	D=M
	@R1
	D=D-M //Subtracts multiplier from loop iterator
	@End
	D;JGT //If the multiplication is done D-M >=0
	@R0 //Loads Multiplicand
	D=M
	@R2  //Loads R2
	M=M+D  //This adds one level of the multiplicand R2=Multiplacan+R2
	@Mult
	0;JMP

(End)
	@End
	0;JMP