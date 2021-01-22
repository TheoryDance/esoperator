package com.theorydance.esoperator.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import lombok.Data;

@Data
public class TextFile {
	
	public static void main(String[] args) {
		TextFile textFile = new TextFile("d:/tmp/test.txt", false);
		textFile.append("hello world");
		textFile.append("你好");
		System.out.println(textFile.read());
		System.out.println(textFile.read());
		System.out.println(textFile.read());
		System.out.println(textFile.read());
		textFile.close();
	}

	private File file;
	private PrintWriter printWriter;
	private BufferedReader br;
	
	public TextFile(String path, boolean append) {
		this(new File(path), append);
	}
	public TextFile(File file, boolean append) {
		this.file = file;
		File dir = file.getParentFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				return;
			}
		}
		try {
			FileWriter fw = new FileWriter(file, append);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			printWriter = new PrintWriter(fw, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void append(String line) {
		printWriter.append(line);
		printWriter.flush();
	}
	
	public String read() {
		try {
			String line = br.readLine();
			return line;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void close() {
		closeIO(br);
		closeIO(printWriter);
	}
	
	private void closeIO(Closeable stream) {
		if(stream!=null) {
			try {
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
