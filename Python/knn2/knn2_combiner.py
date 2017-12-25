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
            doclist=""
            for current_word,count in group:
                doclist=doclist+count+"," 
            print("%s%s%s" % (current_word, separator, doclist)) 
        except ValueError:   
            pass  
  
if __name__ == "__main__":  
    main() 