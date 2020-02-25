import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List; 
import java.util.*;
import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.Duration;

/**
 * 
 * Compress Image and other Files
 * Date: 02/22/20
 * @author benjaSantana
 * 
 */

public class ImageCompressor {

	/*
	 *
	 * The way this compressor it's going to work it's pretty simple. We will scan the given file for patterns,
	 * unfortunately with larger files this might get a little slow. Thats because in order to find the pattern 
	 * out algorithm has a compelxity O(n²). Later I'll explain the algorithm better.
	 *
	 * Then, after finding the pattern, we store it in a file, and then proceed to store the index of the first
	 * element in the pattern in the same file. We call that file index.in.
	 * 
	 * After that we remove all the patterns that were found. (We can safely do this, because we already have the pattern
	 * and all the index in wich each pattern start.)
	 *
	 * We store the remaining bytes in a file call file.bsv.
	 *
	 * And last we move the two files (file.bsv, index.in) to a directory named file.bs
	 *
	 *
	 */
	public static void main(String[] args){
		
		
		int lengthPattern = 12; /*The number of bytes that will be in the pattern*/
		int leastPatternsFound = 10; /* The least number of patterns the algorithm most find in order to use it*/
		
		// Checks the program has recived just one file.
		if(args.length != 1){

			System.out.println("Please provide input file (Ex. java ImageCompressor file.bmp)");
			System.exit(0);

		}	

		//We assing the file to a string, and read the file 
		String inputFile = args[0];

		/*
		 * We store the name of the file without the last 3 characters (bmp) in order for us two 
		 * save the new one with the same name just different extension.
		 *
		 */
		char[] outN = new char[inputFile.length()-3];
		for(int c=0; c<inputFile.length()-3; c++){
			outN[c] = inputFile.charAt(c);
		
		}
		String outputFile = new String(outN);
		/*
		 * We create the directory with the same name as the file just different extension.
		 */
		String outputDir = new String("./" + outputFile+ "bs"); 
		File dir = new File(outputDir);
		dir.mkdir();
		
		//Read and Create both files
		try(
			InputStream inputStream = new FileInputStream(inputFile);
			OutputStream outputStream = new FileOutputStream(outputDir + "/" + outputFile + "bsv");

			
		   ){
			

			long fileSize = new File(inputFile).length(); /*Length of all the bytes that are on the bmp*/
			byte[] allBytes = new byte[(int)fileSize]; /* An array of bytes that will store all the bytes of the file*/
			inputStream.read(allBytes); /* Read the bytes and store them in allBytes*/
			

			/*
			 *
			 * Create a list of integers, they'll be the index that we store in the file mentioned at the start of the code
			 *
			 */
			List<Integer> indexList = new ArrayList<Integer>();
			Instant start = Instant.now(); /* Just to check the time */

			/*
			 * Here's where the magic happens the mehtod downbellow find the pattern, more info at the method
			 *
			 */
			indexList = FindPattern(allBytes.clone(), allBytes.length, lengthPattern, leastPatternsFound, outputDir);
			Instant end = Instant.now(); /* Checks execution time */
			System.out.println(Duration.between(start, end));  /*print duration*/

			/*
			 *
			 * Then we will copy the array of bytes into an arraylist for us to remove the pattern in a simpler way
			 *
			 */
			List<Byte> listBytes = new ArrayList<Byte>();
			for(byte byt: allBytes){
			
				listBytes.add(byt);
			}

			/*
			 * Remove by index all the patterns found. 
			 *
			 */
			for(int index: indexList){

			
				for(int m=0; m<lengthPattern; m++){
					
					if(index+m<listBytes.size()){
						listBytes.remove(index+m);
					}

				}
			
			}			

			/*
			 * Pass from an ArrayList to an Array of chars in order to write them into the final file. 
			 *
			 */
			byte[] compressedBytes = new byte[listBytes.size()];
			for(int j=0; j<listBytes.size(); j++){

				compressedBytes[j] = listBytes.get(j);	
			
			
			}


			/*
			 *
			 * Store the array into a new file.
			 *
			 */

			outputStream.write(compressedBytes);


		   } catch (IOException ex){
			
			   ex.printStackTrace();

		   }





	}

	/**
	 * ** This was used in other version of the compressor ** 
	 *
	 * Returns the most common byte in an array
	 *
	 * @param byt[] the array of bytes for us to do the operation;
	 * @param lengthBytes the length of the array
	 *
	 */
	public static byte MostCommonByte(byte byt[], int lengthBytes){

		//Sort the array
		Arrays.sort(byt);
		
		byte commonValue = byt[0];
	
		int mostCommon = 1;
		int currCommon = 1;

		/*
		 *
		 * We'll go element by element
		 * First we check if the element it's equal to the last one 
		 * if it is we add to currCommon 1
		 * if not we compare mostCommon with currCommon
		 * The one that's higher stays as mostCommon 
		 *
		 */
		for(int i=1; i<lengthBytes; i++){

			if(byt[i] == byt[i-1]){
			
				currCommon++;
	
			}else{

				if(currCommon > mostCommon){
					mostCommon = currCommon;
					commonValue = byt[i];
				
				}

				currCommon = 1;
			}

		}

		//Check if the last element is the most common
		if(currCommon > mostCommon){

			mostCommon = currCommon;
			commonValue = byt[lengthBytes-1];

		}

		System.out.println(mostCommon);
		return commonValue;

	}
	
