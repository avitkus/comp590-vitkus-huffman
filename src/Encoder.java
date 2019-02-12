import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.PriorityQueue;

import io.OutputStreamBitSink;

public class Encoder {
	public static byte[] encode(String message) {
		int codesLength = 256;
		int[] counts = new int[codesLength];
		int[] values = new int[codesLength];
		for(int i = 0; i < codesLength; i ++) {
			values[i] = i;
			counts[i] = 0;
		}
		
		for(byte b : message.getBytes()) {
			int iVal = ((int)b) & 0xFF;
			counts[iVal] ++;
		}
		
		double entropy = 0;
		System.out.println("<table>\n<tr><th>Code</th><th>Char</th><th>Probability</th></tr>");
		for(int i = 0; i < codesLength; i ++) {
			double prob = (double)counts[i] / message.length();
			if (counts[i] != 0) {
				entropy -= prob * Math.log(prob) / Math.log(2);
			}
			if (Character.isAlphabetic(i) || Character.isDigit(i)) {
				System.out.printf("<tr><td>%2X</td><td>%c</td><td>%8.5f%%</td></tr>\n", i, i, prob * 100);
			} else if (Character.isWhitespace(i)) {
				System.out.printf("<tr><td>%2X</td><td>'%c'</td><td>%8.5f%%</td></tr>\n", i, i, prob * 100);
			} else {
				System.out.printf("<tr><td>%2X</td><td></td><td>%8.5f%%</td></tr>\n", i, prob * 100);
			}
		}
		System.out.println("</table>");
		System.out.printf("Theoretical entropy: %.5f\n", entropy);
		for(int i = 1; i < codesLength; i ++) {
			int tmpCounts = counts[i];
			int tmpVal = values[i];
			int j = i-1;
			for(; j >= 0 && counts[j] > tmpCounts; j--) {
				counts[j+1] = counts[j];
				values[j+1] = values[j];
			}
			counts[j+1] = tmpCounts;
			values[j+1] = tmpVal;
		}
		
		PriorityQueue<Node> queue = new PriorityQueue<>();
		for(int i = 0; i < values.length; i ++) {
			queue.add(new Node((byte)values[i], counts[i]));
		}
		
		while(queue.size() > 1) {
			Node a = queue.poll();
			Node b = queue.poll();
			Node newNode = new Node(a, b);
			queue.add(newNode);
		}
		Node root = queue.poll();
		
		BigInteger[][] codes = getCodes2(root, codesLength);
		{
			int[] lengths = new int[codesLength];
			int codeLen = 1;
			BigInteger code = BigInteger.ZERO;
			values = new int[codesLength];
			for(int i = 0; i < codesLength; i ++) {
				lengths[i] = codes[i][1].intValue();
				values[i] = i;
			}
			for(int i = 1; i < codesLength; i ++) {
				int tmpLength = lengths[i];
				int tmpVal = values[i];
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
				while(codeLen < newLen) {
					code = code.shiftLeft(1);
					codeLen++;
				}
				codes[values[i]][0] = code;
				codes[values[i]][1] = BigInteger.valueOf(codeLen);
				code = code.add(BigInteger.ONE);
			}
		}

		int bitsUsed = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStreamBitSink osbs = new OutputStreamBitSink(baos)) {
			for(int i = 0; i < codesLength; i ++) {
				osbs.write(codes[i][1].byteValue(), 8);
			}
			
			osbs.write(message.length(), 32);
			
			for(byte symbol : message.getBytes()) {

				int iVal = ((int)symbol) & 0xFF;
				BigInteger code = codes[iVal][0];
				int codeLen = codes[iVal][1].intValue();
				String codeStr = code.toString(2);
				int missing = codeLen - codeStr.length();
				while(missing > 32) {
					osbs.write(0, 32);
					missing -= 32;
					bitsUsed += 32;
				}
				if (missing != 0) {
					osbs.write(0, missing);
					bitsUsed += missing;
				}
				osbs.write(codeStr);
				bitsUsed += codeStr.length();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf("Compressed entropy: %.5f\n", ((double)bitsUsed) / message.length());
		return baos.toByteArray();
	}
	
	private static Node codesToStandardTree(int[] lengthsIn) {
		Node root = new Node();
		int codesLength = lengthsIn.length;
		byte[] lengths = new byte[codesLength];
		byte[] values = new byte[codesLength];
		byte codeLen = 0;
		byte code = 0;
		
		for(int i = 0; i < codesLength; i ++) {
			lengths[i] = (byte)lengthsIn[i];
			values[i] = (byte)i;
		}
		
		for(int i = 1; i < codesLength; i ++) {
			byte tmpLength = lengths[i];
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
			byte newLen = lengths[i];
			if (newLen != codeLen) {
				while(codeLen < newLen) {
					code <<= 1;
					codeLen++;
				}
			}
			root.addDescendant(code, codeLen, values[i]);
			code ++;
		}
		
		return root;
	}
	
	private static int[][] getCodes(Node root, int count) {
		int[][] codes = new int[count][2];
		
		getCodes(root, 0, 0, codes);
		
		return codes;
	}
	
	private static int[][] getCodes(Node root, int curCode, int curCodeLength, int[][] codes) {
		if (root.isLeaf()) {
			int val = root.getVal() & 0xFF;
			codes[val][0] = curCode;
			codes[val][1] = curCodeLength;
		} else {
			getCodes(root.getLeft(), curCode << 1, curCodeLength + 1, codes);
			getCodes(root.getRight(), (curCode << 1) + 1, curCodeLength + 1, codes);
		}
		return codes;
	}
	

	
	private static BigInteger[][] getCodes2(Node root, int count) {
		BigInteger[][] codes = new BigInteger[count][2];
		
		getCodes(root, BigInteger.ZERO, BigInteger.ZERO, codes);
		
		return codes;
	}
	
	private static BigInteger[][] getCodes(Node root, BigInteger curCode, BigInteger curCodeLength, BigInteger[][] codes) {
		if (root.isLeaf()) {
			int val = root.getVal() & 0xFF;
			codes[val][0] = curCode;
			codes[val][1] = curCodeLength;
		} else {
			BigInteger nextCode = curCode.shiftLeft(1);
			BigInteger nextCodeLength = curCodeLength.add(BigInteger.ONE);
			getCodes(root.getLeft(), nextCode, nextCodeLength, codes);
			getCodes(root.getRight(), nextCode.add(BigInteger.ONE), nextCodeLength, codes);
		}
		return codes;
	}
}
