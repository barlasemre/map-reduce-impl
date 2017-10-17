package hello;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Words {

	public Map<String, Integer> wordMap;
	public Map<String, Integer> sortedWordMap;
	public String word;
	public int number;
	
	public Words() {}
	
	public Words(String json) {
	
		wordMap = new HashMap<String, Integer>();
		
		json = json.substring(1, json.length()-1);
		String[] pairs = json.split(",");
		for (int i=0;i<pairs.length;i++) {
		    String pair = pairs[i];
		    String[] keyValue = pair.split(":");
		    wordMap.put(keyValue[0], Integer.valueOf(keyValue[1]));
		}
		
		 sortedWordMap =
				 wordMap.entrySet().stream()
	                        .sorted(Map.Entry.comparingByValue())
	                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
	                                (e1, e2) -> e1, LinkedHashMap::new));
		
		setSortedWordMap(sortedWordMap);
		
	}
	
	public Words(File file) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Integer> wordMap = null;
		
		try {
            wordMap = mapper.readValue(file, new TypeReference<Map<String, Integer>>() {
            });
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		sortedWordMap =
				 wordMap.entrySet().stream()
	                        .sorted(Map.Entry.comparingByValue())
	                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
	                                (e1, e2) -> e1, LinkedHashMap::new));
		
		setSortedWordMap(sortedWordMap);
	}

	public Map<String, Integer> getWordmap() {
		return wordMap;
	}

	public Map<String, Integer> getSortedWordMap() {
		return sortedWordMap;
	}


	public void setSortedWordMap(Map<String, Integer> sortedWordMap) {
		this.sortedWordMap = sortedWordMap;
	}


	public Map<String, Integer> getWordMap() {
		return wordMap;
	}


	
	
}
