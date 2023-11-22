package huffman.src.huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        StdIn.setFile(fileName);
        sortedCharFreqList = new ArrayList<>();
        String first = "";
        while (StdIn.hasNextChar()){
            char fill = StdIn.readChar();
            first += fill;
        }
        String second = "";
        for (int i = 0; i < first.length(); i++)
        {
            String fill = "" + first.charAt(i);
            if (second.indexOf(fill) == -1)
                second += first.charAt(i);
        }
        for (int i = 0; i < second.length(); i++){
            double freq = 0;
            for (int b = 0; b < first.length(); b++)
            {
                if (second.charAt(i) == first.charAt(b))
                freq++;
            }
            freq /= first.length();
            if ( freq != 0)
                sortedCharFreqList.add(new CharFreq(second.charAt(i), freq));
        }
        if (sortedCharFreqList.size() == 1){
            int chars = (int) ((second.charAt(0) + 1) % 128);
            sortedCharFreqList.add(new CharFreq((char) chars, 0));
        }
        Collections.sort(sortedCharFreqList);
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
    Queue<TreeNode> src = new Queue <TreeNode>();
    Queue<TreeNode> target = new Queue <TreeNode>();
    for (int i = 0; i < sortedCharFreqList.size(); i++){
        src.enqueue(new TreeNode(sortedCharFreqList.get(i),null, null));
    }
    while(!src.isEmpty()){
        ArrayList<TreeNode> list = new ArrayList<TreeNode>();
        for (int i = 0; i < 2; i++){
            double original = 100;
            double target1 = 100;
        if (!src.isEmpty())
            original = src.peek().getData().getProbOcc();
        if (!target.isEmpty())
            target1 = target.peek().getData().getProbOcc();
        if (original > target1)
            list.add(target.dequeue());
        else
            list.add(src.dequeue());
        }
        TreeNode small = list.get(0), big = list.get(1);
            double prob = small.getData().getProbOcc() + big.getData().getProbOcc();
            TreeNode root = new TreeNode(new CharFreq(null, prob), small, big);
            target.enqueue(root);
    }
        while (target.size() != 1){
            TreeNode small = target.dequeue(), big = target.dequeue();
            double prob = small.getData().getProbOcc() + big.getData().getProbOcc();
            TreeNode root = new TreeNode(new CharFreq(null, prob), small, big);
            target.enqueue(root);
        }
        huffmanRoot = target.peek();
    }
    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    private static boolean isFireNode(TreeNode a){
        if (a == null)
            return false;
        if(a.getLeft() == null && a.getRight() == null)
            return true;
        return false;
    }
    
    private static String traversal(TreeNode parent, String get, String[] b){
        if(parent == null)
            return get;
        if (isFireNode(parent)){
            b[(int)parent.getData().getCharacter()] = get;
            return get.substring(0, get.length() - 1);
        }
        get = traversal(parent.getLeft(), get += "0", b);
        get = traversal(parent.getRight(), get += "1", b);
        if (get.length()> 0)
            return get.substring(0, get.length() - 1);
        return get;
    }

    public void makeEncodings() {
        String [] b = new String[128];
        String fill = traversal(huffmanRoot, "", b);
        encodings = b;
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String firsts = StdIn.readAll();
        String finals = "";
        for (int i = 0; i < firsts.length(); i++){
            finals += encodings [(int) firsts.charAt(i)];
        }
        writeBitString(encodedFile, finals);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    private static String decoding(TreeNode parent, String finals){
        if (parent == null)
        return "";
    String getter = "";
    TreeNode a = parent;
    for (int i = 0; i < finals.length(); i++){
        if (finals.charAt(i) == '0'){
            a = a.getLeft();
        }
        else if (finals.charAt(i) == '1'){
            a = a.getRight();
        }
        if (isFireNode(a)){
            getter += a.getData().getCharacter();
            a = parent;
        }
    }
    return getter;
    }
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        String decode = readBitString(encodedFile);
        decodedFile = decoding(huffmanRoot,decode);
        StdOut.print(decodedFile);
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
