#!/bin/bash


while read file; do

  outfile=`echo ${file} | sed s:jpg:jpg.exif:`
  echo ${outfile}
  exiftool -X ${file} > ${outfile}

done
