import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
//	private static byte[] raw = intToByteArr(0x4, 0x42, 0x1, 0x41, 0x2, 0x43, 0x3, 0x44, 0x3, 0x0, 0x0, 0x0, 0x8, 0x6B, 0x7E, 0x0);
	private static final String message = "hello world";
	
	private static final Path PROVIDED_FILE = Paths.get("compressed.dat");
	private static final Path NEW_FILE = Paths.get("new.dat");
	private static final Path RAW_FILE = Paths.get("raw.txt");
	
	public static void main(String[] args) {
		try {
			decompress(); // decompress provided file into raw.txt
//			compress(); // compress raw.txt into new.dat
//			recompress(); // decompress provided and compress into new.dat
//			decompressNew(); // decompress new.dat into raw.txt
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void decompress() throws IOException {
		byte[] data = Files.readAllBytes(NEW_FILE);
		byte[] raw = Decoder.decode(data);
		Files.deleteIfExists(RAW_FILE);
		Files.createFile(RAW_FILE);
		Files.write(RAW_FILE, raw);
	}
	
	private static void decompressNew() throws IOException {
		byte[] data = Files.readAllBytes(PROVIDED_FILE);
		byte[] raw = Decoder.decode(data);
		Files.deleteIfExists(RAW_FILE);
		Files.createFile(RAW_FILE);
		Files.write(RAW_FILE, raw);
	}
	
	private static void recompress() throws IOException {
		byte[] data = Files.readAllBytes(PROVIDED_FILE);
		byte[] raw = Decoder.decode(data);
		byte[] compressed = Encoder.encode(raw);
		Files.deleteIfExists(NEW_FILE);
		Files.createFile(NEW_FILE);
		Files.write(NEW_FILE, compressed);
	}
	
	private static void compress() throws IOException {
		byte[] raw = Files.readAllBytes(RAW_FILE);
		byte[] data = Encoder.encode(raw);
		Files.deleteIfExists(NEW_FILE);
		Files.createFile(NEW_FILE);
		Files.write(NEW_FILE, data);
	}
		
	private static void test() {
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
			byte[] messageBytes = Decoder.decode(data);
			String message = new String(messageBytes);
			System.out.printf("Original: %7d\n", data.length);
			data2 = Encoder.encode(messageBytes);
			byte[] message2Bytes = Decoder.decode(data2);
			String message2 = new String(message2Bytes);
			System.out.printf("New:      %7d\n", data2.length);
			System.out.printf("Savings:  %7d (%.2f%%)\n", data.length - data2.length, ((double)(data.length - data2.length) * 100) / data.length);
//			System.out.println("Prefix? " + message.startsWith(message2));
			System.out.println("Match? " + message.equals(message2));
			System.out.println(message);
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
