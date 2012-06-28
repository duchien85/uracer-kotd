package com.bitfire.uracer.utils;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import com.bitfire.uracer.configuration.LaunchFlags;

public final class CommandLine {

	private CommandLine() {
	}

	private static boolean isInt( String value ) {
		if( value != null ) {
			int len = value.length();
			for( int i = 0; i < len; i++ ) {
				if( !Character.isDigit( value.charAt( i ) ) ) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	public static boolean parseLaunchFlags( String[] argv, LaunchFlags flags ) {
		int c;
		String arg;
		LongOpt[] opts = new LongOpt[ 6 ];
		opts[0] = new LongOpt( "help", LongOpt.NO_ARGUMENT, null, 'h' );
		opts[1] = new LongOpt( "resolution", LongOpt.REQUIRED_ARGUMENT, null, 'r' );
		opts[2] = new LongOpt( "no-vsync", LongOpt.NO_ARGUMENT, null, 'v' );
		opts[3] = new LongOpt( "cpusync", LongOpt.NO_ARGUMENT, null, 'C' );
		opts[4] = new LongOpt( "fullscreen", LongOpt.NO_ARGUMENT, null, 'f' );
		opts[5] = new LongOpt( "right-screen", LongOpt.NO_ARGUMENT, null, 't' );

		Getopt g = new Getopt( "URacer", argv, ":hr:vCft", opts );
		g.setOpterr( false );
		while( (c = g.getopt()) != -1 ) {
			arg = g.getOptarg();

			switch( c ) {
			case 'r':
				String[] res = arg.split( "x" );

				if( res.length == 2 && isInt( res[0] ) && isInt( res[1] ) ) {
					flags.width = Integer.parseInt( res[0] );
					flags.height = Integer.parseInt( res[1] );
				} else {
					if( arg.equals( "low" ) ) {
						flags.width = 800;
						flags.height = 480;
					} else if( arg.equals( "mid" ) ) {
						flags.width = 1280;
						flags.height = 800;
					} else if( arg.equals( "high" ) ) {
						flags.width = 1920;
						flags.height = 1080;
					} else {
						System.out.print( "Invalid resolution specified (" + arg + "), defaulting to " + (flags.width + "x" + flags.height) );
					}
				}

				break;
			case 'h':
				System.out.print( "Valid command-line options:\n" );
				System.out.print( "  -h, --help\t\tshows this help" );
				System.out.print( "  -r, --resolution=RES\tspecify the resolution to use: you can either specify" );
				System.out.print( "  \t\t\ta real resolution =, e.g. --resolution=800x600, or use " );
				System.out.print( "  \t\t\ta built-in shortcut (one of \"low\", \"mid\" or \"high\")." );
				System.out.print( "  \t\t\t(low=800x480, mid=1280x800, high=1920x1080)" );
				System.out.print( "  -v, --no-vsync\tdisable VSync" );
				System.out.print( "  -c, --cpusync\t\tenable CPU sync" );
				System.out.print( "  -f, --fullscreen\tenable fullscreen" );
				System.out.print( "  -t, --right-screen\treposition the game's window to the screen on the right,\n\t\t\tif available." );
				System.out.print( "" );
				return false;
			case 'v':
				flags.vSyncEnabled = false;
				break;
			case 'C':
				flags.useCPUSynch = true;
				break;
			case 'f':
				flags.fullscreen = true;
				break;
			case 't':
				flags.useRightScreen = true;
				break;
			case '?':
				System.out.print( "The specified parameter is not valid.\nTry --help for a list of valid parameters." );
				return false;
			case ':':
				System.out.print( "The specified argument is missing some values.\nTry --help for more information." );
				return false;
			default:
				System.out.print( "getopt() returned " + c + " (" + (char)c + ")\n" );
				return false;
			}
		}

		return true;
	}
}