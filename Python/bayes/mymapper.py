#!/usr/bin/env python3

import sys
import os


for line in sys.stdin:
    line = line.strip()
    strarray=line.split("\t")
    doc=strarray[0]
    line2=strarray[1]
    print('%s\t%s' % (doc, line2))