package dk.simpleconcept.jolienuxt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import jolie.runtime.JavaService;

public class JolieNuxt  extends JavaService {
	
	static Map<String, Object> data = new HashMap<String, Object>();
	static ObjectMapper oMapper = new ObjectMapper();
	static String[] functions = {"use", "render"};
	
	public static void main(String [] args) throws IOException 
	{
		// ARG
		//File template = new File("template.jtf");
		
		parseFile("C:\\Coding\\thesis\\new_demo\\", "template.jtf");
	}
	
	private static void parseFile(String path, String template) throws IOException
	{
		try(BufferedReader br = new BufferedReader(new FileReader(path + template))) {
			String line;
		    while ((line = br.readLine()) != null) {
		       if(line.equals("---")) {
		    	   break;
		       }
		       
				try {
					parseLine(path, line);
				} catch (Exception e) {
					System.out.println(e);
					break;
				}
		    }
		}
		
		System.out.println(data);
	}
	
	private static void parseLine(String path, String line) throws Exception
	{
		String[] parts = line.split(" ");
		String function = "";
		String format = "";
		String location = "";
		
		try {
			function = parts[0];
			format = parts[1];
			location = parts[2];
		} catch (Exception e) {
			System.out.println("Wrong input");
			throw new Exception("Wrong input");
		}
		
		if( ! Arrays.asList(functions).contains(function) ) {
			throw new Exception("Wrong beginning of line.");
		}
		
		try {
			String file_name = path + location + "." + format;
			System.out.println("FILE: " + file_name);
			
    		Map<String, Object> globalMap = oMapper.readValue(new File(file_name), Map.class);
    		
    		data.putAll(globalMap);
    		
        } catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
