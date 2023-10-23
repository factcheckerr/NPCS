package execute;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class ExecuteQuery {
	String endpoint_url;
	String reification_scheme;
	String input_query;
	public ExecuteQuery(String endpoint_url, String reification_scheme, String query) {
		super();
		this.endpoint_url = endpoint_url;
		this.reification_scheme = reification_scheme;
		this.input_query = query;
	}

	 public int execute() throws Exception {
		 int number_of_results=0;
		   // Alternative: connect to a remote repository
if(endpoint_url.contains(":5820"))
{
	 SPARQLRepository repository = new SPARQLRepository(endpoint_url);

		// create a new map of additional http headers
		Map<String, String> headers = new HashMap<String, String>();

		// we set the Accept header to _only_ accept text/plain, forcing the endpoint
		// to use N-Triples as the response format. This overwrites the standard
		// Accept header that RDF4J sends.
		headers.put("Accept", "text/plain");
		repository.setAdditionalHttpHeaders(headers);
		// Repository repo=new SPARQLRepository(sparqlendpoint);
		 //repo.init();
	        // Separate connection to a repository
	        RepositoryConnection connection = repository.getConnection();
	        
	        try {
	        	   TupleQuery query = connection.prepareTupleQuery(input_query);

	    			// A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
	    			try (TupleQueryResult result = query.evaluate()) {
	    				//System.out.println("hello");
	    				// we just iterate over all solutions in the result...
	    				for (BindingSet solution : result) {
	    					number_of_results++;
	    					//System.out.println("hello");
	    					// ... and print out the value of the variable binding for ?s and ?n
	    					//System.out.print ("?v0 = " + solution.getValue("v0"));
	    					//System.out.print ("?v4= " + solution.getValue("v1"));
	    					//System.out.print ("?v4= " + solution.getValue("v2"));
	    				//	System.out.print ("?x = " + solution.getValue("finalprov"));
	    		
	    					//System.out.println("?n = " + solution.getValue("y"));
	    				//	System.out.println();
	    					//System.out.println("?z = " + solution.getValue("z"));
	    				}
	    			}
	        	
	        }
	        finally {
	        	// It is best to close the connection in a finally block
	        	connection.close();
	        }
	
}
else if(endpoint_url.contains(":7200"))
{
	        // Abstract representation of a remote repository accessible over HTTP
	        HTTPRepository repository = new HTTPRepository(endpoint_url);

	        // Separate connection to a repository
	        RepositoryConnection connection = repository.getConnection();
	        try {
	        	   TupleQuery query = connection.prepareTupleQuery(input_query);
                  //    System.out.println(query);
                 //     System.out.println(query);
                      // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
	 	    			try (TupleQueryResult result = query.evaluate()) {
	 	    				//System.out.println("hello");
	 	    				// we just iterate over all solutions in the result...
	 	    				for (BindingSet solution : result) {
	 	    					number_of_results++;
	 	    					//System.out.println("hello");
	 	    					// ... and print out the value of the variable binding for ?s and ?n
	 	    					//System.out.print ("?v0 = " + solution.getValue("v0"));
	 	    					//System.out.print ("?v4= " + solution.getValue("v1"));
	 	    					//System.out.print ("?v4= " + solution.getValue("v2"));
	 	    				//	System.out.print ("?x = " + solution.getValue("finalprov"));
	 	    		
	 	    					//System.out.println("?n = " + solution.getValue("y"));
	 	    				//	System.out.println();
	 	    					//System.out.println("?z = " + solution.getValue("z"));
	 	    				}
	 	    			}
	        	
	        }
	        finally {
	        	// It is best to close the connection in a finally block
	        	connection.close();
	        }
}

	 return number_of_results;       
	 }
}
