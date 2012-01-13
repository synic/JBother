#!/bin/sh

ant && tar -jxvf macosx/osxpack.tar.bz2 && mv JBother.jar JBother.app/Contents/Resources/Java

echo
echo "Done."
echo
