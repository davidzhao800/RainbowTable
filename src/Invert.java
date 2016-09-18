/**
 * @author zhao hang
 * For CS4236 Assignment 1
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Invert {
	
	private static MessageDigest SHA1; 
	static int sha1_counter = 0;                                    // counter for hash()
	static int success = 0;                                         // success invert counter
	static HexBinaryAdapter hexBinConvertor;                        // convert hex string to byte[]
	static HashMap<String, Integer> rainbow_table;					// rainbow table 1
	static BitSet bs;    											// rainbow table 1 head
	static HashMap<String, Integer> rainbow_table2;					// rainbow table 2
	static BitSet bs2;    											// rainbow table 2 head
	static byte[][] m = new byte[5000][20];							// input digest
	static final int L_CHAIN = 220;   		  						//chain length
	static final int N_CHAIN = 88700;         						// number of chains
	static final String table_fileName = "RAINBOW"; 				// Rainbow Table 1 File Name
	static final String table_head_fileName = "RAINBOW_HEAD"; 		// Rainbow Table 1 HEAD File Name
	static final String table_fileName2 = "RAINBOW2"; 				// Rainbow Table 2 File Name
	static final String table_head_fileName2 = "RAINBOW_HEAD2"; 	// Rainbow Table 2 HEAD File Name
	static final String digest_fileName = "SAMPLE_INPUT.data"; 		// Digest Input File Name
	static final String output_fileName = "SAMPLE_OUTPUT.data"; 	// Output File Name
	

	public static void main(String[] args) {
		try {
			SHA1 = MessageDigest.getInstance("SHA1");
			hexBinConvertor = new HexBinaryAdapter();
			rainbow_table = new HashMap<String, Integer>();
			rainbow_table2 = new HashMap<String, Integer>();
			System.out.println("Reading Rainbow Tables into memory...");
			
			//Check for hash function, should be D1725392ADFAF1361C9015546FFB4FC44391B1A7
			//System.out.println( hexBinConvertor.marshal( hash(hexBinConvertor.unmarshal("D1F74D"))));
			
			readT();
			invert();
			writeToFile();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	private static void readT() throws NumberFormatException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(table_head_fileName));
		bs = (BitSet) ois.readObject();
		ois.close();
		
		ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(table_head_fileName2));
		bs2 = (BitSet) ois2.readObject();
		ois2.close();
		
		BufferedReader br = new BufferedReader(new FileReader(table_fileName));
		BufferedReader br2= new BufferedReader(new FileReader(table_fileName2));
		
		String line;
		int i = 0;
		line = br.readLine();
		// 6 char is one digest
		String[] tmp = line.split("(?<=\\G.{6})");
		
		// assign integer as head
		for( String x: tmp){
			while (!bs.get(i)) {
				i = i + 1;
			}
			rainbow_table.put(x, i);
			i = i+1;
		}
		
		String line2;
		i=0;
		line2 = br2.readLine();
		String[] tmp2 = line2.split("(?<=\\G.{6})");
		
		for( String x: tmp2){
			while (!bs2.get(i)) {
				i = i + 1;
			}
			rainbow_table2.put(x, i);
			i = i+1;
		}
	
		br2.close();
		br.close();
	}
	
	private static void writeToFile() throws NumberFormatException, IOException, ClassNotFoundException {
		System.out.println("Wring results to output file...");
		FileWriter outputFile = new FileWriter(output_fileName);

		for (int i = 0; i < m.length; i++) {
			String temp = hexBinConvertor.marshal(m[i]);

			if (temp.equals("000000")) {
				outputFile.write("0\n");
			} else {
				outputFile.write(temp + "\n");
			}
		}
		outputFile.write("The total number of words found is: " + success + "\n");
		outputFile.close();
		System.out.println("Done.");
	}
	
	private static void invert() throws IOException {
		System.out.println("Reading the digest from input file...");
		BufferedReader digestFile_br = new BufferedReader(new FileReader(digest_fileName));
		int j = 0;
		byte[][] digests = new byte[5000][20];
		String line;
		while ((line = digestFile_br.readLine()) != null) {

			String hex = line.substring(2, 10) + line.substring(12, 20) + line.substring(22, 30) + line.substring(32, 40) + line.substring(42, 50);
			hex = hex.replaceAll("\\s", "0");
			//unmarshal is used to convert string to binary[]
			digests[j] = hexBinConvertor.unmarshal(hex);
			j++;
		}
		digestFile_br.close();
		
		System.out.println("Start Inverting...");
		
		sha1_counter = 0;
		int counter_1 = 0;     // counter for success in table 1
		int counter_2 = 0;     // counter for success in table 2
		success = 0;
		
		for (int i = 0; i < digests.length; i++) {

			byte[] d = digests[i];

			byte[] result = new byte[3];
			String key = "";
			
			// regenerate the chain
			for (int k = L_CHAIN - 1; k >= 0; k--) {
				result = null;

				byte[] digest_to_match = d;
				byte[] value = new byte[3];

				for (int k1 = k; k1 < L_CHAIN; k1++) {
					value = reduce(digest_to_match, k1);
					digest_to_match = hash(value);
					sha1_counter++;
				}
				// convert the hex value computed into string
				key = hexBinConvertor.marshal(value);

				// if the key is inside table 1, regenerate that chain
				if (rainbow_table.containsKey(key)) {
					byte[] word = integerToBytes(rainbow_table.get(key));
					byte[] digest;
					for (int l = 0; l < L_CHAIN; l++) {
						digest = hash(word);
						sha1_counter++;

						if (Arrays.equals(digest, d)) {
							result = word;
							counter_1++;
							break;
						}
						word = reduce(digest, l);
					}

					if (result != null) {
						break;
					}
				}
				
				digest_to_match = d;
				
				for (int k2 = k; k2 < L_CHAIN; k2++) {
					value = reduce2(digest_to_match, k2);
					digest_to_match = hash(value);
					sha1_counter++;
				}
				key = hexBinConvertor.marshal(value);

				// if the key is inside table 2, regenerate that chain
				if (rainbow_table2.containsKey(key)) {
					byte[] word = integerToBytes(rainbow_table2.get(key));
					byte[] digest;
					for (int l = 0; l < L_CHAIN; l++) {
						digest = hash(word);
						sha1_counter++;

						if (Arrays.equals(digest, d)) {
							result = word;
							counter_2++;
							break;
						}
						word = reduce2(digest, l);
					}

					if (result != null) {
						break;
					}
				}
				
			}

			if (result != null) {
				success++;
				m[i] = result;
			} else {
				m[i] = integerToBytes(0);
			}
		}
		System.out.println("Number of hash() called: " + sha1_counter);
		System.out.println("Numbers solve by table1 : " + counter_1);
		System.out.println("Numbers solve by table2 : " + counter_2);
		System.out.println("F: " + 5000 * Math.pow(2, 23) / sha1_counter );
		int total = counter_1+counter_2;
		System.out.println("C: " + 1.0*total/5000*100 + "%" + "(" + total + "/5000)" );
	}
	
	private static byte[] hash(byte[] plainText) {
		byte digest[] = new byte[20];
		try {
			digest = SHA1.digest(plainText);
			SHA1.reset();
		} catch (Exception e) {
			System.out.println("SHA1 Exception:" + e.getMessage());
		}
		return digest;
	}
	//1
	private static byte[] reduce(byte[] digest, int i) {
		byte[] word = new byte[3];
		word[0] = (byte) ((digest[0] + (byte) i) % 256);
		word[1] = (byte) ((digest[1]) % 256);
		word[2] = (byte) ((digest[2]) % 256);
		return word;
	}
	//2
	public static byte[] reduce3(byte[] digest, int i) {
		byte last_byte = (byte) i;
		byte[] word = new byte[3];
		for (int j = 0; j < word.length; j++) {
			word[j] = (byte) (digest[(i + j) % 20] + last_byte);
		}
		return word;
	}
	//3
	public static byte[] reduce2(byte[] digest, int i) {
		int start = i % 17;
		byte[] reduced = Arrays.copyOfRange(digest, start, start + 3);
		reduced[0] += (71 * i) % 251;
		reduced[1] += (107 * i) % 251;
		reduced[2] += (197 * i) % 251;
		return reduced;
	}
	
	private static byte[] integerToBytes(int n) {
		byte a[] = new byte[3];
		a[0] = (byte) ((n >> 16) & 0xFF);
		a[1] = (byte) ((n >> 8) & 0xFF);
		a[2] = (byte) n;
		return a;
	}

}
