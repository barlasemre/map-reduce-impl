package hello;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SpringBootApplication
public class Application implements CommandLineRunner {

	
	@Autowired
	private WordsRepository wordsRepository;

	private static final String TEXT_FILE_DIRECTORY = "input";
	private static final String REDUCED_FILE_DIRECTORY = "reduced";
	private static final int MAP_SIZE = 1024 * 1024 * 100; // 10MB
	private static File folder;
	private static File mapFolder;
	private static HashMap<String, Integer> mapHash = new HashMap<>();
	private static final Gson gson = new Gson();
	
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	
	@Override
	public void run(String... args) throws Exception {
		
		FileUtils.cleanDirectory(new File(REDUCED_FILE_DIRECTORY)); 
		
		 folder = new File(TEXT_FILE_DIRECTORY);
	     File[] inputFiles = folder.listFiles();
	     map(inputFiles);
	     reduce();
		
		wordsRepository.deleteAll();
		
		Words word = createObjectToWriteMongo();
		
		try {
			wordsRepository.save(word);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		for (Words words : wordsRepository.findAll()) {
			
			Entry<String, Integer> firstEntry = words.getSortedWordMap().entrySet().iterator().next();
			
			Iterator<Map.Entry<String,Integer>> iterator = words.getSortedWordMap().entrySet().iterator();
		    Map.Entry<String, Integer> lastEntry = null;
			
		    while (iterator.hasNext()) {
		    	lastEntry = iterator.next();
			}
			
			String firstKey = firstEntry.getKey();
			int firstValue = firstEntry.getValue();
			
			String lastKey = lastEntry.getKey();
			int lastValue = lastEntry.getValue();
			
			System.out.println("Least appeared key ");
			System.out.println("Word " + firstKey + " appeared " + firstValue + " times" );
			System.out.println("-----------------------------------");
			
			System.out.println("Most appeared key ");
			System.out.println("Word " + lastKey + " appeared " + lastValue + " times" );
			System.out.println("-----------------------------------");
		
		}
		
	}

    public static String generateFileName(String count){
        return UUID.randomUUID().toString().replace("-", "") + "_reduce_" + count + ".json";
    }

    /* Mapping Job
     * It counts the words partially, then writes under reduced directory
     * 
     */
    public static void map(File[] inputFiles){
        int bytesCount = 0;
        for (File file : inputFiles) {
            if (file.isFile()) {
                try (BufferedReader br = Files.newBufferedReader(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
                    for (String line = null; (line = br.readLine()) != null; ) {
                        try {
                            String[] split = line.replaceAll("[^\\p{L}\\p{Z}]", "").trim().toLowerCase().split(" ");
                            processLine(split);
                            bytesCount += line.getBytes().length;

                            if (bytesCount > MAP_SIZE) {
                                writeReducedJson(mapHash,"0");
                                mapHash = null;
                                mapHash = new HashMap<>();
                                bytesCount = 0;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        writeReducedJson(mapHash, "0");
    }

    public static void processLine(String[] split){
        Arrays.stream(split).filter(word ->!word.equals("")).sequential().forEach(word->{
            Integer num = mapHash.get(word);
            if(num == null){
                mapHash.put(word,1);
            }else{
                mapHash.put(word,++num);
            }
        });
    }
    
    /*
     * The process function that writes reduced map file into reduced directory 
     */
    public static void writeReducedJson(HashMap<String, Integer> map, String count){
        Path path = Paths.get(REDUCED_FILE_DIRECTORY + "/" + generateFileName(count));
        try {
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String json = gson.toJson(map);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(json);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    
    /*
     * Reduce jobs takes reduced files as input, then combines to big picture from smaller pieces.
     * This process will be last until only 1 file occurs under reduced file directory.
     * 
     */
    public static void reduce(){
        folder = new File(REDUCED_FILE_DIRECTORY);
        File[] reducedFiles = null;
        int count = 1;

        reducedFiles = folder.listFiles();
        while (reducedFiles.length >=2){
            mergeFile(reducedFiles[0], reducedFiles[1], count);
            count++;
            reducedFiles = folder.listFiles();
        }
    }

    /*
     * The process which merges 2 reduced file into 1 combined map file. 
     * This process lasts until all files combined. 
     */
    public static void mergeFile(File file1, File file2, int count){

        StringBuffer first = new StringBuffer();
        StringBuffer second = new StringBuffer();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(file1.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (String line = null; (line = br.readLine()) != null; ) {
                try {
                    first.append(line);
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(file2.getAbsolutePath()), StandardCharsets.UTF_8)) {
            for (String line = null; (line = br.readLine()) != null; ) {
                try {
                    second.append(line);
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            HashMap<String, Integer> firstMap = gson.fromJson(first.toString(), new TypeToken<HashMap<String, Integer>>(){}.getType());
            HashMap<String, Integer> secondMap= gson.fromJson(second.toString(), new TypeToken<HashMap<String, Integer>>(){}.getType());
            firstMap.forEach((k,v)->{
                Integer val = secondMap.get(k);
                if( val == null){
                    secondMap.put(k, v);
                }else{
                    secondMap.remove(k);
                    secondMap.put(k, val + v);
                }
            });

            file1.delete();
            file2.delete();
            writeReducedJson(secondMap,count+"");

        }catch (Exception e){
            file1.delete();
            file2.delete();
            e.printStackTrace();
        }

    }

    public static Words createObjectToWriteMongo() throws IOException {
    	
    	BufferedReader br = null;
		FileReader fr = null;
		Words word = null;
		mapFolder = new File(REDUCED_FILE_DIRECTORY);
		File[] files = mapFolder.listFiles();
		
		JSONObject json = null;
		
		for (File file : files) {

			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String sCurrentLine;
			
			while ((sCurrentLine = br.readLine()) != null) {
				word = new Words(sCurrentLine);
//				word = new Words(file);
			}
		}
		return word;
    	
    	
    }

}
