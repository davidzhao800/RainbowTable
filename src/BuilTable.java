import java.awt.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class BuilTable {

	public static void main(String[] args) {
		RainbowTable table = new RainbowTable();
		table.build_T(1);
		table.build_T(2);
		
//		ArrayList<Integer> l = new ArrayList<Integer>();
//		
//		for (int i=0; i<20; i++){
//			l.add(i);
//		}
//		ArrayList<Integer> l2 = l;
//		ArrayList<Integer> l3 = l;
//		Collections.shuffle(l);
//		Collections.shuffle(l2);
//		Collections.shuffle(l3);
//		System.out.println(l);
//		System.out.println(l2);
//		System.out.println(l3);
		
//		BitSet bs = new BitSet();
//		bs.set(2);
//		bs.set(4);
//		bs.set(6);
//		System.out.println(bs.toByteArray());
//		byte[] a = bs.toByteArray();
//		
//		BitSet bs2 = fromByteArray(a);
//		System.out.println(bs2.toString());
//		
//		try {
//			Files.write(Paths.get("bytearray"), a);
//			byte[] data = Files.readAllBytes(Paths.get("bytearray"));
//			BitSet bs3 = fromByteArray(data);
//			System.out.println(bs3.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
//	public static BitSet fromByteArray(byte[] bytes) {
//		BitSet bits = new BitSet();
//		for (int i = 0; i < bytes.length * 8; i++) {
//			if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
//				bits.set(i);
//			}
//		}
//		return bits;
//	}
}
