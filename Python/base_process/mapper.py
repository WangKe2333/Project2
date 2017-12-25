#!/usr/bin/env python3

import sys
import os
import jieba

# 分词
filepath = os.environ["map_input_file"]
fileparent=os.path.split(filepath)[0]
filename1 = os.path.split(fileparent)[1]
filename2=os.path.split(filepath)[1]

for line in sys.stdin:
    line = line.strip()
    words = jieba.cut(line)
    for word in words:
        if(word >= u'\u4e00' and word <= u'\u9fa5'):
            print('%s\t%s' % (word+":"+filename1+filename2, "1"))