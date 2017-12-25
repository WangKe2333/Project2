#!/usr/bin/env python3

import sys
import os
import math



def read(filepath):
	file=open(filepath,'r')
	lines=file.readlines()
	return lines

sum=0
lines=read("/Users/wangke/Desktop/train2")
for line in sys.stdin:
    line = line.strip()
    strarray=line.split("\t")
    doc=strarray[0]
    line2=strarray[1]
    a=line2.split(",")
    myindex=[]
    mytfidf=[]
    for x in a:
    	if(len(x)>2):
    	    y=x.split(":")
    	    myindex.append(y[0])
    	    mytfidf.append(y[1])
    train2=[]
    train1=[]
    for train in lines:
    	sum=0
    	if(train.find("\t")>0):
    	    train1=train.split("\t")
    	    train2=train1[1].split(",")
    	trainindex=[]
    	traintfidf=[]
    	for z in train2:
    		if(len(z)>2):
    		    tmp=z.split(":")
    		    trainindex.append(tmp[0])
    		    traintfidf.append(tmp[1])
    	for v in range(0,len(myindex)):
    	    flag=0
    	    for u in range(0,len(trainindex)):
    		    if(myindex[v]==trainindex[u]):
    			    sum=sum+math.pow(float(traintfidf[u])-float(mytfidf[v]),2)
    			    flag==1
    	    if(flag==0):
    		    sum=sum+math.pow(float(mytfidf[v]),2)
    	print('%s\t%s' % (doc, train1[0]+":"+str(sum)))