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
 * Decompress Image and other Files
 * Date: 02/22/20
 * @author benjaSantana
 * 
 */
public class ImageDecompressor
{
	/**
	 *
	 * Decompress the image and save it as a bmp. 
	 * There's no trick here we just read the file in which the pattern and the indexes have been saved and reinsert them in the image. 
	 *
	 * @param args The name of the dir where the .bsv and the .in are saved 
	 *
	 */
    	public static void main( String[] args) {
        
		/* if there are less arguments that the code need it displays helpfull info of how to enter them*/
		if (args.length != 1) {
            		System.out.println("Please provide input dir (Ex. java ImageDecompressor ./dir.bs)");
            		System.exit(0);
        	}
		
        	String inputFile = args[0]; /* Store in a string the name of the dir*/
        	
		

		char[] inName = new char[inputFile.length() - 1];
		for (int i = 1; i < inputFile.length(); ++i) {
            		inName[i-1] = inputFile.charAt(i); /* Remove the first "." in "./file.bs"*/
        	}
        	

		
		char[] outN = new char[inputFile.length() - 3];
		for (int i = 0; i < inputFile.length() - 3; ++i) {
            		outN[i] = inputFile.charAt(i); /* Store the name of the file*/
        	}
        	
		/* Pass from a char of arrays to a string*/
		String inputName = new String(inName);
		String outputName = new String(outN);
		//System.out.println(inputFile+inputName+"v");
		
		File path = new File(inputFile+inputName+"v"); /* Store the path to the file*/

        	try(
			
			InputStream inputStream = new FileInputStream(path); /* open file */
			OutputStream outputStream = new FileOutputStream(inputFile+"/" +outputName + ".bmp"); /* Create the new bmp file */

			
		   ){

			long fileSize = path.length(); /* Get the size of the file */
			byte[] allBytes = new byte[(int)fileSize]; /* Store all the bytes in the allBytes array*/
			inputStream.read(allBytes); /*Save the file in the array of bytes*/

			/*
			 *
			 * We will copy the array of bytes into an arraylist for us to manage the most common byte more easily
			 *
			 */
			List<Byte> listBytes = new ArrayList<Byte>();
			for(byte byt: allBytes){
			
				listBytes.add(byt);
			}
				
			List<Character> listChar = new ArrayList<Character>(); /*List of characters in which the document will be saved*/ 
			listChar = OpenIndexFile(inputFile); /* Call the method that reads the file and store it in the array list we did in line 84*/ 
			/*
			 *
			 * The file is divided in two parts, the part of the pattern separated by commas, and the part of the index, separated by spaces
			 * so in bytesPattern we store the pattern
			 * and in indexInt the index
			 */

			List<Byte> bytesPattern = new ArrayList<Byte>(); 
			List<Integer> indexInt = new ArrayList<Integer>();
			
			boolean isPattern = true;/* Once we passed by all the chars of the pattern this will be false*/
			
			StringBuilder number = new StringBuilder(); /* We will use this two create the numbers, because they are characters and we have two
									append them before casting them to int*/			

			for(int j=0; j<listChar.size(); j++){

					
				/* Passing by the pattern, and saving it in the arraylist bytesPattern, as byte*/
				if(listChar.get(j) == ',' && isPattern){
					
					String num = number.toString();
					int w = Integer.parseInt(num);
					bytesPattern.add((byte)w);
					number = new StringBuilder();

					continue;
				}

				/*Pattern end*/
				if(listChar.get(j) == '\''){
					
					continue;
				}
				/*Passing by the index, and saving in the arraylist indexInt*/
				if(listChar.get(j) == ' '){

					isPattern = false;
					String num = number.toString();
					int w = Integer.parseInt(num);
					indexInt.add(w);
					number = new StringBuilder();
					
					continue;
				}

				number.append(listChar.get(j));
			}

			int patternLength = bytesPattern.size(); /*Store the length of the pattern*/

			/*
			 * Here we insert in the array of bytes the patterns that were mising
			 * We'll use the add method of the arraylist, sending it the index and the byte to 
			 * add.
			 *
			 */
	
			int position = 0;
			byte content = 0;
		
			/*
			 * We will insert them using two for loops, the first moves across the list of indexInt
			 * the second one across the pattern
			 *
			 * So we can add in the index+j, the right byte
			 *
			 *
			 */	
			for(int i=0; i<indexInt.size(); i++){
				for(int j=0; j<patternLength; j++){
					
					position = indexInt.get(i) + j;
					content = bytesPattern.get(j);
					listBytes.add(position, content);

				}

			}

			/*
			 * Copy the arraylist two an array for us to store it in the new file
			 *
			 */
			byte[] decompressedBytes = new byte[listBytes.size()];
			for(int j=0; j<listBytes.size(); j++){

				decompressedBytes[j] = listBytes.get(j);	
			
			
			}


			/*
			 *
			 * Store in the new file
			 *
			 */

			outputStream.write(decompressedBytes);


		   } catch (IOException ex){
		
		   	   System.out.println("Please provide input dir (Ex. java ImageDecompressor ./dir.bs)\n\n");
			   ex.printStackTrace();

		   }


   	}
	/**
	 *
	 * Return the list of characters that represent the bytes and the index in the pattern
	 *
	 * @param inputFile Name of the dir in wich index.in is in
	 *
	 *
	 *
	 */    
    	public static List<Character> OpenIndexFile(String inputFile) {

		/*
		 * Open file and store the content in an array of characters, simple as that.
		 *
		 *
		 */
		List<Character> listChars = new ArrayList<Character>();

        	try {
            
			FileReader fileReader = new FileReader(inputFile + "/index.in");
			//Read File Character by Character
			int nextChar = 0;
			while ((nextChar = fileReader.read()) != -1) {
    			    listChars.add((char) nextChar);
    			}

        
		}
        	catch (IOException ex) {
			
			System.out.println("Please provide input dir (Ex. java ImageDecompressor ./dir.bs)\n\n");
            		ex.printStackTrace();
        	}
        	
		return (listChars);
    	}
}
