package com.noofSQL;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public class SQLCountJSON {
	public static Path originalPath;
	public static Path outFilePath;
	public static Path fullClosePath;
	public static Path jsonPath;
	public static Path fullPath;
	public static Path fullLogPath;
	public static Path errorLogPath;
	public static Path closeErrorLogPath;
	public static Path noChangePath;
	public static String pgmId = "";
	public static String todayDate = "";
	public static String toolLabel = "";
	public static String toolLabelBegin = "";
	public static String toolLabelEnd = "";
	public static String connectionStatement = "";
	public static String methodName = "";
	public static String dbKey = "";
	public static String codeDbKey = "";
	public static String jsonDbKey = "";
	public static StringBuffer sbAllLines = new StringBuffer();
	public static StringBuffer logpath = new StringBuffer();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Properties prop = new Properties();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss", Locale.ENGLISH);
			LocalDateTime dateTime = LocalDateTime.now();
			InputStream property = SQLCountJSON.class.getClassLoader().getResourceAsStream("config.properties");
			prop.load(property);
			Path pathInJava = Paths.get(prop.getProperty("INPUT_DIR"));
			Path pathOutJava = Paths.get(prop.getProperty("OUTPUT_DIR"));
			try (Stream<Path> walk = Files.walk(pathInJava)) {
				// We want to find only regular files with java
				List<String> javaFileList = walk.filter(Files::isRegularFile)
						.filter(path -> path.toString().toLowerCase().endsWith(".json")).map(x -> x.toString())
						.collect(Collectors.toList());
				for (String path : javaFileList) {

					try {
						originalPath = Paths.get(path);
						String programFileName = "";
						Path subPathOut = originalPath.subpath(3, originalPath.getNameCount() - 1);
						outFilePath = pathOutJava.resolve(subPathOut);
						Path fileName = originalPath.getFileName();
						
						programFileName = originalPath.getFileName().toString();
						pgmId = programFileName;
						fullPath = outFilePath.resolve(fileName);
						List<String> allLines = new ArrayList<String>();

						allLines = Files.readAllLines(originalPath);
						countNoOfSQLFilesandClass(allLines);
						Path outFile = pathOutJava;
						Path logFilePath = pathOutJava.resolve(outFile);
						fileName = Paths.get("SQLCounrJSON.txt");
						fullPath = logFilePath.resolve(fileName);
						toWriteJavaFile(sbAllLines, fullPath);

					}catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						System.out.println(fullPath);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	 private static void countNoOfSQLFilesandClass(List<String> allLines) {
		// TODO Auto-generated method stub
		 try {
			 long noofSqls=allLines.stream().filter(t -> t.trim().contains("\"code\"")).count();
//			 System.out.println(pgmId+"||"+noofSqls);
			 sbAllLines.append(pgmId+"||"+noofSqls+"\n");
		 }catch (Exception e) {
			// TODO: handle exception
//			 System.out.println(pgmId);

		}
		
	}
	private static void toWriteJavaFile(StringBuffer sbAllLines, Path fileOutput) {
			// TODO Auto-generated method stub
			try {
				Files.write(fileOutput, sbAllLines.toString().getBytes(StandardCharsets.ISO_8859_1));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	
}
