import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
//	private static byte[] raw = intToByteArr(0x4, 0x42, 0x1, 0x41, 0x2, 0x43, 0x3, 0x44, 0x3, 0x0, 0x0, 0x0, 0x8, 0x6B, 0x7E, 0x0);
	private static final String message = "hello world";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			byte[] data;
			byte[] data2;
			data = Files.readAllBytes(Paths.get("compressed.dat"));
//			System.out.printf("%X %X %X %X - %d\n", data[256], data[257], data[258], data[259], (((int)data[256] & 0xFF) << 24) | (((int)data[257] & 0xFF) << 16) | (((int)data[258] & 0xFF) << 8) | ((int)data[259] & 0xFF));
//			data = Encoder.encode(message);
//			for(byte b : data) {
//				System.out.printf("%X%X ", (b >>> 4) & 0xF, (b & 0xF));
//			}
//			System.out.println();
			String message = Decoder.decode(data);
			System.out.printf("Original: %7d\n", data.length);
			data2 = Encoder.encode(message);
			String message2 = Decoder.decode(data2);
			System.out.printf("New:      %7d\n", data2.length);
			System.out.printf("Savings:  %7d (%.2f%%)\n", data.length - data2.length, ((double)(data.length - data2.length) * 100) / data.length);
//			System.out.println("Prefix? " + message.startsWith(message2));
			System.out.println("Match? " + message.equals(message2));
//			System.out.println(message);
//			System.out.println("Message 1 len: " + message.length());
//			System.out.println("Message 2 len: " + message2.length());
			for(int i = 0; i < message.length() && i < message2.length(); i ++) {
				if (message.charAt(i) != message2.charAt(i)) {
					System.out.println("Original: " + message.substring(i - 10, i + 10));
					System.out.println("New:      " + message2.substring(i - 10, i + 10));
					System.out.println("missmatch at char " + i + "(" + message.charAt(i) + " != " + message2.charAt(i) + ")");
					System.out.printf("%X %X\n", (int)message.charAt(i), (int)message2.charAt(i));
//					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static byte[] intToByteArr(int... arr) {
		byte[] bytes = new byte[arr.length];
		for(int i = 0; i < arr.length; i ++) {
			bytes[i] = (byte)arr[i];
		}
		return bytes;
	}
}
