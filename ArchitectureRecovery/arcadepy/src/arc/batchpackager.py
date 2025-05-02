'''
Created on Jul 24, 2014

@author: joshua
'''

import argparse, os
import packager

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--startdir', type=str, required=True)
    parser.add_argument('--outputfile', type=str, required=True)
    parser.add_argument('--pkgprefixes', type=str, nargs='+', required=True)
    args = vars(parser.parse_args())
    
    startdir = args['startdir']
    pkgprefixes = args['pkgprefixes']
    outputfile = args['outputfile']

    for root, dirs, files in os.walk(startdir):
        print "root: ", root
        for dir in dirs:
            print "  dir: ", dir
        for file in files:
            if file.endswith("deps.rsf"):
                print "  dep rsf file: ", file
                filenameTokens = file.split(".")
                filePrefix = filenameTokens[:-1]
                fileSuffix = filenameTokens[-1]
                packagerArgs = "--pkgprefixes " + ' '.join(pkgprefixes) + " --infile " + root + os.sep + file + " --outfile " + outputfile + os.sep + 'pkg_clusters.rsf'
                packager.main(packagerArgs.split(' '))
    print "all pkg files are saved in " + outputfile
