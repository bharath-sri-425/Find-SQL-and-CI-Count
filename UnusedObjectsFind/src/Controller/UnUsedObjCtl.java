package Controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Operations.ObjectCheck;

public class UnUsedObjCtl {

	public static Path originalPath;
	public static String programFileName="";
	public static Path outFilePath;
	public static StringBuffer sbAllLines = new StringBuffer();
	public static StringBuffer errorFiles = new StringBuffer();

	static Path fullPath;
	static Path fileName;
	static Path errorName;
	static Path errorPath;

	static ObjectCheck oc=new ObjectCheck();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties prop = new Properties();
			InputStream property = UnUsedObjCtl.class.getClassLoader().getResourceAsStream("config.properties");
			prop.load(property);
			Path pathInJava = Paths.get(prop.getProperty("INPUT_DIR"));
			Path pathOutJava = Paths.get(prop.getProperty("OUTPUT_DIR"));
			try (Stream<Path> walk = Files.walk(pathInJava)) {
				// We want to find only regular files with java
				List<String> javaFileList = walk.filter(Files::isRegularFile)
						.filter(path -> path.toString().toLowerCase().endsWith(".java")).map(x -> x.toString())
						.collect(Collectors.toList());
				
				Path outFile = pathOutJava;
				Path logFilePath = pathOutJava.resolve(outFile);
				fileName = Paths.get("connectionLeak.txt");
				fullPath = logFilePath.resolve(fileName);
				errorName = Paths.get("errorFiles.txt");
				errorPath = logFilePath.resolve(errorName);
				for (String path : javaFileList) {
					originalPath = Paths.get(path);
					if (originalPath.toString().toLowerCase().contains(".java"))
					oc.unusedObjectCheck(originalPath);
				}
				writeFile(sbAllLines,fullPath);
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				errorFiles.append(errorPath.toString()+"\n");
			}
			
	
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			errorFiles.append(errorPath.toString()+"\n");
		}
		errorReadFile(errorFiles,errorPath);
	

}
	public static void writeFile(StringBuffer sbAllLines, Path outPath) {
		// TODO Auto-generated method stub
		try {
			Files.write(outPath, sbAllLines.toString().getBytes(StandardCharsets.ISO_8859_1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void errorReadFile(StringBuffer sbAllLines, Path outPath) {
		// TODO Auto-generated method stub
		try {
			Files.write(outPath, sbAllLines.toString().getBytes(StandardCharsets.ISO_8859_1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	}
