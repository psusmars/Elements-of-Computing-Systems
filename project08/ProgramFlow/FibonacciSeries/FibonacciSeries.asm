//push argument 1
@1
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//pop pointer 4
@SP
A=M-1
D=M
@SP
M=M-1
@4
M=D
//Push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//pop that 0
@0
D=A
@THAT
D=D+M
@R10
M=D
@SP
A=M-1
D=M
@SP
M=M-1
@R10
A=M
M=D
//Push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
//pop that 1
@1
D=A
@THAT
D=D+M
@R10
M=D
@SP
A=M-1
D=M
@SP
M=M-1
@R10
A=M
M=D
//push argument 0
@0
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//Push constant 2
@2
D=A
@SP
A=M
M=D
@SP
M=M+1
// sub
@SP
AM=M-1
D=M
@SP
A=M-1
M=M-D
//pop argument 0
@0
D=A
@ARG
D=D+M
@R10
M=D
@SP
A=M-1
D=M
@SP
M=M-1
@R10
A=M
M=D
//label MAIN_LOOP_START
(main$MAIN_LOOP_START)
//push argument 0
@0
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//ifgoto COMPUTE_ELEMENT
@SP
A=M-1
D=M
@SP
M=M-1
@main$COMPUTE_ELEMENT
D;JNE
// goto
@main$END_PROGRAM
0; JMP
//label COMPUTE_ELEMENT
(main$COMPUTE_ELEMENT)
//push that 0
@0
D=A
@THAT
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//push that 1
@1
D=A
@THAT
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
@SP
A=M-1
M=D+M
//pop that 2
@2
D=A
@THAT
D=D+M
@R10
M=D
@SP
A=M-1
D=M
@SP
M=M-1
@R10
A=M
M=D
//push pointer 4
@4
D=M
@SP
A=M
M=D
@SP
M=M+1
//Push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
@SP
A=M-1
M=D+M
//pop pointer 4
@SP
A=M-1
D=M
@SP
M=M-1
@4
M=D
//push argument 0
@0
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
//Push constant 1
@1
D=A
@SP
A=M
M=D
@SP
M=M+1
// sub
@SP
AM=M-1
D=M
@SP
A=M-1
M=M-D
//pop argument 0
@0
D=A
@ARG
D=D+M
@R10
M=D
@SP
A=M-1
D=M
@SP
M=M-1
@R10
A=M
M=D
// goto
@main$MAIN_LOOP_START
0; JMP
//label END_PROGRAM
(main$END_PROGRAM)
