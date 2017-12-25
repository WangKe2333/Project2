#!/usr/bin/env python3

import sys
import os


index=0

for line in sys.stdin:
    line = line.strip()
    strarray=line.split("\t")
    line2=strarray[1]
    strarray2=line2.split(';')
    t=len(strarray2)
    index=index+1
    for str1 in strarray2:
    	#a=str.split(":")
    	if(len(str1)>0):
            print('%s\t%s' % (str(index)+";"+str1, str(t)))