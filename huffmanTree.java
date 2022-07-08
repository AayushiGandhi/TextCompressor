import java.util.HashMap;


class huffmanNode{
	int value;
	String character;
	
	huffmanNode left;
	huffmanNode right;
}

public class huffmanTree {

	HashMap<String, huffmanNode> treeMap = new HashMap<String, huffmanNode>();
	int rebuildStage=0;
	HashMap<Integer, huffmanNode> rebuildEncodeMap = new HashMap<Integer, huffmanNode>();
	String tempKey="";

	//--------------------rebuildHuffmanTree----------------------------
	/***
	 * This method rebuilds the tree after completion of first adaptation
	 * @param frequencyMap : Map with character as key and frequencies of the character as values
	 * @return : returns updated tree after completion of rebuilt
	 */
	public huffmanNode rebuildHuffmanTree(HashMap<String, Integer> frequencyMap) {
		
		int countForNodeName=1;
	
		huffmanNode root = new huffmanNode();
	
		String[] sortedArray;
		FileCompressorClass obj = new FileCompressorClass();
		sortedArray = obj.sortMap(frequencyMap);
		
		while(frequencyMap.size() > 1){
			
			if(treeMap.isEmpty()) {
				int count=0;
				
				huffmanNode hn1 = new huffmanNode();
				hn1.character = sortedArray[count];
				hn1.value = frequencyMap.get(sortedArray[count]);
				frequencyMap.remove(sortedArray[count]);
				count++;
				
				huffmanNode hn2 = new huffmanNode();
				hn2.character = sortedArray[count];
				hn2.value = frequencyMap.get(sortedArray[count]);
				frequencyMap.remove(sortedArray[count]);
				count++;
				
				huffmanNode hn3 = new huffmanNode();
				hn3.left = hn1;
				hn3.right = hn2;
				hn3.character = "|" + countForNodeName;
				countForNodeName++;
				hn3.value = hn1.value + hn2.value;
				
				treeMap.put(hn3.character, hn3);
				
				frequencyMap.put(hn3.character, hn3.value);
				
				sortedArray = obj.sortMap(frequencyMap);
			}
			else {
				int count=0;
				
			
				huffmanNode hn1 = new huffmanNode();
				
				if(treeMap.containsKey(sortedArray[0])) {
					hn1 = treeMap.get(sortedArray[0]);
					treeMap.remove(sortedArray[0]);
				}
				else {
					hn1.character = sortedArray[0];
					hn1.value = frequencyMap.get(sortedArray[0]);
				}
				frequencyMap.remove(sortedArray[0]);
				
				huffmanNode hn2 = new huffmanNode();
				if(treeMap.containsKey(sortedArray[1])) {
					hn2 = treeMap.get(sortedArray[1]);
					treeMap.remove(sortedArray[1]);
				}
				else {
					hn2.character = sortedArray[1];
					hn2.value = frequencyMap.get(sortedArray[1]);
				}
				frequencyMap.remove(sortedArray[1]);
				
				huffmanNode hn3 = new huffmanNode();
				hn3.left = hn1;
				hn3.right = hn2;
				hn3.character = "|" + countForNodeName;
				countForNodeName++;
				hn3.value = hn1.value + hn2.value;
				
				root = hn3;
				
				
				tempKey = hn3.character;
				treeMap.put(hn3.character, hn3);
				
				
				frequencyMap.put(hn3.character, hn3.value);
								
				sortedArray = obj.sortMap(frequencyMap);
				count++;
			}
		}
		rebuildEncodeMap.put(rebuildStage, treeMap.get(tempKey));
		setRebuildEncodeMap(rebuildEncodeMap);
		rebuildStage++;
		return root;
	}

	public HashMap<Integer, huffmanNode> getRebuildEncodeMap() {
		return rebuildEncodeMap;
	}

	public void setRebuildEncodeMap(HashMap<Integer, huffmanNode> rebuildEncodeMap) {
		this.rebuildEncodeMap = rebuildEncodeMap;
	}
}
