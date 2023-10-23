package npcs;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.eclipse.rdf4j.query.parser.ParsedQuery;

import execute.ExecuteQuery;
import renderer.SparqlQueryRender;
import npcs.rewriting.NPCSRewrite;
//import npcs.rewriting.SparqlProvStarRewrite;

//import renderer.SparqlQueryRender;
//import sparqlprovstar.rewriting.SparqlProvStarRewrite;

public class App {
	static String rdfstar="SPARQL_Star";
	static String namedgraph="Namedgraph";
	static String wikidata="Wikidata";
	static String wikidatareal="Wikidatareal";
	static String sparql="SPARQL";
	static String standard="Standard";
	
	private static String readQueryFromPath(String path) throws FileNotFoundException {
		String strLine="";
		try {

			
			String strFileDirectoryPath =path;	
			FileInputStream fstream = new FileInputStream(strFileDirectoryPath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String st;
			// Read File Line By Line
			while ((st = br.readLine()) != null) {
				strLine=strLine+st+"\n";
				System.out.println(st);
			}
			in.close();
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		return strLine;
	}

	    public static void readFolderFiles(String folderPath) throws FileNotFoundException {
	        // Specify the path to the folder you want to read
	       // String folderPath = "/path/to/your/folder";

	        // Create a File object representing the folder
	        File folder = new File(folderPath);

	        // Check if the path is indeed a directory
	        if (folder.isDirectory()) {
	            // List all files in the folder
	            File[] files = folder.listFiles();

	            if (files != null) {
	                // Iterate through the files and print their names
	                for (File file : files) {
	                    if (file.isFile()) {
	                        System.out.println("File: " + file.getName());
	                        readQueryFromPath(folderPath+file.getName());
	                        
	                    }
	                }
	            } else {
	                System.err.println("Failed to list files in the folder.");
	            }
	        } else {
	            System.err.println("The specified path is not a directory.");
	        }
	    }

	public static void main(String[] args) throws Exception {
	
		
	String pathh=args[1];
	String reification_scheme=args[0];
	String endpoint_url=args[2];
	String times=args[3];
	String query=readQueryFromPath(pathh);
    if(!(endpoint_url.equals(null)))
    {
    	double sum=0;
   		System.out.println(endpoint_url);
   		System.out.println(times);
   		for(int i=0;i<Integer.parseInt(times);i++)
   		{
   			
   			long start = System.currentTimeMillis();
   			NPCSRewrite sparqlrewrite=new NPCSRewrite(reification_scheme);
   			
   			ParsedQuery querytree = sparqlrewrite.parseQuery(query,null);
   			
   			SparqlQueryRender spd2 = new SparqlQueryRender();				
   			
   			String rewritten_query=spd2.render(querytree);
   			long startonlyexec = System.currentTimeMillis();
   		  //  System.out.println(rewritten_query);
   			ExecuteQuery exec_query=new ExecuteQuery(endpoint_url,reification_scheme,rewritten_query);
   			int number_of_results=exec_query.execute();
   			long finish = System.currentTimeMillis();
   	    	sum=sum+(finish - start);
   	    	
   	   //  System.out.println("rewritten Query"+rewritten_query);
   	    //	System.out.println("rewritten Query"+rewritten_query);
   	    	System.out.println("Query"+pathh);
   	  System.out.println("Number of Results"+number_of_results);
   	System.out.println("Rewriten+Execution Time"+(finish - start));
	System.out.println("Only Execution Time"+(finish - startonlyexec));   	
   	    	
   		}
   		
   		
    
	
    }
    else
    {
	
	
	NPCSRewrite sparqlrewrite=new NPCSRewrite(reification_scheme);
	
	ParsedQuery querytree = sparqlrewrite.parseQuery(query,null);
	
	SparqlQueryRender spd2 = new SparqlQueryRender();				
	
	String rewritten_query=spd2.render(querytree);

    System.out.println(rewritten_query);
   
	
    }
		
	}

}
