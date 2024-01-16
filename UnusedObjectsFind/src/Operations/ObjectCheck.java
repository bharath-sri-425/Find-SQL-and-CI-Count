package Operations;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashSet;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import Controller.UnUsedObjCtl;

public class ObjectCheck extends UnUsedObjCtl {
	public static List<MethodDeclaration> methodDeclare = new ArrayList<>();
	public static List<VariableDeclarationExpr> initilizationVariable = new ArrayList<>();

	public void unusedObjectCheck(Path originalPath) {
		try {
			// TODO Auto-generated method stub
			String filePath = "";
			filePath = originalPath.toString();
			File file = new File(filePath);
			CompilationUnit methodCu = StaticJavaParser.parse(file);
			String imports = methodCu.getImports().toString();
			if (imports.contains("import panacea.DBEngine.DBExecutor;")
					&& imports.contains("import panacea.DBEngine.DBEngineException;")) {
				methodCu.getAllContainedComments().forEach(Comment::remove);
				methodDeclare = listMethod(methodCu);
				for (MethodDeclaration method : methodDeclare) {
					String[] MethodLines;
					String methodName = method.getNameAsString();
					MethodLines = method.toString().split("\\r\\n");
					List<String> methodList = Arrays.asList(MethodLines);
					methodList.stream().filter(a -> a.contains("openConnection();")).map(a -> a.strip()).distinct()
							.collect(Collectors.toList());
					List<String> varDeclare = methodList.stream()
							.filter(a -> (a.trim().contains("PreparedStatement") || a.trim().contains("Connection")
									|| a.trim().contains("ResultSet")) && !(a.contains("closeConnection")))
							.map(x -> x.strip()).collect(Collectors.toList());

					varDeclare = varDeclare.stream().filter(a -> !a.toString().startsWith("//")).distinct()
							.collect(Collectors.toList());
					List<String> closeList = methodList.stream()
							.filter(a -> a.contains(".close();") || a.contains("closeConnection();"))
							.filter(a -> !a.strip().startsWith("//")).map(a -> a.strip().replace(".close();", ""))
							.collect(Collectors.toList());
					List<String> onlyVaList = varDeclare.stream().filter(xx -> !xx.contains("openConnection();"))
							.map(x -> x.replace("ResultSet", "").replace("=", "").replace("=", "").replace(";", "")
									.replace("null", "").replace("Connection", "").replace("PreparedStatement", ""))
							.map(a -> a.strip()).collect(Collectors.toList());
					List<String> onlyColseList = closeList.stream().distinct().collect(Collectors.toList());
					onlyVaList.stream().filter(x -> !onlyColseList.contains(x)).collect(Collectors.toList());
					sbAllLines.append(inilizationVar(methodList, onlyVaList, onlyColseList, methodName, filePath));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			errorFiles.append(originalPath.toString() + "\n");
		}

	}

	private StringBuffer inilizationVar(List<String> MethodLines, List<String> varDeclare, List<String> onlyColseList,
			String methodName, String filePath) {
		StringBuffer sb = new StringBuffer();
		for (String var : varDeclare) {
			sb = new StringBuffer();
			List<String> usedObj = new ArrayList<>();
			Predicate<? super String> varContains = methodLine -> methodLine.contains(var)
					&& !methodLine.contains("null");
			Predicate<? super String> commentFilter = vars -> !vars.strip().startsWith("//");
			Predicate<? super String> notEqlAndGet = methodLine -> methodLine.contains("=")
					&& !methodLine.contains(".get");

			usedObj = MethodLines.stream().filter(varContains).filter(commentFilter).filter(notEqlAndGet)
					.map(x -> x.strip().toString()).collect(Collectors.toList());
			if (usedObj.size() == 0) {
				if (onlyColseList.contains(var)) {

					sb.append(filePath + "\t" + methodName + "\t Comment the statement : " + var + "\n");
					if (onlyColseList.contains("_pstmt")) {
						// sbAllLines.append("Comment the close statement : _pstmt"+"\n");
						sb.append(filePath + "\t" + methodName + "\t Comment the statement : _pstmt" + "\n");
					}
				}
			}
		}
		List<String> openClose = MethodLines.stream()
				.filter(a -> a.contains("openConnection();") || a.contains("closeConnection();"))
				.filter(v -> !v.contains("=")).filter(a -> !a.strip().contains("//")).map(a -> a.strip())
				.collect(Collectors.toList());
		if (openClose.size() > 0) {
			sb.append(filePath + "\t" + methodName + "\t Open Connection and Close Connection Methods " + openClose
					+ "\n");
		}
		StringBuffer sbs = new StringBuffer();
		IntStream.range(1, MethodLines.size())
				.filter(i -> MethodLines.get(i).contains(".close()") && !MethodLines.contains("if"))
				.filter(i -> !MethodLines.get(i).strip().startsWith("//"))
				.filter(i -> !MethodLines.get(i - 1).contains("if"))
				.forEach(value -> sbs.append(MethodLines.get(value).strip().toString() + ","));
		if (!(sbs.toString() == null || sbs.toString().isEmpty())) {
			sb.append(filePath + "\t" + methodName + "\t Statements not checked by null : " + sbs.toString() + "\n");
		}
		return sb;
	}

	private static class MethodVisitor extends VoidVisitorAdapter<Set<String>> {
		@Override
		public void visit(MethodDeclaration n, Set<String> usedObjects) {
			super.visit(n, usedObjects);
			// Visit method body to find used object names
			n.accept(new ObjectVisitor(), usedObjects);
		}
	}

	private List<MethodDeclaration> listMethod(CompilationUnit methodCu) {
		// TODO Auto-generated method stub

		List<MethodDeclaration> method = new ArrayList<>();
		try {
			methodCu.accept(new VoidVisitorAdapter<Void>() {
				@Override
				public void visit(MethodDeclaration md, Void arg) {
					super.visit(md, arg);
					method.add(md);
					methodCu.remove(md);
				}

			}, null);
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		return method;
	}

	private static class ObjectVisitor extends VoidVisitorAdapter<Set<String>> {
		@Override
		public void visit(ObjectCreationExpr n, Set<String> usedObjects) {
			super.visit(n, usedObjects);
			// Extract the object name and add it to the usedObjects set
			usedObjects.add(n.getType().getNameAsString());
		}
	}

}
