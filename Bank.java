/**
 * @author	Dominik Suwala <dxs9411@rit.edu>
 * Date:	2017-01-24
 * Implement a simple single-user "bank account" terminal for Jaroop challenge
 * 
 */


import java.util.Scanner;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class Bank {
	
	/**
	 * 
	 * @param document - the entire HTML document
	 * @return - balance of deposits and withdrawals
	 */
	public static BigDecimal computeBalance( List< String > inputArrayList ) {
		
		int endIndex = Integer.parseInt( inputArrayList.get( inputArrayList.size() - 1 ) );
		BigDecimal curTotal = new BigDecimal( "0" );
		
		int i = endIndex;
		while( true ) {
			String curLine = inputArrayList.get( i );
			if( curLine.contains( "<tr><td>" ) ) {
				curLine = curLine.replace( "<tr><td>", "" )
						.replace( "</td></tr>", "" ).trim();
				curTotal = curTotal.add( new BigDecimal( curLine ) );
			}
			else {
				return curTotal;
			}
			i--;
		}
		
	}
	
	/**
	 * 
	 * @param path - filepath
	 * @param curState - ArrayList containing document to write
	 */
	public static void writeFile( String path, List< String > curState ) {
		
		String saveStr = "";
		String newline = "";
		
		for( int i = 0; i < curState.size() - 1; i++ ) {
			saveStr += newline + curState.get( i );
			newline = "\n";
		}
		
		try {
			Files.write( Paths.get( new File( path ).getAbsolutePath() ), saveStr.getBytes() );
		}
		catch( Exception e ) {
			System.err.println( "Failed to write to file." );
			e.printStackTrace();
			System.exit( 0xBAD );
		}
	}
	
	/**
	 * 
	 * @return - data file is returned as a newline-separated ArrayList
	 */
	public static List< String > fileAsArrayList() {
		
		List< String > returnable = new ArrayList< String >();
		
		// Read data file to a string
		File myHTML = new File( "log.html" );
		if( !myHTML.exists() ) {
			System.err.println( "File \"log.html\" not found. Program terminating." );
			System.exit( 0xBAD );
		}
		
		int lastRow = -1;
		String myLog = "";
		
		try {
			myLog = readFile( myHTML.getAbsolutePath(), Charset.defaultCharset() );
			
		} catch ( IOException e ) {
			System.err.println( "File \"log.html\" not found. Program terminating." );
			System.exit( 0xBAD );
		}
		int i = 0;
		
		for( String line : myLog.split( "\n" ) ) {
			
			returnable.add( line );
			if( line.contains( "<tr><td>" ) ) {
				lastRow = i;
			}
			i++;
		}
		returnable.add( "" + lastRow );
		return returnable;
	}
	
	/**
	 * 
	 * @param curState - ArrayList containing entire document
	 * @param transactionAmount - Transaction to do (deposit/withdraw)
	 */
	public static void addTransaction( List< String > curState, BigDecimal transactionAmount ) {
		int addIndex = Integer.parseInt( curState.get( curState.size() - 1 ) ) + 1;
		String line = "                <tr><td>" + transactionAmount + "</td></tr>";
		try {
			curState.add( addIndex, line );
		}
		catch( Exception e ) {
			
		}
		
	}
	
	/**
	 * 
	 * @param path - absolute path to file
	 * @param encoding - character encoding of file
	 * @return - file as a string is returned
	 * @throws IOException
	 */
	public static String readFile( String path, Charset encoding ) throws IOException {
		byte[] encoded = Files.readAllBytes( Paths.get( path ) );
		return new String( encoded, encoding );
	}
	
	
	/**
	 * @param args - none implemented
	 */
	public static void main( String[] args ) {
		// Scanner reads from stdin
		Scanner stdin = new Scanner( System.in );
		DecimalFormat moneyFormat = new DecimalFormat( "$#,###.##" );
		
		// Initializing user options and program continuity operations
		boolean cont = true;
		String curChoice = "";
		List< String > choices = new ArrayList< String >();
		String[] arr = { "deposit", "withdraw", "balance", "exit" };
		
		for( String str: arr ) {
			choices.add( str );
		}
		
		List< String > curState = fileAsArrayList();
		
		// Continuous loop
		while( cont ) {
			curChoice = "";
			System.out.printf( "Please enter in a command (Deposit, Withdraw, Balance, Exit): " );
			curChoice += stdin.nextLine().toLowerCase();
			
			// Sanity check on user input
			if( !choices.contains( curChoice ) ) {
				System.err.println( curChoice + " is not a valid option" );
				continue;
			}
			
			// The case of exiting the program
			else if( curChoice.equals( "exit" ) ) {
				System.out.println( "Terminating program gracefully." );
				// writeFile( myHTML );
				System.exit( 0x00 );
			}
			
			// The case of asking for the balance
			else if( curChoice.equals( "balance" ) ) {
				// System.out.println( computeBalance( curState ) );
				System.out.printf( "The current balance is: %s%n", moneyFormat.format( computeBalance( curState ) ) );
			}
			
			// The case of withdraw / deposit action
			else {
				while( true ) {
					System.out.printf( "Please enter an amount to %s: ", curChoice );
					String curInput = "" + stdin.nextLine();
					BigDecimal curInputBD;
					if( !curInput.contains( "." ) ) {
						System.err.println( "Entry is missing a decimal point." );
						continue;
					}
					else {
						if( curInput.split( "\\." )[ 1 ].length() != 2 ) {
							System.err.println( "Entry must contain two decimal places." );
							continue;
						}
						if( curInput.contains( "-" ) ) {
							System.err.println( "Invalid data: negative amount was entered." );
						}
					}
					
					try {
						curInputBD = new BigDecimal( curInput );
					}
					catch( Exception e ) {
						System.err.println( "Entry is not a number." );
						continue;
					}
					
					if( curChoice.equals( "withdraw" ) ) {
						curInputBD = curInputBD.negate();
					}
					// Add in memory
					addTransaction( curState, curInputBD );
					// Write to file
					writeFile( "log.html", curState );
					// Read back file
					curState = fileAsArrayList();
					break;
				}
				
			}
			
		}
		
		stdin.close();
	}
}