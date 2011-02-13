for f in *.jpg
do
     echo "Processing $f"

     #  Convert file to png texture
     pngname=`echo $f | sed 's/\.jpg$//g'`
     convert "$f" -background "rgba(0, 0, 0, 0)" -rotate '45' -resize "100%x50%" "$pngname.png"
done
