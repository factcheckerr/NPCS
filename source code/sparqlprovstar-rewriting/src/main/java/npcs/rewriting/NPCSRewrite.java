package npcs.rewriting;

import java.util.Map;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.IncompatibleOperationException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedDescribeQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.ParsedUpdate;
import org.eclipse.rdf4j.query.parser.QueryParser;
import org.eclipse.rdf4j.query.parser.sparql.BaseDeclProcessor;
import org.eclipse.rdf4j.query.parser.sparql.BlankNodeVarProcessor;
import org.eclipse.rdf4j.query.parser.sparql.DatasetDeclProcessor;
import org.eclipse.rdf4j.query.parser.sparql.PrefixDeclProcessor;
import org.eclipse.rdf4j.query.parser.sparql.StringEscapesProcessor;
import org.eclipse.rdf4j.query.parser.sparql.TupleExprBuilder;
import org.eclipse.rdf4j.query.parser.sparql.WildcardProjectionProcessor;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTAskQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTConstructQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTDescribeQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTSelectQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.Node;
import org.eclipse.rdf4j.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

import npcs.rewriting.namedgraph.NamedGraph;
import npcs.rewriting.sparqlstar.*;
import npcs.rewriting.standard.Standard;
import npcs.rewriting.wikidata.Wikidata;
//import npcs.rewriting.wikidatareal.FinalWikiRef;
import npcs.rewriting.wikidatareal.WikidataReal;
//import rewriting.wikidata.FinalWikiRef;
//import wikidatacode.FinalWikiRef;
/*
import renderer.SparqlQueryRender;
import rewriting.wikidata.FinalWikiRef;
import sparqlprovstar.rewriting.NamedGraphRewriting;
import sparqlprovstar.rewriting.Rdfstar_reification;
import sparqlprovstar.rewriting.StandardRewriting;
import sparqlprovstar.rewriting.WikidataRealRewriting;
import sparqlprovstar.rewriting.WikidataRewriting;
import sparqlprovstar.rewriting.optional.Rdfstaroptional;
import sparqlprovstar.rewriting.optional.WikidataRealOptional;
*/
/*public class NPCSRewrite {

}
*/
public class NPCSRewrite implements QueryParser {
	
	public String refication_scheme="SPARQL";
	public String qrst=null;
	
	

	public NPCSRewrite(String refication_scheme) {
		super();
		this.refication_scheme = refication_scheme;
	}

	public ParsedUpdate parseUpdate(String updateStr, String baseURI) throws MalformedQueryException {
		// TODO Auto-generated method stub
		return null;
	}

	private TupleExpr buildQueryModel(Node qc) throws MalformedQueryException {
		TupleExprBuilder tupleExprBuilder = new TupleExprBuilder(SimpleValueFactory.getInstance());
		try {
			return (TupleExpr) qc.jjtAccept(tupleExprBuilder, null);
		} catch (VisitorException e) {
			throw new MalformedQueryException(e.getMessage(), e);
		}
	}

