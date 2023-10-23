package npcs.rewriting.sparqlstar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
import org.eclipse.rdf4j.query.algebra.Group;
import org.eclipse.rdf4j.query.algebra.GroupConcat;
import org.eclipse.rdf4j.query.algebra.GroupElem;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Str;
import org.eclipse.rdf4j.query.algebra.TripleRef;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Union;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.sparql.TripleRefCollector;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;

public class RewriteStatementPattern {
	private QueryModelNode statementptrn;
	private TupleExpr tupleExpr;
	public int statement_number;
	
	public Var unionrightleft;
	public int join_number;
	public int union_number;
	public int optional_number;
	public int getUnion_number() {
		return union_number;
	}

	public void setUnion_number(int union_number) {
		this.union_number = union_number;
	}

	public int getJoin_number() {
		return join_number;
	}

	public void setJoin_number(int join_number) {
		this.join_number = join_number;
	}

	public int getStatement_number() {
		return statement_number;
	}

	public void setStatement_number(int statement_number) {
		this.statement_number = statement_number;
	}
	private static ValueFactory vf= SimpleValueFactory.getInstance();;
	List<Var> triple_pattern_varibles=new ArrayList<Var>();
	
	Var triple_pattern_provenenace_varible=new Var();
	Var triple_pattern_bind_varible=new Var();
	Var triple_pattern_group_varible=new Var();
	Var triple_pattern_optional_variable=new Var();
	public Var getTriple_pattern_optional_variable() {
		return triple_pattern_optional_variable;
	}

	public void setTriple_pattern_optional_variable(Var triple_pattern_optional_variable) {
		this.triple_pattern_optional_variable = triple_pattern_optional_variable;
	}

	public Var getTriple_patterns_union_varible() {
		return triple_patterns_union_varible;
	}

	public void setTriple_patterns_union_varible(Var triple_patterns_union_varible) {
		this.triple_patterns_union_varible = triple_patterns_union_varible;
	}
	Var triple_patterns_join_varible=new Var();
	public Var getTriple_patterns_join_varible() {
		return triple_patterns_join_varible;
	}

	public void setTriple_patterns_join_varible(Var triple_patterns_join_varible) {
		this.triple_patterns_join_varible = triple_patterns_join_varible;
	}
	Var triple_patterns_union_varible=new Var();
	/*RewriteStatementPattern(QueryModelNode node)
	{
		this.statementptrn=node;
		if(node instanceof StatementPattern)
		{
		
      
		this.triple_pattern_varibles.addAll(getVariableVars(getStatementPatternVars((TupleExpr)node.clone())));
		}
		else if(node instanceof Join)
		{
			String sjoinnum="fjoin"+(join_number);
			 triple_patterns_join_varible.setName(sjoinnum);
			 
			
		}
		else if(node instanceof Union)
		{
			String sjoinnum="funion"+(union_number);
			 triple_patterns_union_varible.setName(sjoinnum);
			 
			
		}
		
	}
*/
	RewriteStatementPattern(QueryModelNode node)
	{
		
		this.statementptrn=node;	
	}
	RewriteStatementPattern(QueryModelNode node,int st)
	{
		
		 unionrightleft=new Var();
		this.statementptrn=node;
		if(node instanceof StatementPattern)
		{
			//System.out.println("yahanaaaaannaaaaa"+node);
		this.statement_number=st;
		this.triple_pattern_varibles.addAll(getVariableVars(getStatementPatternVars((TupleExpr)node.clone())));
		
		//Var statement_Pattern_Provenance=new Var();
		//Var statement_Join_Provenance=new Var();
		String sprovnum="fprov"+statement_number;
		String sbindnum="fbind"+(statement_number);
		String sgroupnum="fgroupconcat"+(statement_number);
	
		triple_pattern_provenenace_varible.setName(sprovnum);
		triple_pattern_bind_varible.setName(sbindnum);
		triple_pattern_group_varible.setName(sgroupnum);
		}
		else if(node instanceof Join)
		{
			 this.join_number=st;
			String sjoinnum="fjoin"+(join_number);
			 triple_patterns_join_varible.setName(sjoinnum);
			
			 
			
		}
		else if(node instanceof Union)
		{
			 this.union_number=st;
			String sjoinnum="funion"+(union_number);
			 triple_patterns_union_varible.setName(sjoinnum);
			
			 
			
		}
		else if(node instanceof LeftJoin)
		{
			 this.union_number=st;
				String sjoinnum="foptional"+(optional_number);
				triple_pattern_optional_variable.setName(sjoinnum);
		}
	}
	

