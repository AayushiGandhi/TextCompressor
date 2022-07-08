import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


public interface FileCompressor {
	
	boolean encode ( String input_filename, int level, boolean reset, String output_filename );
	
	boolean decode ( String input_filename, String output_filename );
	
	Map<Character, String> codebook ( );
}

class FileCompressorClass implements FileCompressor{
	
	HashMap<String, Integer> periodicFrequencyMap = new HashMap<String, Integer>();
	String returnString="";
	huffmanNode rootNode = new huffmanNode();
	HashMap<Integer, huffmanNode> encodeMap = new HashMap<Integer, huffmanNode>();
	HashMap<Integer, huffmanNode> finalRebuildEncodeMap = new HashMap<Integer, huffmanNode>();
	HashMap<Character, String> codeBookMap = new HashMap<Character, String>();
	int rebuildMapCount=0;
	
	//getters and setters
	public HashMap<Character, String> getCodeBookMap() {
		return codeBookMap;
	}

	public void setCodeBookMap(HashMap<Character, String> codeBookMap) {
		this.codeBookMap = codeBookMap;
	}

	public HashMap<Integer, huffmanNode> getEncodeMap() {
		return encodeMap;
	}

	public void setEncodeMap(HashMap<Integer, huffmanNode> encodeMap) {
		this.encodeMap = encodeMap;
	}

	public String getReturnString() {
		return returnString;
	}

	public void setReturnString(String returnString) {
		this.returnString = returnString;
	}

	public HashMap<String, Integer> getPeriodicFrequencyMap() {
		return periodicFrequencyMap;
	}

	public void setPeriodicFrequencyMap(HashMap<String, Integer> periodicFrequencyMap) {
		this.periodicFrequencyMap = periodicFrequencyMap;
	}
	
	//-----------------------sortedMap-------------------------------------
	/***
	 * sorts HashMap according to the increasing order of the frequency value obtained from the value of the HashMap
	 * @param periodicFrequencyMap : hashmap with key as characters and value as the frequencies of the character
	 * @return : returns a sorted array of key values of the HashMap
	 */
	public String[] sortMap(HashMap<String, Integer> periodicFrequencyMap) {
				
		String[] sortedArray = new String[periodicFrequencyMap.size()];
		int i=0;
		
		TreeSet valueTreeSet = new TreeSet();
		valueTreeSet.addAll(periodicFrequencyMap.values());
				
		TreeSet keyTreeSet = new TreeSet();
		keyTreeSet.addAll(periodicFrequencyMap.keySet());
				
		Iterator value = valueTreeSet.iterator();
		
		while(value.hasNext()) {
			Object nextValue = value.next();
			for(Object keyTemp :  keyTreeSet) {
				if(periodicFrequencyMap.get(keyTemp) == nextValue) {
					sortedArray[i] = keyTemp.toString();
					i++;
				}
			}			
		}
		return sortedArray;
	}
	
	//-----------------------insertAtRightMostNode-------------------------------------
	/***
	 * Inserts character at rightmost node
	 * @param hn1 : tree in which the character needs to be inserted
	 * @param character : the character to insert
	 * @param value : the frequency of the character
	 * @param rootCharName : name of the combined node
	 * @return : returns the huffman tree after inserting the new character at the rightmost node
	 */
	public huffmanNode insertAtRightMostNode(huffmanNode hn1, String character, int value, String rootCharName) {
		
		if(hn1.right == null) {
			
			huffmanNode leftNode = new huffmanNode();
			leftNode.character = hn1.character;
			leftNode.value = hn1.value;
			
			huffmanNode rightNode = new huffmanNode();
			rightNode.character = character;
			rightNode.value = value;
			
			hn1.character = rootCharName;
			hn1.value = leftNode.value + rightNode.value;
			hn1.left=leftNode;
			hn1.right=rightNode;	
			
			return hn1;
		}
		
		//recursively calling the method 
		huffmanNode rootNode = insertAtRightMostNode(hn1.right, character, value, rootCharName);
		hn1.right = rootNode;
		return hn1;
	}
	
