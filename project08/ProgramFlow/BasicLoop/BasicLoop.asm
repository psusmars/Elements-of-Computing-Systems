//Push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//pop Local 0
@0
D=A
@LCL
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
//label LOOP_START
(LOOP_START)
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
//push local 0
@0
D=A
@LCL
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
//pop Local 0	
@0	
D=A
@LCL
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
//ifgoto LOOP_START
@SP
A=M-1
D=M
@SP
M=M-1
@LOOP_START
D;JNE
//push local 0
@0
D=A
@LCL
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