	public QueryModelNode getStatementptrn() {
		return statementptrn;
	}


	public void setStatementptrn(QueryModelNode statementptrn) {
		this.statementptrn = statementptrn;
	}


	public TupleExpr getTupleExpr() {
		return tupleExpr;
	}


	public void setTupleExpr(TupleExpr tupleExpr) {
		this.tupleExpr = tupleExpr;
	}


	public List<Var> getTriple_pattern_varibles() {
		return this.triple_pattern_varibles;
	}


	public void setTriple_pattern_varibles(List<Var> triple_pattern_varibles) {
		this.triple_pattern_varibles = triple_pattern_varibles;
	}


	public Var getTriple_pattern_provenenace_varible() {
		return triple_pattern_provenenace_varible;
	}


	public void setTriple_pattern_provenenace_varible(Var triple_pattern_provenenace_varible) {
		this.triple_pattern_provenenace_varible = triple_pattern_provenenace_varible;
	}


	public Var getTriple_pattern_bind_varible() {
		return triple_pattern_bind_varible;
	}


	public void setTriple_pattern_bind_varible(Var triple_pattern_bind_varible) {
		this.triple_pattern_bind_varible = triple_pattern_bind_varible;
	}


	public Var getTriple_pattern_group_varible() {
		return triple_pattern_group_varible;
	}


	public void setTriple_pattern_group_varible(Var triple_pattern_group_varible) {
		this.triple_pattern_group_varible = triple_pattern_group_varible;
	}
	public FunctionCall ProvAggSum(Var pelemlistforgroup)
	{FunctionCall fn=new FunctionCall("http://www.w3.org/2005/xpath-functions#concat");
	 
	   // ExtensionElem extElem = new ExtensionElem(fn,"prov");
	  ValueConstant vc = new ValueConstant(vf.createLiteral("⊕("));
	  fn.addArg(vc);
	  fn.addArg(pelemlistforgroup);
	  ValueConstant vc2 = new ValueConstant(vf.createLiteral(")"));
	  fn.addArg(vc2);
	  
		return fn;
		
	}
	
	public QueryModelNode applyBind(QueryModelNode qmnode,List<Var> bindinglist,Var join)
	{
		Extension anonymousExtension1111 = new Extension((TupleExpr) qmnode);
	 	 ExtensionElem extElem111 = new ExtensionElem(ProvProd(bindinglist),join.getName());
	 	anonymousExtension1111.addElement(extElem111);

	 	return anonymousExtension1111;
	}
	public QueryModelNode applyBindforUnion(QueryModelNode qmnode,List<Var> bindinglist,Var join)
	{
		Extension anonymousExtension1111 = new Extension((TupleExpr) qmnode);
	 	 ExtensionElem extElem111 = new ExtensionElem(ProvSum(bindinglist),join.getName());
	 	anonymousExtension1111.addElement(extElem111);

	 	return anonymousExtension1111;
	}
	public FunctionCall ProvProd(List<Var> variables)
	{FunctionCall fn=new FunctionCall("http://www.w3.org/2005/xpath-functions#concat");
	  ValueConstant vc = new ValueConstant(vf.createLiteral("(⊗"));
	  fn.addArg(vc);
	  
	  for(Var v:variables)
	  {
		  fn.addArg(new Str(v));
		  ValueConstant vc3 = new ValueConstant(vf.createLiteral(","));
		  fn.addArg(vc3);
		
	  }
	  ValueConstant vc2 = new ValueConstant(vf.createLiteral(")"));
	  fn.addArg(vc2);
	  
		return fn;
		
	}
	public FunctionCall ProvSum(List<Var> variables)
	{FunctionCall fn=new FunctionCall("http://www.w3.org/2005/xpath-functions#concat");
	  ValueConstant vc = new ValueConstant(vf.createLiteral("(⊕"));
	  fn.addArg(vc);
	  
	  for(Var v:variables)
	  {
		  fn.addArg(v);
		  ValueConstant vc3 = new ValueConstant(vf.createLiteral(","));
		  fn.addArg(vc3);
		
	  }
	  ValueConstant vc2 = new ValueConstant(vf.createLiteral(")"));
	  fn.addArg(vc2);
	  
		return fn;
		
	}

