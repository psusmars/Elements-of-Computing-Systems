//Push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
//Push constant 17
@17
D=A
@SP
A=M
M=D
@SP
M=M+1
//eq
@SP
M=M-1
A=M
D=M
@SP
A=M-1
D=M-D
@setTrue0
D; JEQ
@SP
A=M-1
M=0
@continue0
0; JMP
(setTrue0)
@SP
A=M-1
M=-1
(continue0)
//Push constant 892
@892
D=A
@SP
A=M
M=D
@SP
M=M+1
//Push constant 891
@891
D=A
@SP
A=M
M=D
@SP
M=M+1
// lt
@SP
M=M-1
A=M
D=M
@SP
A=M-1
D=M-D
@setTrue1
D; JLT
@SP
A=M-1
M=0
@continue1
0; JMP
(setTrue1)
@SP
A=M-1
M=-1
(continue1)
//Push constant 32767
@32767
D=A
@SP
A=M
M=D
@SP
M=M+1
//Push constant 32766
@32766
D=A
@SP
A=M
M=D
@SP
M=M+1
// gt
@SP
M=M-1
A=M
D=M
@SP
A=M-1
D=M-D
@setTrue2
D; JGT
@SP
A=M-1
M=0
@continue2
0; JMP
(setTrue2)
@SP
A=M-1
M=-1
(continue2)
//Push constant 56
@56
D=A
@SP
A=M
M=D
@SP
M=M+1
//Push constant 31
@31
D=A
@SP
A=M
M=D
@SP
M=M+1
//Push constant 53
@53
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
//Push constant 112
@112
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
// neg
@SP
A=M-1
M=-M
// and
@SP
AM=M-1
D=M
@SP
A=M-1
M=D&M
//Push constant 82
@82
D=A
@SP
A=M
M=D
@SP
M=M+1
// and
@SP
AM=M-1
D=M
@SP
A=M-1
M=D|M
