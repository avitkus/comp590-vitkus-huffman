import java.math.BigInteger;

public class Node implements Comparable<Node>{
	private final byte val;
	private Node parent;
	private Node left, right;
	private int weight;
	private boolean isLeaf;
	private int height;

	public Node() {
		this(null);
	}
	
	public Node(Node left, Node right) {
		this(null, (byte)0, left.weight + right.weight);
		left.parent = this;
		right.parent = this;
		this.left = left;
		this.right = right;
		isLeaf = false;
		int leftHeight = (left == null ? 0 : left.height);
		int rightHeight = (right == null ? 0 : right.height);
		height = 1 + Math.max(leftHeight, rightHeight);
	}
	
	public Node(byte val, int weight) {
		this(null, val, weight);
	}

	public Node(Node parent) {
		this(parent, (byte) 0);
	}

	public Node(byte val) {
		this(null, val);
	}

	public Node(Node parent, byte val) {
		this(parent, val, 1);
	}

	public Node(Node parent, byte val, int weight) {
		this.parent = parent;
		this.val = val;
		this.weight = weight;
		left = null;
		right = null;
		isLeaf = true;
		height = 0;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		if (this.left != null) {
			weight -= this.left.weight;
		}
		this.left = left;
		if (left != null) {
			left.parent = this;
			isLeaf = false;
			weight += left.weight;
			if (left.height >= height) {
				height = left.height + 1;
			}
		} else {
			if (right == null) {
				isLeaf = true;
				height = 0;
			} else {
				height = right.height + 1;
			}
		}
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		if (this.right != null) {
			weight -= this.right.weight;
		}
		this.right = right;
		if (right != null) {
			isLeaf = false;
			right.parent = this;
			weight += right.weight;
			if (right.height >= height) {
				height = right.height + 1;
			}
		} else {
			if (left == null) {
				isLeaf = true;
				height = 0;
			} else {
				height = left.height + 1;
			}
		}
	}

	public byte getVal() {
		return val;
	}

	public boolean isLeaf() {
		return isLeaf;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public Node getParent() {
		return parent;
	}

	public void addDescendant(BigInteger code, int codeLength, byte value) {
		boolean useRight = code.testBit(codeLength-1);
		if (codeLength == 1) {
			Node newNode = new Node(this, value);
			if (useRight) {
				setRight(newNode);
			} else {
				setLeft(newNode);
			}
		} else {
			Node next;
			if (useRight) {
				if (right == null) {
					setRight(new Node(this));
				}
				next = right;
			} else {
				if (left == null) {
					setLeft(new Node(this));
				}
				next = left;
			}
			next.addDescendant(code, codeLength - 1, value);
		}
	}

	public void addDescendant(int code, byte codeLength, byte value) {
		boolean useLeft = ((code >> (codeLength - 1)) & 1) == 0;
		if (codeLength == 1) {
			Node newNode = new Node(this, value);
			if (useLeft) {
				setLeft(newNode);
			} else {
				setRight(newNode);
			}
		} else {
			Node next;
			if (useLeft) {
				if (left == null) {
					setLeft(new Node(this));
				}
				next = left;
			} else {
				if (right == null) {
					setRight(new Node(this));
				}
				next = right;
			}
			next.addDescendant(code, (byte) (codeLength - 1), value);
		}
	}

	@Override
	public String toString() {
		return toString(0);
	}

	private String toString(int level) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < level; i++) {
			sb.append(" ");
		}
		sb.append(val).append("\n");
		if (!isLeaf) {
			sb.append(left == null ? "\n" : left.toString(level + 1));
			sb.append(right == null ? "\n" : right.toString(level + 1));
		}

		return sb.toString();
	}

	@Override
	public int compareTo(Node o) {
		if (weight != o.weight) {
			return weight - o.weight;
		} else {
			return height - o.height;
		}
	}
}