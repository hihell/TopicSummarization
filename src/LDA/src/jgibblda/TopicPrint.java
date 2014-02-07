package LDA.src.jgibblda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class TopicPrint {

	public static void main(String[] args)throws Exception{
		
	BufferedReader reader = new BufferedReader(new InputStreamReader(
			new FileInputStream("newdocs.dat.model-final.twords"), "UTF-8"));
	
	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream("Topic-Word.txt"), "UTF-8"));
	
	String line;
	
	line = reader.readLine();
	
	   while(line != null)
	   {
		   writer.write(line + "\t");
		   line = reader.readLine();
		   if(line == null)
		   {
			   break;
		   }
		   int count  = 0;
		   while(line != null && !line.contains("Topic"))
		   {
			   line = line.replace("\t", "");
			   count ++;
			   writer.write(line.substring(0,line.indexOf(" ")) + "\t");
			   line = reader.readLine();
		   }
		  System.out.println(count);
		  writer.write("\r\n");
	   }
	      writer.close( );
	}
}
