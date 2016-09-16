import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Invert {
	
	private static MessageDigest SHA1; 
	static int success = 0;
	static HexBinaryAdapter hexBinConvertor;
	static HashMap<String, Integer> rainbow_table;
	static BitSet bs;    // indicate which head is used
	static HashMap<String, Integer> rainbow_table2;
	static BitSet bs2;    // indicate which head is used
	static byte[][] m = new byte[5000][20];
	static final int L_CHAIN = 180;   		  //chain length
	static final int N_CHAIN = 110000;         // number of chains
	static final String table_fileName = "RAINBOW"; // Rainbow Table File Name
	static final String table_head_fileName = "RAINBOW_HEAD"; // Rainbow Table File Name
	static final String table_fileName2 = "RAINBOW2"; // Rainbow Table File Name
	static final String table_head_fileName2 = "RAINBOW_HEAD2"; // Rainbow Table File Name
	static final String digest_fileName = "SAMPLE_INPUT.data"; // Input File Name
	static final String output_fileName = "SAMPLE_OUTPUT.data"; // Output File Name
	

	public static void main(String[] args) {
		try {
			SHA1 = MessageDigest.getInstance("SHA1");
			hexBinConvertor = new HexBinaryAdapter();
			rainbow_table = new HashMap<String, Integer>();
			rainbow_table2 = new HashMap<String, Integer>();
			readT();
//			System.out.println(rainbow_table.size());
//			Set<Entry<String, Integer>> entrySet = rainbow_table.entrySet();
//			for (Entry<String, Integer> pair : entrySet) {
//				System.out.println(pair.getValue().toString() + " " + pair.getKey() + "\n");
//			}
			invert();
			writeToFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	//------------  Read in the Table ------------------//                   //
	private static void readT() throws NumberFormatException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(table_head_fileName));
		bs = (BitSet) ois.readObject();
		ois.close();
		
		ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(table_head_fileName2));
		bs2 = (BitSet) ois2.readObject();
		ois2.close();
		//System.out.println(bs.toString());
		BufferedReader br = new BufferedReader(new FileReader(table_fileName));
		BufferedReader br2= new BufferedReader(new FileReader(table_fileName2));
		
		String line;
		int i = 0;
		while ((line = br.readLine()) != null) {
			while (!bs.get(i)) {
				i = i + 1;
			}
			rainbow_table.put(line, i);
			i = i+1;
			// key is the digest and value is word in Integer
			// written as word : digest in file hence stored in opposite way
		}
		i=0;
		while ((line = br2.readLine()) != null) {
			while (!bs2.get(i)) {
				i = i + 1;
			}
			rainbow_table2.put(line, i);
			i = i+1;
			// key is the digest and value is word in Integer
			// written as word : digest in file hence stored in opposite way
		}
		br2.close();
		br.close();
	}
	
	private static void writeToFile() throws NumberFormatException, IOException, ClassNotFoundException {
		FileWriter outputFile = new FileWriter(output_fileName);

		for (int i = 0; i < m.length; i++) {
			String temp = hexBinConvertor.marshal(m[i]);

			if (temp.equals("000000")) {
				outputFile.write("0\n");
			} else {
				outputFile.write(temp + "\n");
			}
		}
		outputFile.write("\n\nTotal number of words found: " + success + "\n");
		outputFile.close();
	}
	
	private static void invert() throws IOException {
		// read the digest from input file
		BufferedReader digestFile_br = new BufferedReader(new FileReader(digest_fileName));
		int j = 0;
		byte[][] digests = new byte[5000][20];
		String line;
		while ((line = digestFile_br.readLine()) != null) {

			// Not Reading space
			String hex = line.substring(2, 10) + line.substring(12, 20) 
			+ line.substring(22, 30) + line.substring(32, 40) + line.substring(42, 50);

			// Replacing space with 0 i.e. F86786 = 0F86786
			hex = hex.replaceAll("\\s", "0");
			digests[j] = hexBinConvertor.unmarshal(hex);
			j++;
		}
		digestFile_br.close();
		
		int sha1_counter = 0;
		int counter_2 = 0;
		success = 0;
		for (int i = 0; i < digests.length; i++) {

			// read the digests read from input file
			byte[] d = digests[i];

			byte[] result = new byte[3];
			String key = "";
			
			// regenerate the chain for current digest d
			for (int k = L_CHAIN - 1; k >= 0; k--) {
				result = null;

				byte[] digest_to_match = d;
				byte[] val = new byte[3];

				// reduce then hash (opposite of rainbow table generation), get a digest for matching
				for (int k1 = k; k1 < L_CHAIN; k1++) {
					val = reduce(digest_to_match, k1);
					digest_to_match = hash(val);
					sha1_counter++;
				}
				key = hexBinConvertor.marshal(val);

				// if the key is found i.e. digest is found, regenrate the
				// chain and get the plaintext corresponding to it
				if (rainbow_table.containsKey(key)) {
					byte[] word = integerToBytes(rainbow_table.get(key));
					byte[] digest;
					for (int l = 0; l < L_CHAIN; l++) {
						digest = hash(word);
						sha1_counter++;

						// break the loop when digest is found
						if (Arrays.equals(digest, d)) {
							result = word;
							break;
						}
						word = reduce(digest, l);
					}

					if (result != null) {
						break;
					}
				}
				
				digest_to_match = d;
				
				// reduce then hash (opposite of rainbow table generation), get a digest for matching
				for (int k2 = k; k2 < L_CHAIN; k2++) {
					val = reduce2(digest_to_match, k2);
					digest_to_match = hash(val);
					sha1_counter++;
				}
				key = hexBinConvertor.marshal(val);

				// if the key is found i.e. digest is found, regenrate the
				// chain and get the plaintext corresponding to it
				if (rainbow_table2.containsKey(key)) {
					byte[] word = integerToBytes(rainbow_table2.get(key));
					byte[] digest;
					for (int l = 0; l < L_CHAIN; l++) {
						digest = hash(word);
						sha1_counter++;

						// break the loop when digest is found
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

			// If digest is found store the result else 0
			if (result != null) {
				success++;
				m[i] = result;
			} else {
				m[i] = integerToBytes(0);
			}
		}
		System.out.println("Number of hash(): " + sha1_counter);
		System.out.println("Numbers solve by table2 : " + counter_2);
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
	
	private static byte[] reduce(byte[] digest, int i) {
		byte[] word = new byte[3];
		word[0] = (byte) ((digest[0] + (byte) i) % 256);
		word[1] = (byte) ((digest[1]) % 256);
		word[2] = (byte) ((digest[2]) % 256);
		return word;
	}
	
	public static byte[] reduce2(byte[] digest, int i) {
		byte last_byte = (byte) i;
		byte[] word = new byte[3];
		for (int j = 0; j < word.length; j++) {
			word[j] = (byte) (digest[(i + j) % 20] + last_byte);
		}
		return word;
	}
	
	private static byte[] integerToBytes(int n) {
		byte a[] = new byte[3];
		a[0] = (byte) ((n >> 16) & 0xFF);
		a[1] = (byte) ((n >> 8) & 0xFF);
		a[2] = (byte) n;
		return a;
	}

}
