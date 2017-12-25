#!/usr/bin/env python3

  
from itertools import groupby  
from operator import itemgetter  
import sys  
import math
  
def read_mapper_output(file, separator='\t'):  
    for line in file:  
        yield line.rstrip().split(separator, 1)  
  
def main(separator='\t'):  
    data = read_mapper_output(sys.stdin, separator=separator)  
    num=[]
    train=[] 
    for current_word, group in groupby(data, itemgetter(0)):  
        try:  
            doclist=""
            i=0
            num=[]
            train=[]
            for current_word,count in group:
                t=count.split(":")
                train.append(t[0])
                t[1]=float(t[1])
                num.append(t[1])
            type="negative"
            mymax=0
            mymax=num.index(max(num))
            if(train[mymax].find("negative")>=0):
                type="negative"
            elif(train[mymax].find("positive")>=0):
                type="positive"
            elif(train[mymax].find("neutral")>=0):
                type="neutral"
            
            print("%s%s%s" % (current_word, separator, str(mymax)+type)) 
        except ValueError:   
            pass  
  
if __name__ == "__main__":  
    main() 