	public QueryModelNode handleUnOptimedStatementPattern(QueryModelNode node,List<Var> eachstatementVariables,Var statement_Join_Provenance) throws VisitorException{
		
		Var triplePattern_Concat_Provenance=new Var();
		triplePattern_Concat_Provenance.setName("fgroup"+statement_number);
		////System.out.println("node changes"+node);
		
		QueryModelNode bindExtension = applyBind(node.clone(),eachstatementVariables,triplePattern_Concat_Provenance);
	//	singlestatementsprovenanceVariables= new ArrayList<Var>();
		  Var anonVarnotused = createAnonVar();
			 FunctionCall fn=ProvAggSum(anonVarnotused);
		  QueryModelNode node1;
		
			return node1 = applyGroupingAndProjection2(bindExtension,triplePattern_Concat_Provenance,statement_Join_Provenance,eachstatementVariables,anonVarnotused,fn);
		
	}
	public QueryModelNode handleStatementPatternUnOpt(QueryModelNode node,Var statement_Pattern_Provenance)
	{
		
	
			//This is only needed in case we need al the variables in query
			//totalstatementsVariables.addAll(getVariableVars(getStatementPatternVars((TupleExpr)node)));
			
			
			
			////System.out.println(totalstatementsVariables);
			QueryModelNode node1= rewriteStatementPattern(node.clone(),statement_Pattern_Provenance);
			//node.replaceWith(node1);
			
			
			 //totalstatementsprovenanceVariables.add(statement_Pattern_Provenance);
			 //singlestatementsprovenanceVariables.add(statement_Pattern_Provenance);
			 
			// //System.out.println("hshshshsshshshhshs"+totalstatementsprovenanceVariables);
			
			 //node1.visit(this);
			 return node1;
		
		
	}
	
	
	public QueryModelNode handleStatementPattern(QueryModelNode node,Var statement_Pattern_Provenance)
	{
		
	/*	String sprovnum="fprov"+statement_number;
		statement_Pattern_Provenance.setName(sprovnum);
		String ns=node.toString();
		if (ns.contains("http://example.org/occurrenceOf"))
		{
			
		}
		else
		{
			//This is only needed in case we need al the variables in query
			//totalstatementsVariables.addAll(getVariableVars(getStatementPatternVars((TupleExpr)node)));
			
			
		*/	
		//	//System.out.println(totalstatementsVariables);
			QueryModelNode node1= rewriteStatementPattern(node.clone(),statement_Pattern_Provenance);
		//	node.replaceWith(node1);
			
			
			// totalstatementsprovenanceVariables.add(statement_Pattern_Provenance);
			 
			// //System.out.println("hshshshsshshshhshs"+totalstatementsprovenanceVariables);
			 this.statement_number=this.statement_number+1;
		//	 node1.visit(this);
			 return node1;
		//}
	//	return null;
		
	}
	
	public QueryModelNode rewriteStatementPattern(QueryModelNode node, Var v)
	{
	
		QueryModelNode node1= Reify(node,v);

		return node1;
	}
	
	
	public QueryModelNode Reify(QueryModelNode node,Var sprovnum)
	{
		
		 QueryModelNode currentnode = node.clone();
		  		  
	       TupleExpr tupleExpr = (TupleExpr)currentnode;
	       StatementPattern rightJoin = (StatementPattern) tupleExpr;

	       
	       TripleRef leftJoinArg = new TripleRef();
	       leftJoinArg.setSubjectVar(rightJoin.getSubjectVar());
	       leftJoinArg.setObjectVar(rightJoin.getObjectVar());
	       leftJoinArg.setPredicateVar(rightJoin.getPredicateVar());
	       				
	       ValueConstant predicate=new ValueConstant(vf.createIRI("http://example.org/occurrenceOf"));
	       rightJoin.setPredicateVar(createConstVar(predicate.getValue()));

	       
	       Var anonVar = createAnonVar();
	       leftJoinArg.setExprVar(anonVar);
	       rightJoin.setSubjectVar(anonVar);
	       rightJoin.setObjectVar(sprovnum);
	       				
	      	Join newjoin = new Join();    				
	       	newjoin.setLeftArg(leftJoinArg);
	       	newjoin.setRightArg(rightJoin);
	       		
		return newjoin;
		
	}

