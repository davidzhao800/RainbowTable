import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;

public class BuilTable {

	public static void main(String[] args) {
		RainbowTable table = new RainbowTable();
		table.build_T(1);
		table.build_T(2);
//		try {
//			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("RAINBOW"));
//			BitSet bs = (BitSet) ois.readObject();
//			System.out.println(bs.toString());
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String s = "7477178117683218292";
//		Long i = Long.parseLong(s);
//		System.out.println(i);
//		BitSet bs = convert(i);
//		
//		
//		System.out.println(bs.toString());
		
	}
}
