package com.raidomatic.build;

import org.eclipse.core.resources.*;
//import org.jruby.*;
//import org.jruby.javasupport.*;
import java.util.*;
import java.io.*;

public class RubyBuilder {
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// This expects one argument, which is the Eclipse project path, relative to the workspace
		String projectPath = args[0];
		
//		Ruby runtime = JavaEmbedUtils.initialize(new ArrayList());
//		RubyRuntimeAdapter evaler = JavaEmbedUtils.newRuntimeAdapter();
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath);
		IFolder rubySource = project.getFolder("WebContent/rb");
		IFolder rubyBuild = project.getFolder("WebContent/rb-build");
		for(IResource member : rubySource.members()) {
			if(member.getType() == IResource.FILE) {
				IFile rubyFile = (IFile)member;
				if(rubyFile.getName().toLowerCase().endsWith(".rb")) {
					System.out.println(rubyFile.getName() + " is ruby file!");
					
					
					boolean firstLine = true;
					String packageName = "";
					StringBuilder rubyCode = new StringBuilder();
					BufferedReader reader = new BufferedReader(new InputStreamReader(rubyFile.getContents()));
					while(reader.ready()) {
						String line = reader.readLine();
						if(firstLine) {
							if(line.startsWith("#package ")) {
								packageName = line.substring(9);
							}
							firstLine = false;
						}
						rubyCode.append(line + "\n");
					}
					reader.close();
					
					File buildDir = new File("C:\\ruby-test\\build");
					File fsRubyFile = new File("c:\\ruby-test\\build\\" + rubyFile.getName());
					if(fsRubyFile.exists()) {
						fsRubyFile.delete();
					}
					fsRubyFile.createNewFile();
					FileWriter writer = new FileWriter(fsRubyFile);
					writer.write(rubyCode.toString());
					writer.close();
					
					Runtime sysRuntime = Runtime.getRuntime();
					Process convertProc = sysRuntime.exec(new String[] {
							"C:\\jruby-1.5.2\\bin\\jrubyc.bat",
							"--java",
							fsRubyFile.getName()
					}, null, buildDir);
					//convert.directory();
					//out.println("\t" + convert.command());
					
					StreamGobbler errorGobbler = new StreamGobbler(convertProc.getErrorStream(), "Error");
					StreamGobbler outputGobbler = new StreamGobbler(convertProc.getInputStream(), "Output");
					errorGobbler.start();
					outputGobbler.start();

					convertProc.waitFor();
					
					for(File buildFile : buildDir.listFiles()) {
						if(buildFile.getName().toLowerCase().endsWith(".java")) {
							System.out.println("Found Java file: " + buildFile.getAbsolutePath());

							IFile workspaceBuildFile = rubyBuild.getFile(getPathForPackage(packageName, rubyBuild) + buildFile.getName());
							StringBuilder sourceCode = new StringBuilder();
							if(!packageName.equals("")) {
								sourceCode.append("package " + packageName + ";");
							}
							sourceCode.append(getFileContents(buildFile));
							
							ByteArrayInputStream bytes = new ByteArrayInputStream(sourceCode.toString().getBytes());
							
							if(!workspaceBuildFile.exists()) {
								workspaceBuildFile.create(bytes, 0, null);
							} else {
								workspaceBuildFile.setContents(bytes, 0, null);
							}
							
							buildFile.delete();
							
						}
					}
					
					fsRubyFile.delete();
				}
			}
		}
	}
	
	public static String getPathForPackage(String packageName, IFolder rubyBuild) throws Exception {
		String path = packageName.replace(".", "\\");
		
		String currentPath = "";
		StringTokenizer tokens = new StringTokenizer(path, "\\");
		while(tokens.hasMoreTokens()) {
			String part = tokens.nextToken();
			currentPath += part;
			IFolder thisPart = rubyBuild.getFolder(currentPath);
			if(!thisPart.exists()) {
				thisPart.create(0, false, null);
			}
			currentPath += "\\";
		}
		
		return currentPath;
	}
	
	public static String getFileContents(File buildFile) throws Exception {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(buildFile));
		while(reader.ready()) {
			builder.append(reader.readLine());
		}
		reader.close();
		return builder.toString();
	}
}

class StreamGobbler extends Thread {
	InputStream is;
	String type;
	
	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}
	
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while((line = br.readLine()) != null) {
				System.out.println(type + "> " + line);
			}
		} catch(Exception ioe) {
			ioe.printStackTrace();
		}
	}
}