	public QueryModelNode rewrite(QueryModelNode node) throws VisitorException {
	
	//System.out.println("i am inside rewritess");
		//System.out.println(node);
		
		//System.out.println(triple_pattern_varibles);
		
			Var statement_single_Pattern_Provenance=new Var();
			QueryModelNode nodechanged=handleStatementPatternUnOpt(node.clone(),triple_pattern_provenenace_varible);
			//QueryModelNode rewriten=handleStatementPattern(node,statement_single_Pattern_Provenance);
			//QueryModelNode xyz=handleUnOptimedStatementPattern(nodechanged.clone(),triple_pattern_varibles,triple_pattern_group_varible);
			 statement_number=statement_number+1;
			return nodechanged;
		
		

		//return null;
	}

	public QueryModelNode handlesinglepatternBindAndGroup(QueryModelNode node, List<Var> totaljoinstatementsprovenanceVariables, List<Var> variablesstart, Var finalprovennacevariable) throws VisitorException {
		 QueryModelNode node1=null;
		 QueryModelNode bindExtension=null;
		  Var anonVarnotused = createAnonVar();
			 FunctionCall fn=ProvAggSum(anonVarnotused);
			try {
		 /*	List<Var> triple_pattern_varibles=new ArrayList<Var>();
		
		Var triple_pattern_provenenace_varible=new Var();
		Var triple_pattern_bind_varible=new Var();
		Var triple_pattern_group_varible=new Var();*/
		
		 if(node instanceof Join)
		 {
		bindExtension=applyBind(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_join_varible);	
		
		//System.out.println("QWTNFFHJRFJFJF");
		//System.out.println(triple_patterns_join_varible);
		//System.out.println(finalprovennacevariable);
		//System.out.println(totaljoinstatementsprovenanceVariables);
		node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_join_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
		//this.join_number=number+1;
		 }
		 else if(node instanceof Union)
		 {
			 bindExtension=applyBindforUnion(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_union_varible);	 
				node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_union_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
			
			//	 this.union_number=number+1;
				
		 }
		 else if(node instanceof LeftJoin)
		 {
			 
			 //System.out.println("gghhhhhhhhjjjjjjjjjjjjjjjjjjjjjjjjj");	
		//System.out.println(totaljoinstatementsprovenanceVariables);	 
		//System.out.println(totaljoinstatementsprovenanceVariables);	 	 
				//
		if (!totaljoinstatementsprovenanceVariables.isEmpty()) {
			node1 = applyGroupingAndProjectionMINUS(node.clone(),totaljoinstatementsprovenanceVariables.get(0),totaljoinstatementsprovenanceVariables.get(1),finalprovennacevariable,variablesstart,anonVarnotused);
		  //  this.optional_number=number+1;
		    
			// Now, 'firstElement' contains the first element of the list.
		} else {
		    // Handle the case where the list is empty.
		}		
			 
		 }
	
			unionrightleft=finalprovennacevariable;
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return node;
		return node1;
	}
	/*changed in wikidata code
	 * public QueryModelNode handlesinglepatternBindAndGroup(QueryModelNode node, List<Var> variablesstart, Var finalprovennacevariable) throws VisitorException {
	 
		 
		
		
		QueryModelNode node1=null;
		 QueryModelNode bindExtension=null;
	
		 if(node instanceof Join)
		 {
		bindExtension=applyBind(node.clone(),triple_pattern_varibles,triple_pattern_group_varible);
		 }
		 else if(node instanceof Union)
		 {
			 bindExtension=applyBindforUnion(node.clone(),triple_pattern_varibles,triple_pattern_group_varible);	  
		 }
		Var anonVarnotused = createAnonVar();
			 FunctionCall fn=ProvAggSum(anonVarnotused);
		
		try {
			
			
			//System.out.println("PENPENPENEPE");			
			
			node1 = applyGroupingAndProjection2(bindExtension,triple_pattern_group_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
		
			
			

			unionrightleft=finalprovennacevariable;		     
		  
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return node;
		return node1;
	}
*/
	public QueryModelNode handlemultiplejoinBindAndGroup(QueryModelNode node, List<Var> totaljoinstatementsprovenanceVariables, List<Var> variablesstart, Var finalprovennacevariable,int number) throws VisitorException {
		 QueryModelNode node1=null;
		 QueryModelNode bindExtension=null;
		  Var anonVarnotused = createAnonVar();
			 FunctionCall fn=ProvAggSum(anonVarnotused);
			try {
		 /*	List<Var> triple_pattern_varibles=new ArrayList<Var>();
		
		Var triple_pattern_provenenace_varible=new Var();
		Var triple_pattern_bind_varible=new Var();
		Var triple_pattern_group_varible=new Var();*/
		
		 if(node instanceof Join)
		 {
		bindExtension=applyBind(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_join_varible);	
		node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_join_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
		this.join_number=number+1;
		 }
		 else if(node instanceof Union)
		 {
			 bindExtension=applyBindforUnion(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_union_varible);	 
				node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_union_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
			
				 this.union_number=number+1;
				
		 }
		 else if(node instanceof LeftJoin)
		 {
			 
			 //System.out.println("gghhhhhhhhjjjjjjjjjjjjjjjjjjjjjjjjj");	
		//System.out.println(totaljoinstatementsprovenanceVariables);	 
		//System.out.println(totaljoinstatementsprovenanceVariables);	 	 
				//
		if (!totaljoinstatementsprovenanceVariables.isEmpty()) {
			node1 = applyGroupingAndProjectionMINUS(node.clone(),totaljoinstatementsprovenanceVariables.get(0),totaljoinstatementsprovenanceVariables.get(1),finalprovennacevariable,variablesstart,anonVarnotused);
		    this.optional_number=number+1;
		    
			// Now, 'firstElement' contains the first element of the list.
		} else {
		    // Handle the case where the list is empty.
		}		
			 
		 }
	
			unionrightleft=finalprovennacevariable;
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return node;
		return node1;
	}
	public int getOptional_number() {
		return optional_number;
	}

	public void setOptional_number(int optional_number) {
		this.optional_number = optional_number;
	}

	/*public QueryModelNode handlemultiplejoinBindAndGroupOptional(QueryModelNode node, List<Var> totaljoinstatementsprovenanceVariables, List<Var> variablesstart, Var finalprovennacevariable,int number) throws VisitorException {
		 QueryModelNode node1=null;
		 QueryModelNode bindExtension=null;
		  Var anonVarnotused = createAnonVar();
			 FunctionCall fn=ProvAggSum(anonVarnotused);
			try {
		
		
		 if(node instanceof Join)
		 {
		bindExtension=applyBind(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_join_varible);	
		node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_join_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
		this.join_number=number+1;
		 }
		 else if(node instanceof Union)
		 {
			 bindExtension=applyBindforUnion(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_union_varible);	 
				node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_union_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
			
				 this.union_number=number+1;
				
		 }
	
			unionrightleft=finalprovennacevariable;
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return node;
		return node1;
	}*/
	public QueryModelNode applyGroupingAndProjectionMINUS(QueryModelNode node,Var optionalleftVariable,Var optionalrightVariable,Var finalprov,List<Var> statementVars, Var anonVarnotused) throws VisitorException
	{
		
		//System.out.println("I am inside MIUD");
		//System.out.println(anonVarnotused);
		List<Var> difflist=new ArrayList<>();
		difflist.add(optionalleftVariable);
		difflist.add(anonVarnotused);
		
		 FunctionCall functionCall=ProvSDiff(difflist);
		//System.out.println(functionCall);
		 ProjectionElemList pel=new ProjectionElemList();
	      
	       for (Var sp : statementVars) {
	    	   ProjectionElem vy = new ProjectionElem();
			     //  v.setTargetName("prov"+number);
			       vy.setSourceName(sp.getName());
			       vy.setTargetName(sp.getName());
		   		////System.out.println(sp);
			       pel.addElement(vy);
	             		 }
	       ProjectionElem vyx = new ProjectionElem();
	    		   vyx.setSourceName(finalprov.getName());
	       vyx.setTargetName(finalprov.getName());	   
	       pel.addElement(vyx);
		
	       //px.setProjectionElemList(pel);
	       //pel.setParentNode(px);
		//return px;
		//Var provenanceVariable=new Var(sprovnum);
		  //Var anonVarnotused = createAnonVar();
	 	  FunctionCall fn=new FunctionCall("http://www.w3.org/2005/xpath-functions#concat");
	 	 //GroupConcat groupcon=new GroupConcat(fn);
	 	 GroupConcat groupcon=new GroupConcat(optionalrightVariable);
	 	 //GroupConcat groupcon=new GroupConcat(fn);
	 	 fn.addArg(optionalrightVariable);
	 	 GroupElem grpElem = new GroupElem(anonVarnotused.getName(), groupcon);
	     	Group g = new Group((TupleExpr) node);
	      	g.addGroupElement(grpElem);
			//g.setGroupBindingNames(stptrnbindings.));
	      	 for (Var sp : statementVars) {
 
	      		g.addGroupBindingName(sp.getName());
             		 }
			    g.addGroupBindingName(optionalleftVariable.getName());  	 
	
	      	 
	      Extension anonymousExtension = new Extension((TupleExpr) g);
	      ExtensionElem extElem = new ExtensionElem(groupcon,anonVarnotused.getName());
	   		anonymousExtension.addElement(extElem);
	   		anonymousExtension.setArg(g);
	   		Extension topextension = new Extension((TupleExpr) anonymousExtension);
	   		//System.out.println("*************************");
	   		//System.out.println(functionCall);
	   		
	   		ExtensionElem extElemfortop = new ExtensionElem(functionCall,finalprov.getName());
	   		
	   		topextension.addElement(extElemfortop);
	   		topextension.setArg(anonymousExtension);
	   		vyx.setSourceExpression(extElemfortop);
	   	 Projection px=new Projection((TupleExpr) topextension,pel);
		return px;
	}
	public QueryModelNode handlemultiplejoinBindAndGroupUnion(QueryModelNode node, List<Var> totaljoinstatementsprovenanceVariables, List<Var> variablesstart, Var finalprovennacevariable,int join_number) throws VisitorException {
		 QueryModelNode node1=null;
		 
		 /*	List<Var> triple_pattern_varibles=new ArrayList<Var>();
		 
		Var triple_pattern_provenenace_varible=new Var();
		Var triple_pattern_bind_varible=new Var();
		Var triple_pattern_group_varible=new Var();*/
		 QueryModelNode bindExtension=applyBind(node.clone(),totaljoinstatementsprovenanceVariables,triple_patterns_union_varible);	
		
		 //System.out.println("££££££££££££££££££££££££££££££££££££££££££££££££££££££");
		 
		 //System.out.println(bindExtension);
		 
		 
		 
		 //QueryModelNode bindExtension=applyBind(node.clone(),triple_pattern_varibles,triple_pattern_group_varible);
		  Var anonVarnotused = createAnonVar();
			 FunctionCall fn=ProvAggSum(anonVarnotused);
		
		try {
			node1 = applyGroupingAndProjection2(bindExtension,triple_patterns_union_varible,finalprovennacevariable,variablesstart,anonVarnotused,fn);
		
			
			this.union_number=union_number+1;

			     
		  
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return node;
		return node1;
	}
	
	public QueryModelNode rewritemore(QueryModelNode node) throws VisitorException {
		
		//Var statement_Pattern_Provenance=new Var();
		//Var statement_Join_Provenance=new Var();
		String sprovnum="fprov"+statement_number;
		String sbindnum="fbind"+(statement_number);
		String sgroupnum="fgroupconcat"+(statement_number);
		
		triple_pattern_provenenace_varible.setName(sprovnum);
		triple_pattern_bind_varible.setName(sbindnum);
		triple_pattern_group_varible.setName(sgroupnum);
	//System.out.println("i am inside rewrite");
		//System.out.println(node);
		
		//System.out.println(triple_pattern_varibles);
		String ns=node.toString();
	
			Var statement_single_Pattern_Provenance=new Var();
			QueryModelNode nodechanged=handleStatementPatternUnOpt(node.clone(),triple_pattern_provenenace_varible);
			//QueryModelNode rewriten=handleStatementPattern(node,statement_single_Pattern_Provenance);
			QueryModelNode xyz=handleUnOptimedStatementPattern(nodechanged.clone(),triple_pattern_varibles,triple_pattern_group_varible);
			 statement_number=statement_number+1;
			return xyz;
		}
		

	//	return null;
	
	public QueryModelNode applyGroupingAndProjection2(QueryModelNode node,Var provenanceVariable,Var finalprov,List<Var> statementVars, Var anonVarnotused,FunctionCall functionCall) throws VisitorException
	{
		
		//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		//System.out.println(node);
		//System.out.println(provenanceVariable);
		//System.out.println(finalprov);
		//System.out.println(statementVars);
		//System.out.println(anonVarnotused);
		//System.out.println(functionCall);
		 ProjectionElemList pel=new ProjectionElemList();
	      
	       for (Var sp : statementVars) {
	    	   ProjectionElem vy = new ProjectionElem();
			     //  v.setTargetName("prov"+number);
			       vy.setSourceName(sp.getName());
			       vy.setTargetName(sp.getName());
		   		////System.out.println(sp);
			       pel.addElement(vy);
	             		 }
	       ProjectionElem vyx = new ProjectionElem();
	    		   vyx.setSourceName(finalprov.getName());
	       vyx.setTargetName(finalprov.getName());	   
	       pel.addElement(vyx);
		
	       //px.setProjectionElemList(pel);
	       //pel.setParentNode(px);
		//return px;
		//Var provenanceVariable=new Var(sprovnum);
		  //Var anonVarnotused = createAnonVar();
	 	  FunctionCall fn=new FunctionCall("http://www.w3.org/2005/xpath-functions#concat");
	 	 //GroupConcat groupcon=new GroupConcat(fn);
	 	 GroupConcat groupcon=new GroupConcat(provenanceVariable);
	 	 //GroupConcat groupcon=new GroupConcat(fn);
	 	 fn.addArg(provenanceVariable);
	 	 GroupElem grpElem = new GroupElem(anonVarnotused.getName(), groupcon);
	     	Group g = new Group((TupleExpr) node);
	      	g.addGroupElement(grpElem);
			//g.setGroupBindingNames(stptrnbindings.));
	      	 for (Var sp : statementVars) {
 
	      		g.addGroupBindingName(sp.getName());
             		 }
			      	 
	
	      	 
	      Extension anonymousExtension = new Extension((TupleExpr) g);
	      ExtensionElem extElem = new ExtensionElem(groupcon,anonVarnotused.getName());
	   		anonymousExtension.addElement(extElem);
	   		anonymousExtension.setArg(g);
	   		Extension topextension = new Extension((TupleExpr) anonymousExtension);
	   		ExtensionElem extElemfortop = new ExtensionElem(functionCall,finalprov.getName());
	   		
	   		topextension.addElement(extElemfortop);
	   		topextension.setArg(anonymousExtension);
	   		vyx.setSourceExpression(extElemfortop);
	   	 Projection px=new Projection((TupleExpr) topextension,pel);
	   //	 statement_number=statement_number+1;
		return px;
	}
	public FunctionCall ProvSDiff(List<Var> variables)
	{FunctionCall fn=new FunctionCall("http://www.w3.org/2005/xpath-functions#concat");
	  ValueConstant vc = new ValueConstant(vf.createLiteral("(⊖"));
	  fn.addArg(vc);
	  
	  for(Var v:variables)
	  {
		  fn.addArg(v);
		  ValueConstant vc3 = new ValueConstant(vf.createLiteral(","));
		  fn.addArg(vc3);
		
	  }
	  ValueConstant vc2 = new ValueConstant(vf.createLiteral(")"));
	  fn.addArg(vc2);
	  
		return fn;
		
	}
	protected Var createAnonVar() {
		// dashes ('-') in the generated UUID are replaced with underscores so
		// the
		// varname
		// remains compatible with the SPARQL grammar. See SES-2310.
		final Var var = new Var("_anon_" + java.util.UUID.randomUUID().toString().replaceAll("-", "_"));
		var.setAnonymous(true);
		return var;
	}
	public static Var createConstVar(Value value) {
		String varName = getConstVarName(value);
		Var var = new Var(varName);
		var.setConstant(true);
		var.setAnonymous(true);
		var.setValue(value);
		return var;
	}

	public static String getConstVarName(Value value) {
		if (value == null) {
			throw new IllegalArgumentException("value can not be null");
		}

		// We use toHexString to get a more compact stringrep.
		String uniqueStringForValue = Integer.toHexString(value.stringValue().hashCode());

		if (value instanceof Literal) {
			uniqueStringForValue += "_lit";

			// we need to append datatype and/or language tag to ensure a unique
			// var name (see SES-1927)
			Literal lit = (Literal) value;
			if (lit.getDatatype() != null) {
				uniqueStringForValue += "_" + Integer.toHexString(lit.getDatatype().hashCode());
			}
			if (lit.getLanguage() != null) {
				uniqueStringForValue += "_" + Integer.toHexString(lit.getLanguage().hashCode());
			}
		} else if (value instanceof BNode) {
			uniqueStringForValue += "_node";
		} else {
			uniqueStringForValue += "_uri";
		}

		return "_const_" + uniqueStringForValue;
	}
	protected List<Var> getStatementPatternVars(TupleExpr tupleExpr) {
		////System.out.println("In getStatementPatternVars function");
		List<StatementPattern> stPatterns = StatementPatternCollector.process(tupleExpr);
		List<Var> varList = new ArrayList<>(stPatterns.size() * 4);
		for (StatementPattern sp : stPatterns) {
		//if(isConstant(sp.getPredicateVar()))
		//{

			////System.out.println("I am variable function");
			sp.getVars(varList);
		//}
			
		}
		return varList;
	}
	protected List<Var> getConstantVars(Iterable<Var> vars) {
		List<Var> constantVars = new ArrayList<>();

		for (Var var : vars) {
			if (var.hasValue()) {
				constantVars.add(var);
			}
		}

		return constantVars;
	}
	protected List<Var> getVariableVars(Iterable<Var> vars) {
		List<Var> constantVars = new ArrayList<>();

		for (Var var : vars) {
			if (!var.hasValue()) {
			if(!var.getName().startsWith("_anon"))
				
				constantVars.add(var);
			}
		}

		return constantVars;
	}
	@SuppressWarnings("unchecked")
	public List<Var> getStatementPatternAndTriplePatternVars(TupleExpr tupleExpr) {
		////System.out.println("In getStatementPatternVars function");
		List<StatementPattern> stPatterns = StatementPatternCollector.process(tupleExpr);
		Map<String, Object> trPatterns = TripleRefCollector.process(tupleExpr);
		List<Var> varList = new ArrayList<>(stPatterns.size() * 4);
		for (StatementPattern sp : stPatterns) {
		//if(isConstant(sp.getPredicateVar()))
		//{
			//isConstant(
			////System.out.println("I am variable function");
			sp.getVars(varList);
		//}
			
		}
		////System.out.println(trPatterns);
		for (Map.Entry<String, Object> entry : trPatterns.entrySet()) {
		    String key = entry.getKey();
		    Object value = entry.getValue();
           TripleRef trf=(TripleRef) value;
           
		    // Your processing logic for each key-value pair goes here
		   // //System.out.println("Key: " + key);
		   // //System.out.println("Value: " + trf.getVarList());
		    for (Var sp2 : trf.getVarList()) {
		    	
		    	varList.add(sp2);
		    }
		    
		    
		}
		
		
		/*for (TripleRef sp : trPatterns) {
			//if(isConstant(sp.getPredicateVar()))
			//{

				////System.out.println("I am variable function");
				sp.getVars(varList);
			//}
				
			}
			*/
		return varList;
	}
	
}
