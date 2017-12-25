#!/usr/bin/env python3

import sys
import os


for line in sys.stdin:
    line = line.strip()
    strarray=line.split("\t")
    word1=strarray[0]
    line2=strarray[1]
    strarray2=line2.split("\t")
    t=str(len(strarray2))
    for str1 in strarray2:
    	print("%s\t%s" % (word1+";"+str1,t))