	//-----------------------nodePath-------------------------------------
	/***
	 * 
	 * @param node : tree in which the character is present
	 * @param key : the character whose path needs to be found
	 * @param returnString : path with combination of 0's and 1's
	 * @return : returns path of the given key from the tree
	 */
	public boolean nodePath(huffmanNode node, String key, String returnString) {
		
		//If the key is null, then the method returns false
		if(node == null) {
			return false;
		}
		//If key is present at the root of the tree, then the method returns empty string as an output path
		if(node.character.equals(key)) {
			setReturnString(returnString);
			return true;
		}
		//The method runs recursively to the left of the tree and if key is present at the left of the current root node, then the method appends 0 to the path string
		if(nodePath(node.left, key, returnString += 0)) {
			return true;
		}
		
		returnString = returnString.substring(0, returnString.length() - 1);
		
		//The method runs recursively to the right of the tree and if key is present at the right of the current root node, then the method appends 1 to the path string
		if(nodePath(node.right, key, returnString += 1)) {
			return true;
		}
		
		returnString = returnString.substring(0, returnString.length() - 1);
		return false;
	}
	
	//-----------------------searchNode-------------------------------------
	/***
	 * 
	 * @param decodeMapNode : tree in which the character needs be found from the given path
	 * @param expression : the path with combination of 0's and 1's indicating the path of the character in the given tree
	 * @return : returns the character present at the given path
	 */
	public String searchNode(huffmanNode decodeMapNode, String expression) {
		
		for(int temp=0; temp<expression.length(); temp++) {
			char path = expression.charAt(temp);
			
			if(path == '0') {
				decodeMapNode = decodeMapNode.left;
			}
			else {
				decodeMapNode = decodeMapNode.right;
			}
		}
		return decodeMapNode.character;
	}
	
	//-----------------------encode-------------------------------------

