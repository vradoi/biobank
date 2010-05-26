#!/usr/bin/perl -w

use strict;
use Cwd;
use File::Find;
use File::Copy;
use Getopt::Long;




main();

sub main {

	my $verbose;

    if (!GetOptions ('verbose'  => \$verbose)) {
        die "ERROR: bad options in command line\n";
    }

    if($#ARGV+1 < 3){
		die "Usuage: translator.pl umlInputFilePath hmbProcessingDirectory outputPath [verbose=0]\n";
	}

	my $umlFile = $ARGV[0];
	my $hbmDir=$ARGV[1];
	my $outputPath = $ARGV[2];
	my %umlVarCharMap = ();
	my %umlTextMap = ();
	my @hbmDirList = ();

    #Parse the UML file
    #The key is the identifier
    #The value is the size of the varchar field
    open (FH, "<",$umlFile) or die $!;
    my %text;
    while (my $umlLine = <FH>) {
	if ($umlLine =~ m/>(.*) : VARCHAR\((\d+)\)/i) {
            $umlVarCharMap{ $1 } = $2;
	}
	if ($umlLine =~ m/>(.*) : TEXT</i) {
            $umlTextMap{$1} = 1;
	}
    }
    close(FH);

	#Saves the %umlVarCharMap hash file
	print "Generating VarCharLengths.properies... ".">".$outputPath."VarCharLengths.properties";
	open(OUTP, ">".$outputPath."VarCharLengths.properties") or die("Error: cannot open file 'VarCharLengths.properties'\n");
	print OUTP "#Found the following: \n\n";
	print OUTP "#Identifier = Varchar# \n";
	print OUTP "#--------------------------\n";
	while ( my ($key, $value) = each(%umlVarCharMap) ) {
		print OUTP "$key = $value\n";
	}
	print OUTP "#--------------------------\n\n";

    #Prints the %umlVarCharMap hash file
    if ($verbose) {
        print "Found the following: \n\n";
	print "VARCHAR fields\n";
	print "--------------------------\n";
	while ( my ($key, $value) = each(%umlVarCharMap) ) {
            print "$key = $value\n";
	}
	print "--------------------------\n\n";

	print "TEXT fields\n";
	print "--------------------------\n";
	while ( my ($key, $value) = each(%umlTextMap) ) {
            print "$key\n"
	}
	print "--------------------------\n\n";
    }

    #Browses the input directory
    #Creates an array @hbmDirList of all of the files
    #that end with the extension hbm.xml
    #find( {wanted=> \&wanted=>, no_chdir => 1}, $hbmDir );
    opendir(IMD, $hbmDir) || die("Cannot open directory");
    @hbmDirList = grep /hbm\.xml$/, readdir(IMD);
    closedir(IMD);

    #Prints the @hbmDirList array file
    if ($verbose) {
	print "Files found in directory '$hbmDir'\n";
	print "--------------------------\n";
	foreach (@hbmDirList) {
            print("$_\n");
	}
	print "--------------------------\n\n";

    }

    #Scan through each hbm.xml file in the input directory
    #Look for type="string" column="X", where X is a valid key in umlVarCharMap
    #Replace type="string" with type="VARCHAR(Y)" where Y is the value of the column key
    #Save changes in the same directory with .new appended to the file name
    my $origfname;
    my $newfname;

    my $linesChanged = 0;
    foreach (@hbmDirList) {
        $origfname = "$hbmDir/$_";
        $newfname = "$origfname.new";
	open (FO, ">", $newfname) or die $!;
	open (FH, "<", $origfname) or die $!;
	while (my $line = <FH>) {
            if (($line =~ m/<.*type="string".*column="([^"]*)"\/>/i) && ($line =~ /length="(\d+)"/i)) {
                if (exists $umlVarCharMap{ uc($1) }) {
                    my $name = $1;
                    $line =~ s/$2/$umlVarCharMap{uc($1)}/e;
                    $linesChanged++;
                    if ($verbose) {
                        print("Found line with column '$name' in umlVarCharMap\n");
                        print("\t$origfname: $line");
                    }
                }
            }
            elsif ($line =~ m/<.*type="string".*column="(.*)"\/>/i) {
                if (exists $umlVarCharMap{ uc($1) }) {
                    #if the column is found in umlVarCharMap
                    my $name = $1;
                    my $s1 = "type=\"string\"";
                    my $s2 = "type=\"string\" length=\"$umlVarCharMap{uc($1)}\"";
                    $line =~ s/$s1/$s2/e;
                    $linesChanged++;
                    if ($verbose) {
                        print("Found line with column '$name' in umlVarCharMap\n");
                        print("\t$origfname: $line");
                    }

                }
                if (exists $umlTextMap{ uc($1) }) {
                    #if the column is found in umlVarCharMap
                    my $name = $1;
                    my $s1 = "type=\"string\"";
                    my $s2 = "type=\"string\" length=\"500\"";
                    $line =~ s/$s1/$s2/e;
                    $linesChanged++;
                    if ($verbose) {
                        print("Found line with column '$name' in umlVarCharMap\n");
                        print("\t$origfname: $line");
                    }

                }
            }
            print FO $line or die $!;
	}
	close(FH);
	close(FO);
    }

    if ($verbose) {
	print("$linesChanged lines changed.\n\n");
    }

    #Remove the original files, Rename the new files
    foreach (@hbmDirList) {
        $origfname = "$hbmDir/$_";
        $newfname = "$origfname.new";
	unlink($origfname) or die $!; #move("$_","$_.old") or die $!;
	move($newfname, $origfname) or die $!;
    }
}
