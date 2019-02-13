import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Decoder {
	
	public static byte[] decode(byte[] raw) {
		int codesLength = 256;
		BigInteger code = BigInteger.ZERO;
		int codeLen = 0;
		Node root = new Node();
		int[] lengths = new int[codesLength];
		byte[] values = new byte[codesLength];
		for(int i = 0; i < codesLength; i ++) {
			lengths[i] = raw[i] & 0xFF;
			values[i] = (byte)i;
		}
		for(int i = 1; i < codesLength; i ++) {
			int tmpLength = lengths[i];
			byte tmpVal = values[i];
			int j = i-1;
			for(; j >= 0 && lengths[j] > tmpLength; j--) {
				lengths[j+1] = lengths[j];
				values[j+1] = values[j];
			}
			lengths[j+1] = tmpLength;
			values[j+1] = tmpVal;
		}
		for(int i = 0; i < codesLength; i ++) {
			int newLen = lengths[i];
			if (newLen != codeLen) {
				while(codeLen < newLen) {
					code = code.shiftLeft(1);
					codeLen++;
				}
			}
			root.addDescendant(code, codeLen, values[i]);
			code = code.add(BigInteger.ONE);
		}
		
		int messageLength = 0;
		for(int i = 0; i < 4; i ++ ) {
			messageLength <<= 8;
			messageLength += ((int)raw[codesLength+i] & 0xFF);
		}
		
		ByteBuffer bb = ByteBuffer.allocate(messageLength);
		
		int bitsUsed = 0;
		
		int base = codesLength+4;
		int symbolCount = 0;
		Node curNode = root;
		for(int i = 0; base+i < raw.length; i ++) {
			int data = raw[base + i] & 0xFF;
			for(int j = 7; j >= 0 && symbolCount < messageLength; j --) {
				bitsUsed ++;
				if (((data >> j) & 1) == 0) {
					curNode = curNode.getLeft();
				} else {
					curNode = curNode.getRight();
				}
				if (curNode.isLeaf()) {
					bb.put(curNode.getVal());
					curNode = root;
					symbolCount++;
				}
			}
		}
		
//		System.out.printf("Decoded entropy: %.5f\n", (double) bitsUsed / messageLength);
		
		byte[] ret;
		if (bb.hasArray()) {
			ret = bb.array();
		} else {
			ret = new byte[bb.limit()];
			bb.rewind();
			bb.get(ret);
		}
		
		return ret;
	}
	
	public static String decodeExtended(byte[] raw) {
		byte codesLength = raw[0];
		byte[] map = new byte[(int)Math.pow(2, codesLength) - 1];
		byte symbol;
		byte code = 0;
		byte codeLen = 0;
		Node root = new Node();
		for(int i = 0; i < codesLength; i ++) {
			symbol = raw[1+2*i];
			byte newLen = raw[2+2*i];
			if (newLen != codeLen) {
				code <<= 1;
				codeLen = newLen;
			}
			root.addDescendant(code, codeLen, symbol);
			int insertIdx = codeToIdx(code, codeLen);
			map[insertIdx] = symbol;
			code ++;
		}
		
//		for(int i = 0; i < map.length; i ++) {
//			System.out.println(i + ": " + map[i]);
//		}
		
		int messageLength = 0;
		for(int i = 0; i < 4; i ++ ) {
			messageLength <<= 8;
			messageLength += raw[1+2*codesLength+i];
		}
		
		System.out.println(root);
		System.out.println(messageLength);
		
		StringBuilder sb = new StringBuilder();
		
		int base = 5+2*codesLength;
		int symbolCount = 0;
		Node curNode = root;
		for(int i = 0; base+i < raw.length; i ++) {
			byte data = raw[base + i];
			System.out.println(data);
			for(int j = 7; j >= 0 && symbolCount < messageLength; j --) {
				if (((data >> j) & 1) == 0) {
					curNode = curNode.getLeft();
				} else {
					curNode = curNode.getRight();
				}
				if (curNode.isLeaf()) {
					sb.append((char)curNode.getVal());
					curNode = root;
					symbolCount++;
				}
			}
		}
		
		System.out.println(sb);
		
		return sb.toString();
	}
	
	public static int codeToIdx(byte code, byte length) {
		int idx = 0;
		
		for(int i = length-1; i >= 0; i --) {
			idx = idx * 2 + 1 + ((code >>> i) & 1);
		}
		
		return idx;
	}
}