	@Override
	/***
	 * this method encodes the given input string by performing three adaptations and rebuilding the tree
	 * @param input_filename : input file name
	 * @param level : level indicates the value till which character can be added to rightmost node before rebuilding the tree again
	 * @param reset : if reset is true then it recomputes the huffman tree with reseted frequencies and if reset is false it recomputes the huffman tree with frequencies by updating in the previous set of frequencies
	 * @param ouput_filename : output file name  
	 * returns : returns true after successfully encoding the file
	 */
	public boolean encode(String input_filename, int level, boolean reset, String output_filename) {

		FileReader inputStream = null;
		int countForNodeName=1;
		int charAscii;
		String inputString="";
		int stages = 0;
		String encodeString="";
		int levelTemp=1;
		
		try {
			inputStream = new FileReader(input_filename);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		int base=2;
	
		periodicFrequencyMap.put("nc", 0);
		periodicFrequencyMap.put("eof", 0);
		
		
		if(encodeMap.isEmpty()) {
			
			huffmanNode hn1 = new huffmanNode();
			hn1.character = "nc";
			hn1.value = 0;
			
			huffmanNode hn2 = new huffmanNode();
			hn2.character = "eof";
			hn2.value = 0;
			
			huffmanNode hn3 = new huffmanNode();
			hn3.character = "|" + countForNodeName;
			countForNodeName++;
			hn3.value = hn1.value + hn2.value;
			hn3.left = hn1;
			hn3.right = hn2;
			
			encodeMap.put(0, hn3);	
		}
		
		
		//outer : for(int levelTemp=1; levelTemp<=level; levelTemp++){
		try {
			outer : while((charAscii=inputStream.read())!=-1) {
				int result=1;
				int characterCount=0;
				
				
				for (int powerTemp = levelTemp; powerTemp != 0; powerTemp--) {
			        result = result * base;   
			    }
				
				
				int resultTempValue=0;
				int decide = resultTempValue;
				for(int resultTemp=resultTempValue; resultTemp<result; resultTemp++) {
					
					if(charAscii !=-1) {
						
						char character = (char) charAscii;
						
						boolean flag=false;
						Integer frequencyCount=1;
						
						//It will check whether the character has come for the first time or does it already exist.
						for(String c : periodicFrequencyMap.keySet()) {
							if(c.equals(Character.toString(character))) {
								flag=true;
								break;
							}
							else {
								flag=false;
							}
						}
						
						//Character already exists therefore the frequency of the character is updated by 1
						if(flag) {
							huffmanNode hn;
							
							frequencyCount = 1 + periodicFrequencyMap.get(Character.toString(character));
							
							periodicFrequencyMap.put(Character.toString(character), frequencyCount);
							
							//Computes the path as a combination of 0’s and 1’s at the node where the character exists
							if(decide == resultTempValue) {
								hn = encodeMap.get(stages);
							
								nodePath(hn, Character.toString(character), returnString);
								
							}
							else {
								nodePath(rootNode, Character.toString(character), returnString);
								
							}
							
							encodeString += returnString  + " ";
							returnString="";
						}
						
						//Character is encountered for the first time therefore the character is inserted to the frequency map and its frequency is updated to 1
						else {
							huffmanNode hn;
							
							periodicFrequencyMap.put(Character.toString(character) , frequencyCount);
							
							//Value of “new character nc” is increased by 1
							int newCharacterTemp = periodicFrequencyMap.get("nc") + 1;
							periodicFrequencyMap.put("nc", newCharacterTemp);
							
							//The new character is added to the rightmost node of the tree and it computes the updated path of “nc”
							if(decide == resultTempValue) {
								hn = encodeMap.get(stages);
							
								nodePath(hn, "nc", returnString);
								rootNode = insertAtRightMostNode(hn, Character.toString(character), frequencyCount, "|" + countForNodeName);
								countForNodeName++;
								decide++;
								
								returnString += Character.toString(character);
								encodeString += returnString  + " ";
								returnString="";
							}
							else {
								rootNode = insertAtRightMostNode(rootNode, Character.toString(character), frequencyCount, "|" + countForNodeName);
								countForNodeName++;
								decide++;
								nodePath(rootNode, "nc", returnString);
								
								returnString += Character.toString(character);
								encodeString += returnString + " ";
								returnString="";
							}
								String s="";
								if(resultTemp +1 == result) {
									codebook1(rootNode, s);
									
								}
								
							}
						if(characterCount +1 <result ) {
							if((charAscii = inputStream.read()) != -1) {
								characterCount++;
							}
							
						}
						
						}						
					else {
						
						HashMap<String, Integer> storedFrequencyMap = new HashMap<String, Integer>();
						storedFrequencyMap.putAll(periodicFrequencyMap);
						
						periodicFrequencyMap.put("eof",1);
						
						huffmanTree ht = new huffmanTree();
						huffmanNode rootNode = new huffmanNode();
						rootNode = ht.rebuildHuffmanTree(periodicFrequencyMap);
						
						HashMap<Integer, huffmanNode> hnTemp = ht.getRebuildEncodeMap();
						
						
						finalRebuildEncodeMap.put(rebuildMapCount, hnTemp.get(0));
						rebuildMapCount++;
						
						String s="";
						codebook1(rootNode, s);
						stages++;
						encodeMap.put(stages, rootNode);
						
						
						periodicFrequencyMap.clear();
						periodicFrequencyMap.put("eof", 0);
						periodicFrequencyMap.put("nc", 0);
						periodicFrequencyMap.putAll(storedFrequencyMap);
						
						break outer;
					}
				}
				
				//The tree is periodically rebuilt
				HashMap<String, Integer> storedFrequencyMap = new HashMap<String, Integer>();
				storedFrequencyMap.putAll(periodicFrequencyMap);
				
				huffmanTree ht = new huffmanTree();
				huffmanNode rootNode = new huffmanNode();
				rootNode = ht.rebuildHuffmanTree(periodicFrequencyMap);
				
			
				HashMap<Integer, huffmanNode> hnTemp = ht.getRebuildEncodeMap();
							
				
				finalRebuildEncodeMap.put(rebuildMapCount, hnTemp.get(0));
				rebuildMapCount++;
				
				String s="";
				codebook1(rootNode, s);
				stages++;
				encodeMap.put(stages, rootNode);
				
				
				periodicFrequencyMap.clear();
				periodicFrequencyMap.put("eof", 0);
				periodicFrequencyMap.put("nc", 0);
				periodicFrequencyMap.putAll(storedFrequencyMap);
				
				if(levelTemp == level) {
					levelTemp=level;
				}
				else {
					levelTemp++;
				}
				
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}			

		int resetValue=0;
		if(reset==false) {
			resetValue=0;
		}
		else {
			resetValue=1;
		}
		
		//Final encoded string is obtained
		encodeString = level + " " + String.valueOf(resetValue) + " " + encodeString;
		
		
		try {
			FileWriter writerObj = new FileWriter("encode_output.txt");
			writerObj.write(encodeString);
			writerObj.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		setCodeBookMap(codeBookMap);
		return true;
	}
	
	public HashMap<Integer, huffmanNode> getFinalRebuildEncodeMap() {
		return finalRebuildEncodeMap;
	}

	public void setFinalRebuildEncodeMap(HashMap<Integer, huffmanNode> finalRebuildEncodeMap) {
		this.finalRebuildEncodeMap = finalRebuildEncodeMap;
	}

	

	//-----------------------decode-------------------------------------
	@Override
	/***
	 * This method decodes the original encoded file
	 * @param input_filename : input file name
	 * @param output_filename : output file name
	 * returns : returns true after successfully decoding the file
	 */
	public boolean decode(String input_filename, String output_filename) {
		String decodeString="";
		String decodedReturnString="";
		FileReader inputStream = null;
		int charAscii;
		HashMap<Integer, huffmanNode> decodeMap = getFinalRebuildEncodeMap();
        int space=0;
        int stages=0;
        int level=1;
        int count=2;
        int last=0;
		
		try {
			inputStream = new FileReader(input_filename);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
		try {
			while((charAscii=inputStream.read())!=-1){
				decodeString += (char)charAscii;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//The method splits the given input encoded string by “ ” (space)
		String[] splitString = decodeString.split(" ");
        
        int decodeLevel = Integer.parseInt(splitString[0]);
        int decodeReset = Integer.parseInt(splitString[1]);
        
        while(splitString.length > count) {
        	int result=1;
        	if(decodeLevel<level) {
        		level=decodeLevel;
        	}
        	int power=level;
        	int base=2;
        	while(power != 0) {
        		result *= base;
        		--power;
        	}
        	last = count + result + space;
        	if(splitString.length < last) {
        		last = splitString.length;
        	}
        	
        	huffmanNode rootTempLeft = new huffmanNode();
        	rootTempLeft.character="nc";
        	rootTempLeft.value=0;
        	
        	huffmanNode rootTempRight = new huffmanNode();
        	rootTempRight.character="eof";
        	rootTempRight.value=0;
        	
        	huffmanNode rootTemp = new huffmanNode();
        	rootTemp.character = "|1";
        	rootTemp.value = 0;
        	rootTemp.left = rootTempLeft;
        	rootTemp.right = rootTempRight;
        	
        	huffmanNode decodeMapNode;
        	if(stages==0) {
        		decodeMapNode = rootTemp;
        	}
        	else {
        		decodeMapNode = decodeMap.get(stages-1);
        	}
        	
                	
        	while (count < last) {
                String expression = splitString[count];
                if (count + 1 < splitString.length) {
                    if (splitString[count + 1].equals("")) {
                    	expression = expression + " ";
                        count++;
                        last++;
                    }
                }

                Boolean flag = true;
                for (int i = 0; i < expression.length(); i++) {
                    flag = Character.isDigit(expression.charAt(i));
                }
                String character = "";

                //The character is not encountered for the first time, the path of 0’s and 1’s is obtained.
                if (flag) {
                	character = searchNode(decodeMapNode, expression);
                }
                //The character is already present in the expression (when the character is encountered for the first time), then it directly returns the character as the decoded value and adds the character to the rightmost node of the tree
                else {
                	character = Character.toString(expression.charAt(expression.length() - 1));
                	insertAtRightMostNode(decodeMapNode, character, 0, "|0");
                }

                decodedReturnString = decodedReturnString + character;
                                
                count++;
            }

            stages++;
            level++;
        }
        
        //Writing decoded string to the output file
        try {
			FileWriter writerObj = new FileWriter("decode_output.txt");
			writerObj.write(decodedReturnString);
			writerObj.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		return true;
	}

	////-----------------------codebook-------------------------------------
	@Override
	/***
	 * this method displays the path of a particular character after the final rebuild is performed
	 */
	public Map<Character, String> codebook() {
		HashMap<Character, String> rebuildTreeMap = getCodeBookMap();
		
		return rebuildTreeMap;
	}
	
	HashMap<Character, String> rebuildTreeMap = new HashMap<Character, String>();
	
	//-----------------------codebook1-------------------------------------
	public void codebook1(huffmanNode root, String s) {
		

		if(root.left == null && root.right == null) {
			
			
			rebuildTreeMap.put((root.character).toCharArray()[0], s);
			setCodeBookMap(rebuildTreeMap);
			return;
		}
		
		codebook1(root.left, s + "0");
		codebook1(root.right, s + "1");
		
	}	
}