package dk.simpleconcept.jolienuxt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import jolie.net.CommMessage;
import jolie.runtime.FaultException;
import jolie.runtime.JavaService;
import jolie.runtime.Value;

public class JolieNuxt  extends JavaService {
	
	Map<String, Object> data = new HashMap<String, Object>();
	ObjectMapper oMapper = new ObjectMapper();
	String[] functions = {"use", "render"};
	
	public Value Render( Value request )
	{
		String template_path = request.getFirstChild( "file" ).strValue();
		
		File template = new File(template_path);
		String path = template.getPath().replaceFirst(template.getName(), "");
		
		Value response = Value.create();
		
		try {
			Path content = parseFile(path, template.getName());
			
			System.out.println("Content: " + content.toString());
			
			Path html = render(new File(content.toString()));
			
			String tempFileContent = Files
	                .lines(html, StandardCharsets.UTF_8)
	                .collect(Collectors.joining(System.lineSeparator()));
	        System.out.println(tempFileContent);
	        
	        response.getFirstChild( "response" ).setValue( html.toString() );
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return response;
	}
	
	private Path parseFile(String path, String template) throws IOException
	{
		List<String> content = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(path + template))) {
			String line;
			Boolean preamble = true;
		    while ((line = br.readLine()) != null) {
		        if(line.equals("---")) {
		        	preamble = false;
		        	continue;
		        }
		        
		        if(preamble) {
		        	try {
						parseLine(path, line);
					} catch (Exception e) {
						System.out.println(e);
						break;
					}
		        } else {
		        	content.add(line);
		        }
		    }
		}
		
		Path tempFile = Files.createTempFile(null, null);
		
		Files.write(tempFile, content, StandardOpenOption.APPEND);
		
		String tempFileContent = Files
                .lines(tempFile, StandardCharsets.UTF_8)
                .collect(Collectors.joining(System.lineSeparator()));
        System.out.println(tempFileContent);
		
		System.out.println(data);
		
		return tempFile;
	}
	
	private void parseLine(String path, String line) throws Exception
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
	
	private Path render(File file) throws IOException
	{
		Path tempFile = Files.createTempFile(null, null);
		
		String path = file.getPath().replaceFirst(file.getName(), "");
		
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
		cfg.setClassForTemplateLoading(JolieNuxt.class, "/");
		cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
		cfg.setLogTemplateExceptions(false);
		
		try {
			cfg.setDirectoryForTemplateLoading(new File(path));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("HERE 0");
			e1.printStackTrace();
		}
		
		try {
			//Load template from source folder
			Template template = cfg.getTemplate(file.getName());
			
			// File output
			Writer new_file = new FileWriter (new File(tempFile.toString()));
			template.process(data, new_file);
			
			new_file.flush();
			new_file.close();
			
		} catch (IOException e) {
			System.out.println("HERE 1");
			e.printStackTrace();
		} catch (TemplateException e) {
			System.out.println("HERE 2");
			e.printStackTrace();
		}

		return tempFile;
	}

}

class MyTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, java.io.Writer out)
            throws TemplateException {
    	System.out.print("Missing variable found: ");
    	
    	System.out.println(StringUtils.substringBetween(te.getFTLInstructionStack(), "{", "}"));
        //try {
        //    out.write("[ERROR: " + te.getMessage() + "]");
        //} catch (IOException e) {
        //    throw new TemplateException("Failed to print error message. Cause: " + e, env);
        //}
    }
}
