package LDA.src.jgibblda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileProcess {
	public static void main(String args[]) throws Exception{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream("1012939152_seg.txt"), "UTF-8"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("trndoc.txt"), "UTF-8"));
		
		writer.write("59" + "\n");
		String line = "";
		
		int count = 0;
		
		line = reader.readLine();
		while(line != null)
		{
			String title = reader.readLine();
			writer.write(title);
			String content = reader.readLine();
			writer.write(content + "\n");
			reader.readLine();
			line = reader.readLine();
			count++;
		}
		
		System.out.println(count);
		reader.close();
		writer.close();
		
	
	}
}
