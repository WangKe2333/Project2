#!/usr/bin/env python3

import sys
import os


def read(filepath):
	file=open(filepath,'r')
	lines=file.readlines()
	return lines

sum=0
lines=read("/Users/wangke/Desktop/bayes")
for line in sys.stdin:
    line = line.strip()
    strarray=line.split("\t")
    doc=strarray[0]
    line2=strarray[1]
    a=line2.split(",")
    myindex=[]
    mynum=[]
    for x in a:
    	tmp=x.split(":")
    	if(len(tmp)>1):
    		myindex.append(tmp[0])
    		mynum.append(tmp[1])

    sum_negative=0
    sum_neutral=0
    sum_positive=0
    for y in lines:
    	train3=y.split("\t")
    	train2=train3[1].split(":")
    	for v in range(0,len(myindex)):
    		if(train3[0].find(myindex[v]!=-1)):
    			sum_negative=sum_negative+float(mynum[v])*float(train2[0])
    			sum_neutral=sum_neutral+float(mynum[v])*float(train2[1])
    			sum_positive=sum_positive+float(mynum[v])*float(train2[2])
    max=sum_negative
    type="negative"
    if(max<sum_neutral):
    	type="neutral"
    	max=sum_neutral
    if(max<sum_positive):
    	type="positive"
    print("%s\t%s" % (doc,type))