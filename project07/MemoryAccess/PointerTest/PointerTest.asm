//Push constant 3030
@3030
D=A
@SP
A=M
M=D
@SP
M=M+1
//pop pointer 3
@SP
A=M-1
D=M
@SP
M=M-1
@3
M=D
//Push constant 3040
@3040
D=A
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
//Push constant 32
@32
D=A
@SP
A=M
M=D
@SP
M=M+1
//pop this 2
@2
D=A
@THIS
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
//Push constant 46
@46
D=A
@SP
A=M
M=D
@SP
M=M+1
//pop that 6
@6
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
//push pointer 3
@3
D=M
@SP
A=M
M=D
@SP
M=M+1
//push pointer 4
@4
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
//push this 2
@2
D=A
@THIS
A=D+M
D=M
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
//push that 6
@6
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
