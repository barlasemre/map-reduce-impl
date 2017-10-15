package hello;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.util.Map;
 
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Words {

	public Map<String, Integer> wordmap;
	
	public String word;
	public int number;
	
	public Words() {}
	
	public Words(String json) {
		wordmap = new HashMap<String, Integer>();
		
		json = json.substring(1, json.length()-1);
		String[] pairs = json.split(",");
		for (int i=0;i<pairs.length;i++) {
		    String pair = pairs[i];
		    String[] keyValue = pair.split(":");
		    wordmap.put(keyValue[0], Integer.valueOf(keyValue[1]));
		}
		System.out.println(getWordmap().toString());
		
		
		
	}
	
	public Words(File file) {
		ObjectMapper mapper = new ObjectMapper();

		try {
            Map<String, Integer> wordmap = mapper.readValue(file, new TypeReference<Map<String, Integer>>() {
            });
 
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}

	public Map<String, Integer> getWordmap() {
		return wordmap;
	}

	public void setWordmap(Map<String, Integer> wordmap) {
		this.wordmap = wordmap;
	}
	
	
}
