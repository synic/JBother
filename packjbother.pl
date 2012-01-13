#!/usr/bin/perl

open( FILE, "src/com/valhalla/jbother/JBother.java" );
@index = <FILE>;
close( FILE );

$version = "";
foreach $line( @index )
{
	if( $line =~ /public static final String JBOTHER_VERSION \= \"(.*)\"\;/ )
	{
		$version = $1;
	}
}

if( !$version )
{
	print "Could not determine version.\n";
	exit( -1 );
}

chdir( ".." );
system( "find . -name \"*.class\" -exec rm {} \\;" );
system( "rm -rf JBother/build" );
system( "cp JBother/JBother.jar JBother-$version\.jar" );
unlink( "JBother/JBother.jar" );
system( "zip -r JBother-$version\.zip JBother/ -x \"\*\.svn\*\"" );

print "Done.\n\n";
exit;
