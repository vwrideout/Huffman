/**
 * Basic tree data structure used for Huffman coding.
 * @author Vincent Rideout
 *
 */

public class HuffmanNode{
	private int data;
	private int count;
	private HuffmanNode left;
	private HuffmanNode right;
	
	public HuffmanNode(int count, int data){
		this.count = count;
		this.data = data;
		this.left = null;
		this.right = null;
	}
	
	public boolean isLeaf(){
		return (left == null && right == null);
	}
	
	/**
	 * Recursively counts the number of bits that will be used when writing this tree's signature to a binary file.
	 * @return - Number of bits.
	 */
	public int bitsize(){
		if(this.isLeaf()){
			return 9;
		}
		else{
			return 1 + this.left.bitsize() + this.right.bitsize();
		}
	}
	
	//Getters and setters for member variables.
	
	public int getCount(){
		return count;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getData(){
		return data;
	}
	
	public void setData(int data){
		this.data = data;
	}
	
	public HuffmanNode getLeft(){
		return left;
	}
	
	public void setLeft(HuffmanNode left){
		this.left = left;
	}
	
	public HuffmanNode getRight(){
		return right;
	}
	
	public void setRight(HuffmanNode right){
		this.right = right;
	}
}
