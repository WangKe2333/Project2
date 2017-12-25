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
            index,doc1=current_word.split(';',1)
            doc2=current_word.split(';',1)[1]
            #doc2,txt=current_word.split('.',1)
            doclist=""
            for current_word,count in group:
                doclist=doclist+count
            print("%s%s%s" % (doc1[:-4], separator, index+":"+doclist))  
        except ValueError:  
            pass  
  
if __name__ == "__main__":  
    main() 