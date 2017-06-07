/**
 * Class used to perform file compression and decompression using Huffman coding. 
 * @author Vincent Rideout
 *
 */
public class Huffman {
	
	/**
	 * Member variables to track initialization parameters across functions.
	 */
	private static boolean verbose;
	private static boolean force;
	private static final int MAGICNUMBERBITS = 16;
	private static final int HEADERBITS = 32;
	
	/**
	 * Main function performs initialization based on command line parameters, then calls compress or decompress accordingly.
	 * @param args - Supported command line arguments:  (-c): Compress mode (Default if neither -c or -u is specified).
	 * 													(-u): Uncompress mode.
	 * 													(-f): Force a compression when the compressed file will be larger than the input.
	 * 													(-v): Verbose output. Enables Huffman coding details printed to standard output.
	 * 													(Input file, output file): The first file listed will be the input, second will be output.
	 */
	public static void main(String args[]){
		boolean doCompress = true;
		String infile = null;
		String outfile = null;
		verbose = false;
		force = false;
		for(String s: args){
			switch(s){
			case "-c": doCompress = true;
			break;
			case "-u": doCompress = false;
			break;
			case "-f": force = true;
			break;
			case "-v": verbose = true;
			break;
			default: if(infile == null){
				infile = s;
			}
			else{
				outfile = s;
			}
			break;
			}
		}
		if(doCompress){
			compress(infile, outfile);
		}
		else{
			decompress(infile, outfile);
		}
	}
	
	/**
	 * Compress a file using Huffman coding. 
	 * @param infile - Name of input file.
	 * @param outfile - Name of output file.
	 */
	private static void compress(String infile, String outfile){
		int freq[] = new int[256];
		for(int i = 0; i < 256; i++){
			freq[i] = 0;
		}
		TextFile input = new TextFile(infile, 'r');
		int inputbits = 0;
		while(!input.EndOfFile()){
			freq[(int)input.readChar()]++;
			inputbits += 8;
		}
		HuffmanNode trees[] = new HuffmanNode[256];
		int treeCount = 0;
		for(int i = 0; i < 256; i++){
			if(freq[i] > 0){
				trees[treeCount++] = new HuffmanNode(freq[i], i);
			}
		}
		int smallest, nextsmallest;
		HuffmanNode newTree;
		while(treeCount > 2){
			if(trees[0].getCount() < trees[1].getCount()){
				smallest = 0;
				nextsmallest = 1;
			}
			else{
				smallest = 1;
				nextsmallest = 0;
			}
			for(int i = 2; i < treeCount; i++){
				if(trees[i].getCount() < trees[nextsmallest].getCount()){
					if(trees[i].getCount() < trees[smallest].getCount()){
						nextsmallest = smallest;
						smallest = i;
					}
					else{
						nextsmallest = i;
					}
				}
			}
			newTree = new HuffmanNode(trees[smallest].getCount() + trees[nextsmallest].getCount(), 0);
			newTree.setLeft(trees[smallest]);
			newTree.setRight(trees[nextsmallest]);
			trees[smallest] = newTree;
			trees[nextsmallest] = trees[--treeCount];
		}
		if(treeCount == 2){
			newTree = new HuffmanNode(0,0);
			newTree.setLeft(trees[0]);
			newTree.setRight(trees[1]);
		}
		else if(treeCount == 1){
			newTree = new HuffmanNode(0,0);
			newTree.setLeft(trees[0]);
			newTree.setRight(new HuffmanNode(0,0));
		}
		else{
			System.out.println("Empty input file!");
			return;
		}
		String[] codes = new String[256];
		for(int i = 0; i < 256; i++){
			codes[i] = null;
		}
		buildCodes(codes, newTree, "");
		int compressedbits = 0;
		for(int i = 0; i < 256; i++){
			if(codes[i] != null){
				compressedbits += codes[i].length() * freq[i];
			}
		}
		compressedbits += newTree.bitsize() + MAGICNUMBERBITS + HEADERBITS;
		compressedbits += compressedbits % 8;
		if(verbose){
			System.out.println("Frequency of each ASCII character in the input file:");
			for(int i = 0; i < 256; i++){
				if(freq[i] > 0){
					System.out.println(i + " appears " + freq[i] + " times.");
				}
			}
			System.out.println("\nHuffman Tree:");
			printTree(newTree, 0);
			System.out.println("\nHuffman Codes:");
			for(int i = 0; i < 256; i++){
				if(codes[i] != null){
					System.out.println(i + ": " + codes[i]);
				}
			}
			System.out.println("\nUncompressed File Size (in bits): " + inputbits);
			System.out.println("Compressed File Size (in bits): " + compressedbits);
		}
		if(force || (compressedbits < inputbits)){
			BinaryFile output = new BinaryFile(outfile, 'w');
			output.writeChar('H');
			output.writeChar('F');
			writeTree(output, newTree);
			input.rewind();
			while(!input.EndOfFile()){
				char ch = input.readChar();
				for(char c: codes[(int)ch].toCharArray()){
					if(c == '0'){
						output.writeBit(false);
					}
					else{
						output.writeBit(true);
					}
				}
			}
			output.close();
		}
		else{
			System.out.println("Compression would not produce a smaller file. File not compressed.");
		}
		input.close();
	}

	/**
	 * Decompress a file compressed by this class.
	 * @param infile - Name of compressed file.
	 * @param outfile - Name of output file.
	 */
	private static void decompress(String infile, String outfile){
		BinaryFile input = new BinaryFile(infile, 'r');
		if(input.readChar() != 'H' || input.readChar() != 'F'){
			System.out.println("Not a Huffman file!");
			return;
		}
		HuffmanNode root = readTree(input);
		if(verbose){
			System.out.println("Huffman Tree:");
			printTree(root, 0);
		}
		TextFile output = new TextFile(outfile, 'w');
		while(!input.EndOfFile()){
			HuffmanNode traverser = root;
			while(!traverser.isLeaf()){
				if(input.readBit()){
					traverser = traverser.getRight();
				}
				else{
					traverser = traverser.getLeft();
				}
			}
			output.writeChar((char)traverser.getData());
		}
		input.close();
		output.close();
	}
	
	// Recursive helper methods for Huffman Tree traversal.
	
	private static void buildCodes(String[] codes, HuffmanNode tree, String bits){
		if(tree.isLeaf()){
			codes[tree.getData()] = bits;
		}
		else{
			buildCodes(codes, tree.getLeft(), bits + "0");
			buildCodes(codes, tree.getRight(), bits + "1");
		}
	}
	
	private static void writeTree(BinaryFile output, HuffmanNode tree){
		if(tree.isLeaf()){
			output.writeBit(false);
			output.writeChar((char)tree.getData());
		}
		else{
			output.writeBit(true);
			writeTree(output, tree.getLeft());
			writeTree(output, tree.getRight());
		}
	}
	
	private static HuffmanNode readTree(BinaryFile infile){
		if(infile.readBit()){
			HuffmanNode root = new HuffmanNode(0,0);
			root.setLeft(readTree(infile));
			root.setRight(readTree(infile));
			return root;
		}
		else{
			return new HuffmanNode(0, (int)infile.readChar());
		}
	}
	
	private static void printTree(HuffmanNode tree, int offset){
		for(int i = 0; i < offset; i++){
			System.out.print(" ");
		}
		if(tree.isLeaf()){
			System.out.print(tree.getData() + "\n");
		}
		else{
			System.out.print("Node\n");
			printTree(tree.getLeft(), offset + 1);
			printTree(tree.getRight(), offset + 1);
		}
	}
}
