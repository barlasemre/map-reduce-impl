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
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

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

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	private static final String TEXT_FILE_DIRECTORY = "input";
	private static final String REDUCED_FILE_DIRECTORY = "reduced";
	private static final int MAP_SIZE = 1024 * 1024 * 100; // 10MB
	private static File folder;
	private static HashMap<String, Integer> mapHash = new HashMap<>();
	private static final Gson gson = new Gson();
	
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

    public static String generateFileName(String count){
        return UUID.randomUUID().toString().replace("-", "") + "_reduce_" + count + ".json";
    }

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


	@Override
	public void run(String... args) throws Exception {
		
		 folder = new File(TEXT_FILE_DIRECTORY);
	     File[] inputFiles = folder.listFiles();
	     map(inputFiles);
	     reduce();
		
		BufferedReader br = null;
		FileReader fr = null;
		Words word = null;
		Words word2 = null;
		folder = new File(REDUCED_FILE_DIRECTORY);
		File[] files = folder.listFiles();
		
		
		JSONObject json = null;
		
		Date date = new Date();
		System.out.println("Baslamadan once: " + date.toString());
		
		for (File file : files) {

			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String sCurrentLine;
			
			while ((sCurrentLine = br.readLine()) != null) {
				JSONParser parser = new JSONParser();
				json = (JSONObject) parser.parse(sCurrentLine);
				System.out.println(json.toString());
				word = new Words(sCurrentLine);
//				word2 = new Words(file);
			}
		}
//	    System.out.println(word.getWordmap());
		wordsRepository.deleteAll();
//
//		// save a couple of customers
		
		Date date4 = new Date();
		System.out.println("DBye yazılmadan once : " + date4.toString());
		
		try {
			wordsRepository.save(word2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("An error occured");
		}
//		wordsRepository.save(new Customer("Bob", "Smith"));
//
		// fetch all customers
		
		Date date2 = new Date();
		System.out.println("DBye yazıldıktan sonra, çekmeden önce: "+date2.toString());
		
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		for (Words words : wordsRepository.findAll()) {
			System.out.println(words.getWordmap());
		}
		System.out.println();
		
		Date date3 = new Date();
		System.out.println("cekildikten sonra : "+date3.toString());
//
//		// fetch an individual customer
//		System.out.println("Customer found with findByFirstName('Alice'):");
//		System.out.println("--------------------------------");
//		System.out.println(repository.findByFirstName("Alice"));
//
//		System.out.println("Customers found with findByLastName('Smith'):");
//		System.out.println("--------------------------------");
//		for (Customer customer : repository.findByLastName("Smith")) {
//			System.out.println(customer);
//		}

	}

}
