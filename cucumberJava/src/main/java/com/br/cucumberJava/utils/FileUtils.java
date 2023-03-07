package com.br.cucumberJava.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {


	public static String getText(String caminho) throws Exception {
		InputStream stream = new FileInputStream(caminho);
		InputStreamReader streamReader = new InputStreamReader(stream);
		BufferedReader reader = new BufferedReader(streamReader);
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = reader.readLine())!= null) {
			sb.append(line);
		}
		reader.close();
		return sb.toString();
	}
}
