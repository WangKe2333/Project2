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
    for current_word, group in groupby(data, itemgetter(0)):  
        try:  
            
            total_count = sum(int(count) for current_word, count in group)
            tf=float(total_count)
            if(len(current_word)>5):
                index,word=current_word.split(';')
            if(len(current_word)>5):
                doc,num=word.split(':')
            s=float(num)
            t=1500.0/(s+1)
            idf=math.log(t)
            tf_idf=tf*idf
            if(tf_idf>500):
                print("%s%s%s" % (index+";"+doc, separator, str(tf_idf))) 
        except ValueError:   
            pass  
  
if __name__ == "__main__":  
    main() 