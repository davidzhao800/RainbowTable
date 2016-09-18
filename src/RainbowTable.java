/**
 * @author zhao hang
 * For CS4236 Assignment 1
 */

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class RainbowTable {
	private MessageDigest SHA1; 
	static final int L_CHAIN = 220;   		                 //chain length
	static final int N_CHAIN = 88700;                        // number of chains
	static final String fileName = "RAINBOW";                // Rainbow Table 1 File Name
	static final String headFileName = "RAINBOW_HEAD";       // Rainbow Table 1 HEAD File Name
	static final String fileName2 = "RAINBOW2";              // Rainbow Table 2 File Name
	static final String headFileName2 = "RAINBOW_HEAD2";     // Rainbow Table 2 HEAD File Name
	private LinkedHashMap<String, Integer> table;            // store rainbow table 1
	private LinkedHashMap<String, Integer> table2;			 // store rainbow table 2
	
	private BitSet bitset;                                   // store head for rainbow table 1
	private BitSet bitset2;									 // store head for rainbow table 2
	
	public RainbowTable() {
		table = new LinkedHashMap<String, Integer>();
		table2 = new LinkedHashMap<String, Integer>();
		
		bitset = new BitSet();
		bitset2 = new BitSet();
		
		try {
			SHA1 = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Get SHA1 Instance Exception: " + e.getMessage());
		}

	}

	// use sequenced integer to convert to 3-byte word as head
	public void build_T(int choice) {
		try {
			byte[] word;
			int i = 0;
			int success = 0;
			int collision = 0;
			String key;
			HexBinaryAdapter byteToHex = new HexBinaryAdapter();
			if (choice == 1) {
				while (table.size() < N_CHAIN) {
					byte[] digest = new byte[20];
					word = integerToBytes(i);

					// Generating chain
					for (int j = 0; j < L_CHAIN; j++) {
						digest = hash(word);
						word = reduce(digest, j);
					}
					key = byteToHex.marshal(word);
					
					// store the tail
					if (!table.containsKey(key)) {
						table.put(key, i);

						bitset.set(i);
						//System.out.println(i);
						success++;
						//System.out.println("This is :" + success);
					} else {
						collision++;
					}
					i = i + 1;
				}
			} else {
				while (table2.size() < N_CHAIN) {
					byte[] digest = new byte[20];
					word = integerToBytes(i);

					// Generating chain
					for (int j = 0; j < L_CHAIN; j++) {
						digest = hash(word);
						word = reduce2(digest, j);

					}
					key = byteToHex.marshal(word);
					
					// store the tail
					if (!table2.containsKey(key)) {
						table2.put(key, i);

						bitset2.set(i);
						//System.out.println(i);
						success++;
						//System.out.println("This is :" + success);
					} else {
						collision++;
					}
					i = i + 1;
				}
			}
			
			System.out.println("Rainbow Table created, got collisions: " + collision +", writing to files...");
			
			// Write to file
		    ObjectOutputStream O;
		    
			if (choice == 1) {
				
				// Write to the bitset containing information of head into head file
				O = new ObjectOutputStream(new FileOutputStream(headFileName));
				O.writeObject(bitset);
				O.close();
				
				// Only write digest to table file
				FileWriter outputFile = new FileWriter(fileName);
				Set<Entry<String, Integer>> entrySet = table.entrySet();
				for (Entry<String, Integer> pair : entrySet) {
					outputFile.write(pair.getKey());
				}
				outputFile.close();
				
			} else {
				
				// Write to the bitset containing information of head into head file
				O = new ObjectOutputStream(new FileOutputStream(headFileName2));
				O.writeObject(bitset2);
				O.close();
				
				// Only write digest to table file
				FileWriter outputFile = new FileWriter(fileName2);
				Set<Entry<String, Integer>> entrySet = table2.entrySet();
				for (Entry<String, Integer> pair : entrySet) {
					outputFile.write(pair.getKey());
				}
				outputFile.close();
			}
			
			System.out.println("Rainbow Table has been writen to file.");
			
		} catch (Exception e) {
			System.out.println("Build Table Exception: " + e.getMessage());
		}
	}

	private byte[] hash(byte[] plainText) {
		byte digest[] = new byte[20];
		try {
			digest = SHA1.digest(plainText);
			SHA1.reset();
		} catch (Exception e) {
			System.out.println("SHA1 Exception:" + e.getMessage());
		}
		return digest;
	}
	//1 - 3: 90.84%
	//2 - 3: 90.02%
	//1 - 2: 90.06%
	private byte[] reduce(byte[] digest, int i) {
		byte[] word = new byte[3];
		word[0] = (byte) ((digest[0] + (byte) i) % 256);
		word[1] = (byte) ((digest[1]) % 256);
		word[2] = (byte) ((digest[2]) % 256);
		return word;
	}
	//2
	public byte[] reduce3(byte[] digest, int i) {
		byte last_byte = (byte) i;
		byte[] word = new byte[3];
		for (int j = 0; j < word.length; j++) {
			word[j] = (byte) (digest[(i + j) % 20] + last_byte);
		}
		return word;
	}
	//3
	public byte[] reduce2(byte[] digest, int i) {
		int start = i % 17;
		byte[] reduced = Arrays.copyOfRange(digest, start, start + 3);
		reduced[0] += (71 * i) % 251;
		reduced[1] += (107 * i) % 251;
		reduced[2] += (197 * i) % 251;
		return reduced;
	}
	
	private byte[] integerToBytes(int n) {
		byte a[] = new byte[3];
		a[0] = (byte) ((n >> 16) & 0xFF);
		a[1] = (byte) ((n >> 8) & 0xFF);
		a[2] = (byte) n;
		return a;
	}
	
}