	/**
	 * Writes in a file the pattern and the index of the first element in the patten, in an array of bytes
	 *
	 * Returns a list of integers containing de index of the first value of each pattern so later (in the main) remove it from the array,
	 * It also writes in a file the bytes of the pattern that was found.
	 *
	 * @param byt[] Array of bytes that represent the whole file
	 * @param bytLength The length of this array
	 * @param lengthPattern The length of the pattern you want to find
	 * @param leastPatternFound The least number of patterns the algorithm most find in order to use it*
	 * @param outputDir The name of the directory that the "index.in" file  will be saved in.
	 *
	 */
	public static List<Integer> FindPattern(byte byt[], int bytLength, int lengthPattern, int leastPatternsFound, String outputDir){

		int numConcurrency = 1; /* The number of times the values of the two arrays comparing are the same*/
		boolean patternFound = false; /* It's true if it already has find a pattern*/
		int indexOfPattern = 0; /**/
		int contPatterns = 0; /*How many patterns of a type has found*/
		System.out.println(bytLength);
	

		/*
		 * We will search in blocks of lengthPattern (Ex. 12) elements and compare them with all the array
		 * if we find that the pattern repeats at least 10 time we'll break that O(n²) for, and proceed to search for
		 * that particular patern in a for thats only O(n) 
		 *
		 *
		 */	
		for(int i=0; i<bytLength-1; i++){
		
			for (int j=0; j<bytLength-i-1; j++){
			
				for (int w=0; w<lengthPattern; w++){
				
					if(i+w < bytLength && j+w < bytLength){				
					
						if(byt[i+w] == byt[j+w]){
							
							numConcurrency++;	
						


						}
					}

				}
				/*
				 * If the nuber of concurrencies equals the asked length of the pattern, it means 
				 * we found to strings in the array that match, so a pattern is found.
				 *
				 * But if we only found 1 it won't save a lot of memory so it has to find at leat 10 that match
				 *
				 */
				if(numConcurrency == lengthPattern){
					
				//	System.out.println("Found a Pattern");
					contPatterns++; /* It has two find at least 10 patterns for it to be woth*/
					if(contPatterns>leastPatternsFound){
				//		System.out.println("index:" + Integer.toString(i));
				//		System.out.println("indexCon:" + Integer.toString(j));
						indexOfPattern = i;
						patternFound = true;
						break;	
					}
			
				}			
				numConcurrency = 1; /*Reset the number of concurrencies*/
			
			}
			contPatterns = 1; /*There wasn't enough patterns found so reset the counter of patterns*/

			if(patternFound){
				
				// If the patterns meet up with the leastPatternsFound
				break;
			}
		}	


		List<Integer> listIndex = new ArrayList<Integer>(); /* Makes a list of integers in wich we store the index of the first number in each pattern*/
		listIndex.add(indexOfPattern); /*Store the first index of pattern*/
		int concurrencies = 1; 
		if(patternFound){
			
			/* Search for the pattern in the array, but here the complexity it's just O(n), we do this for storing the index and to find 
			 * if we can find another repetition of the pattern 
			 * 
			 */
			for(int i=0; i<bytLength; i++){

				
				for(int j=0; j<lengthPattern;j++){
					if(j+i < bytLength){
						if(byt[indexOfPattern+j] == byt[i+j]){

							concurrencies++;							

						}	
					}	

				}	

				if(concurrencies == lengthPattern){
					
					listIndex.add(i); /* Add the index of the pattern found in the listIndex array*/

				}
				
				concurrencies = 1;

			}



		}
		
		//System.out.println(listIndex);
		
		/*
		 * Write the file index.in, in there the pattern and the starting index of each pattern will be stored
		 *
		 */
		try{
		FileWriter indexFily = new FileWriter(outputDir + "/index.in");
		for(int i=indexOfPattern; i<indexOfPattern+lengthPattern; i++){
			
			indexFily.write((Byte.toString(byt[i]))+ ",");	/* Write the pattern in the file.in*/
			
		} 
		indexFily.write("'");	
		for(int index: listIndex){

			indexFily.write(Integer.toString(index) + " "); /* Write the index in the file.in*/


		}	
			
		indexFily.close(); //close file
	        } catch (IOException e) {
		            System.out.println("An error occurred.");
        		    e.printStackTrace();

        	}

		return (listIndex);
	}

	

} 
