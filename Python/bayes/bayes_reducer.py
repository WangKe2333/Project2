#!/usr/bin/env python3
  
from itertools import groupby  
from operator import itemgetter  
import sys  
  
def read_mapper_output(file, separator='\t'):  
    for line in file:  
        yield line.rstrip().split(separator, 1)  
  
def main(separator='\t'):   
    data = read_mapper_output(sys.stdin, separator=separator)  
    for current_word, group in groupby(data, itemgetter(0)):  
        try: 
            doclist=""
            for current_word,count in group:
                word1=current_word.split(";")
                index=word1[0]
                word2=word1.split("txt")
                doc=word2[0]
                doclist=doclist+count
            print("%s%s%s" % (doc, separator, doclist))  
        except ValueError:  
            pass  
  
if __name__ == "__main__":  
    main() 