	public ParsedQuery parseQuery(String queryStr, String baseURI) throws MalformedQueryException {
		qrst=queryStr;
		// TODO Auto-generated method stub
		try {
		ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(queryStr);
		StringEscapesProcessor.process(qc);	
		BaseDeclProcessor.process(qc, baseURI);
		Map<String, String> prefixes = PrefixDeclProcessor.process(qc);
         System.out.println(prefixes);
		WildcardProjectionProcessor.process(qc);
		BlankNodeVarProcessor.process(qc);
		if (qc.containsQuery()) {

		
			TupleExpr tupleExpr = buildQueryModel(qc);
			//System.out.println("before refication");
			//System.out.println(tupleExpr);
			
			if(this.refication_scheme.equals("SPARQL_Star"))
			{
				SparqlStar rsp=new SparqlStar(null,null,tupleExpr);
				tupleExpr=rsp.rewrite(qc, tupleExpr);
			//	Rdfstar_reification rsp=new Rdfstar_reification(null,null,tupleExpr);
			//	tupleExpr=rsp.rewrite(qc, tupleExpr);
				
			}
			else if(this.refication_scheme.equals("Standard"))
			{
				Standard rsp=new Standard(null,null,tupleExpr);
				//	FinalWikiRef rsp=new FinalWikiRef(null,null,tupleExpr);
							tupleExpr=rsp.rewrite(qc, tupleExpr);	
			//	StandardRewriting rsp=new StandardRewriting(null,null,tupleExpr);
			//	tupleExpr=rsp.rewrite(qc, tupleExpr);
			}
			else if(this.refication_scheme.equals("Namedgraph"))
			{
				NamedGraph rsp=new NamedGraph(null,null,tupleExpr);
				tupleExpr=rsp.rewrite(qc, tupleExpr);
			//	NamedGraphRewriting rsp=new NamedGraphRewriting(null,null,tupleExpr);
			//	tupleExpr=rsp.rewrite(qc, tupleExpr);
				
			}
			else if(this.refication_scheme.equals("Wikidata"))
			{
				Wikidata rsp=new Wikidata(null,null,tupleExpr);
			//	FinalWikiRef rsp=new FinalWikiRef(null,null,tupleExpr);
						tupleExpr=rsp.rewrite(qc, tupleExpr);	
				
			}
			else if(this.refication_scheme.equals("Wikidatareal"))
			{
				WikidataReal rsp=new WikidataReal(null,null,tupleExpr);
			//	FinalWikiRef rsp=new FinalWikiRef(null,null,tupleExpr);
						tupleExpr=rsp.rewrite(qc, tupleExpr);	
			//	WikidataRealRewriting rsp=new WikidataRealRewriting(null,null,tupleExpr);
			//		tupleExpr=rsp.rewrite(qc, tupleExpr);
				
			}
			else if(this.refication_scheme.equals("SPARQL"))
			{
			
			}
			else
			{
				System.out.println("Unknown reification scheme"+refication_scheme);
				
			}
				
			

			ParsedQuery query;

			ASTQuery queryNode = qc.getQuery();
			if (queryNode instanceof ASTSelectQuery) {
				query = new ParsedTupleQuery(queryStr, tupleExpr);				
			} else if (queryNode instanceof ASTConstructQuery) {
				query = new ParsedGraphQuery(queryStr, tupleExpr, prefixes);
			} else if (queryNode instanceof ASTAskQuery) {
				query = new ParsedBooleanQuery(queryStr, tupleExpr);
			} else if (queryNode instanceof ASTDescribeQuery) {
				query = new ParsedDescribeQuery(queryStr, tupleExpr, prefixes);
			} else {
				throw new RuntimeException("Unexpected query type: " + queryNode.getClass());
			} 

			// Handle dataset declaration
			Dataset dataset = DatasetDeclProcessor.process(qc);
			if (dataset != null) {
				query.setDataset(dataset);
			}

			return query;
		} else {
			throw new IncompatibleOperationException("supplied string is not a query operation");
		}
	} catch (Exception | TokenMgrError e) {
		throw new MalformedQueryException(e.getMessage(), e);
	}
	}
	public void checktupleexpr(TupleExpr tupleExpr) throws Exception
	{
		String qrstr="SELECT ?var3\r\n"
				+ "WHERE {\r\n"
				+ "<http://www.wikidata.org/entity/Q137109> ?var1 ?var3 . OPTIONAL { ?var1 <http://www.wikidata.org/prop/direct/P373> ?var2 . }\r\n"
				+ "}";
	//	ParsedQuery query = new ParsedTupleQuery(qrst, tupleExpr);	
	//	SparqlQueryRender spd2 = new SparqlQueryRender();				
	//	System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&");
	//	String rewritten_query=spd2.render(query);
		 // System.out.println(miunsoptionalp);
		//  System.out.println(miunsoptional);
		
	//	System.out.println(qrstr);
	 //    System.out.println(rewritten_query);
		
	}


}
