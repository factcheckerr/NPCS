package npcs.rewriting.sparqlstar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.algebra.Difference;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
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
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ConstantOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.EvaluationStatistics;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SparqlStar extends AbstractQueryModelVisitor<RuntimeException>{
	protected final EvaluationStatistics statistics;
	private static ValueFactory vf=null;
	protected static final Logger logger = LoggerFactory.getLogger(ConstantOptimizer.class);
	protected static EvaluationStrategy strategy = null;
	protected final TupleExpr tupleExpr;
	public int statement_number=0;
	public int join_number=0;
	public int union_number=0;
	public int right_join_number=0;
	public int right_union_number=0;
	public int left_join_number=0;
	public int left_union_number=0;
	public int right_optional_number=0;
	public int optional_number=0;
	public List<StatementPattern> totalstPatterns; 
	public List<Var> totaljoins=new ArrayList<Var>();
	public List<Var> totalunions=new ArrayList<Var>();
	public List<Var> unionrightleftfinalvars=new ArrayList<Var>();
	
	public Difference differnecechange=null;
	
	
	//public int statement_number=0;
	
	List<Var> totaljoinstatementsprovenanceVariables=new ArrayList<Var>();
	
	 // Map<TupleExpr, List<Var>,List<Var>,List<Var>,List<Var> > dataMap = new HashMap<>();
	
	
	final List<ProjectionElemList> projElemLists = new ArrayList<>();
	List<Var> totalstatementsprovenanceVariables=new ArrayList<Var>();
	List<Var> singlestatementsprovenanceVariables=new ArrayList<Var>();
	
	List<Var> totalstatementsVariables=new ArrayList<Var>();
	Var finalprovennacevariable=new Var("finalprovennacevariable");
	List<Var> variablesstart =new ArrayList<>();
	
	List<Var> optionalrightfprov =new ArrayList<>();;
	List<Var> optionalleftfprov =new ArrayList<>();;
	
	
	public List<ProjectionElem> initialBindingNames = new ArrayList<>();
	public SparqlStar(EvaluationStrategy strategy,ValueFactory vf,TupleExpr tupleExpr) {
		this.statistics=new EvaluationStatistics();
		this.strategy = strategy;
		this.vf = SimpleValueFactory.getInstance();
		this.tupleExpr = tupleExpr;
	}
	Var provenance_final_variable=new Var("fprov");
	Var join_final_variable=new Var("fjoin");
	public TupleExpr rewrite(ASTQueryContainer qc, TupleExpr tupleExpr)
	{

		TupleExpr tp=tupleExpr.clone();
		if (!(tp instanceof QueryRoot)) {
			
			tp = new QueryRoot(tp);
		}
			
		    tp.visit(this);
			//tp2.visit(this);
	  	    return tp;

	}
	@Override
	public void meet(ProjectionElemList projElems) {
		super.meet(projElems);
		projElemLists.add(projElems);
	}
	@Override
	public void meet(QueryRoot node) {
		totalstPatterns=StatementPatternCollector.process(tupleExpr);
		//System.out.println("total statements are="+totalstPatterns.size());
		//System.out.println("yahanaaaaannaaaaa"+node);
		////System.out.println("In QueryRoot function");
		node.visitChildren(this);
	}
	
	
	  private TupleExpr processSpecificStatementPattern(TupleExpr insideleftArg) {
		  //System.out.println("i am inside processSpecificStatementPattern");
			//System.out.println(statement_number);
			
		//	opts.setLeftArg((TupleExpr) leftArg);
		//	opts.setRightArg((TupleExpr) rightArg);
			RewriteStatementPattern rewritesp=new RewriteStatementPattern((insideleftArg),statement_number);
			QueryModelNode stmntpatrn = null;
			 try {
				stmntpatrn = rewritesp.rewrite(insideleftArg);
				////System.out.println(")))))))))))))))))))))))))))))))))))");
				////System.out.println(stmntpatrn);
				//optsts.setLeftArg((TupleExpr) stmntpatrn);
				statement_number=rewritesp.getStatement_number();
			//	rightArg.replaceWith(stmntpatrn);
				//stmntpatrn.visit(this);
			} catch (VisitorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return  (TupleExpr) stmntpatrn;
		  
	  }
	
	
	  private Union processSpecificStructure(TupleExpr leftArg) {
		
		  //System.out.println("i am inside processSpecificStructure");
			//System.out.println(statement_number);
		  QueryModelNode cop=leftArg.clone();
			LeftJoin diff=(LeftJoin) cop;
			TupleExpr insideleftArg = diff.getLeftArg();
			TupleExpr insiderightArg = diff.getRightArg();
			
			Union newunionts=new Union();
		      //handle inside leftJOIN here
							if((insideleftArg instanceof StatementPattern)&&(insiderightArg instanceof StatementPattern))
							{
								LeftJoin optdifssts=new LeftJoin();
								Join optsts=new Join();
								  //System.out.println("98989898088888888888888888888888888888");
								optdifssts.setLeftArg(processSpecificStatementPattern(insideleftArg));
								optsts.setLeftArg(processSpecificStatementPattern(insideleftArg));
								optdifssts.setRightArg(processSpecificStatementPattern(insiderightArg));
								optsts.setRightArg(processSpecificStatementPattern(insiderightArg));
								 //System.out.println("98989898088888888888888888888888888888");
						
							newunionts.setLeftArg(optsts);
							newunionts.setRightArg(optdifssts);
						//	//System.out.println("lalalalalalalalla"+newunionts);
							//node.replaceWith(newunion);
							//newunion.visit(this);
							
						}
							
		  
		  
		  
		return newunionts;
	  }
		  
		private Var processjoinhandletopunion(TupleExpr leftArg ,Var rightUnion)
		{
			RewriteStatementPattern rewritesp2=new RewriteStatementPattern(leftArg.clone(),join_number);	
			
			
			List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
			.filter(var -> var.getName().startsWith("fprov"))
			.distinct() 
			.collect(Collectors.toList());
			
			List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
			.filter(var -> !var.getName().startsWith("fprov"))
			.distinct() 
			.collect(Collectors.toList());
			
			//System.out.println("fprovVars: " + rightfprovVars);
			//System.out.println("remainingVars: " + rightremainingVars);
			//System.out.println("Join number: " + join_number);
			
			try {
				 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),join_number);
				QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftArg.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
				 //System.out.println("this opmeis jnjhfshffssafasfa");
					////System.out.println(grp); 
					
				 leftArg.replaceWith(grp);
					 join_number=rewritesp.getJoin_number();
					 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
					 return rewritesp.unionrightleft;
					// unionrightleftfinalvars.add(rewritesp.unionrightleft);
				
			} catch (VisitorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		private List<Var> processupperjoinhandletopunion(TupleExpr leftArg)
		{
			List<Var> variablesfi=new ArrayList<Var>();
			Var rightUnion=new Var("rightunion"+right_union_number);
			right_union_number=right_union_number+1;
			Var mainjoin=null;
			Join insideunionjoinleft=(Join) leftArg;
			TupleExpr insideunionjoinleftleftArg = insideunionjoinleft.getLeftArg();
			TupleExpr insideunionjoinleftrightArg = insideunionjoinleft.getRightArg();
			if((insideunionjoinleftleftArg instanceof Join)&&(insideunionjoinleftrightArg instanceof Join))
			{
				Join insideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftleftArg;
				TupleExpr insideunionjoinleftjoinleftleftArgleftArg = insideunionjoinleftjoinleftleftArg.getLeftArg();
				TupleExpr insideunionjoinleftjoinleftleftArgrightArg = insideunionjoinleftjoinleftleftArg.getRightArg();
				
				if((insideunionjoinleftjoinleftleftArgleftArg instanceof TripleRef)&&(insideunionjoinleftjoinleftleftArgrightArg instanceof StatementPattern))
				{
					mainjoin=processjoinhandletopunion((Join) leftArg ,rightUnion);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin);
					
				}
		
				
			}
			
			//add kiya hai
			if((insideunionjoinleftleftArg instanceof Join)&&(insideunionjoinleftrightArg instanceof Join))
			{
				Join insideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftleftArg;
				TupleExpr insideunionjoinleftjoinleftleftArgleftArg = insideunionjoinleftjoinleftleftArg.getLeftArg();
				TupleExpr insideunionjoinleftjoinleftleftArgrightArg = insideunionjoinleftjoinleftleftArg.getRightArg();
				if((insideunionjoinleftjoinleftleftArgleftArg instanceof Join)&&(insideunionjoinleftjoinleftleftArgrightArg instanceof Join))
				{
					Join ainsideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftjoinleftleftArgleftArg;
					TupleExpr ainsideunionjoinleftjoinleftleftArgleftArg = ainsideunionjoinleftjoinleftleftArg.getLeftArg();
					TupleExpr ainsideunionjoinleftjoinleftleftArgrightArg = ainsideunionjoinleftjoinleftleftArg.getRightArg();
				if((ainsideunionjoinleftjoinleftleftArgleftArg instanceof TripleRef)&&(ainsideunionjoinleftjoinleftleftArgrightArg instanceof StatementPattern))
				{
					mainjoin=processjoinhandletopunion((Join) leftArg ,rightUnion);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin);
					
				}
					}
				
			}
			
			if((insideunionjoinleftleftArg instanceof TripleRef)&&(insideunionjoinleftrightArg instanceof StatementPattern))
			{
				
					mainjoin=processjoinhandletopunion((Join) leftArg ,rightUnion);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin);
					
				
		
				
			}
			
			//yahan tak
			 
			 
			if((insideunionjoinleftleftArg instanceof Union)&&(insideunionjoinleftrightArg instanceof Join))
			{
				Var provvariable=new Var("funion"+union_number);
				union_number=union_number+1;
				Var rightjoin=new Var("rightjoin"+right_join_number);
				right_join_number=right_join_number+1;
				QueryModelNode xyz=handletopestunion(insideunionjoinleftleftArg.clone(),provvariable);
				insideunionjoinleftleftArg.replaceWith(xyz);
				mainjoin=provvariable;
				 variablesfi.add(mainjoin);
				
				Join insideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftrightArg;
				TupleExpr insideunionjoinleftjoinleftleftArgleftArg = insideunionjoinleftjoinleftleftArg.getLeftArg();
				TupleExpr insideunionjoinleftjoinleftleftArgrightArg = insideunionjoinleftjoinleftleftArg.getRightArg();
				
				if((insideunionjoinleftjoinleftleftArgleftArg instanceof TripleRef)&&(insideunionjoinleftjoinleftleftArgrightArg instanceof StatementPattern))
				{
					mainjoin=processjoinhandletopunion((Join) insideunionjoinleftrightArg ,rightjoin);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin);
					
				}
				
				
				
			}
				
			/*	QueryModelNode xyz=handletopestunion(topestunion.clone());
				//System.out.println("hahahahahhahahahaha i am the topest union"+topestunion.getParentNode());
				QueryModelNode currentfinal;
				 QueryModelNode current=topestunion;
				
					 do {
					        
				        		currentfinal=current;
				        		
				        
				        	 current=current.getParentNode();
				        	
				        	
				        }while(!((current instanceof QueryRoot)||(current==null))); 
				 
					 currentfinal.replaceWith(xyz);
					
						//System.out.println("hello topest topest topest node"+topestunion);
				 
						
				
					 //System.out.println("done");
				
				
				
			}
			return mainjoin;
			
			*/
			return variablesfi;
			
		}
		
		private List<Var> processoptional(TupleExpr rightArg,List<Var> variablesoptional)
		{
			List<Var> optionalrightleftfinalvars=new ArrayList<Var>();
			RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
			Var rightUnion=new Var("rightoptional"+right_optional_number);
			right_optional_number=right_optional_number+1;
			
			List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
			.filter(var -> var.getName().startsWith("fprov"))
			.distinct() 
			.collect(Collectors.toList());
			
			List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
			.filter(var -> !var.getName().startsWith("fprov"))
			.distinct() 
			.collect(Collectors.toList());
			
			
			
			//System.out.println("===========================================================");
			//System.out.println("fprovVars: " + rightfprovVars);
			//System.out.println("remainingVars: " + rightremainingVars);
			//System.out.println("Join number: " + join_number);
			try {
				 RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightArg.clone(),join_number);
				QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(rightArg.clone(),variablesoptional,rightremainingVars,rightUnion,join_number);
				 //System.out.println("this opmeis jnjhfshffssafasfa");
					//System.out.println(grp); 
					////System.out.println(leftoptional); 
					//oprtion.setLeftArg((TupleExpr) grp);
					rightArg.replaceWith(grp);
					 join_number=rewritesp.getJoin_number();
					 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
					 optionalrightleftfinalvars.add(rewritesp.unionrightleft);
				
			} catch (VisitorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			
			
			return optionalrightleftfinalvars;
		}
		
		private List<Var> processupperoptionalhandletopunion(TupleExpr leftArg)
		{
			List<Var> variablesfi=new ArrayList<Var>();
			Var mainjoin=null;
			Var rightUnion=new Var("rightoptional"+right_optional_number);
			right_optional_number=right_optional_number+1;
			LeftJoin insideunionjoinleft=(LeftJoin) leftArg;
			TupleExpr insideunionjoinleftleftArg = insideunionjoinleft.getLeftArg();
			TupleExpr insideunionjoinleftrightArg = insideunionjoinleft.getRightArg();
			if((insideunionjoinleftleftArg instanceof Join)&&(insideunionjoinleftrightArg instanceof Join))
			{
				
				
				List<Var> variablesoptional=new ArrayList<Var>();
				
				variablesoptional.addAll(processupperjoinhandletopunion(insideunionjoinleftleftArg));
				variablesoptional.addAll(processupperjoinhandletopunion(insideunionjoinleftrightArg));
				
			   
				variablesfi.addAll(processoptional(leftArg,variablesoptional));
			
			
			
			}
			
		/*	if(rightArg instanceof LeftJoin)
			{
				
				
				
				List<Var> optionalrightleftfinalvars=new ArrayList<Var>();
				 //System.out.println("&&&&&&&&&&&&&&&&&&HHHHHHHHHHHHHHH&&&&&&&&&&&&&&");	
				 ////System.out.println(rightArg);	
				LeftJoin oprtion=(LeftJoin) rightArg;
				TupleExpr leftoptional = oprtion.getLeftArg();
				TupleExpr rightoptional = oprtion.getRightArg();
				//System.out.println("apply logic");
				
				if(leftoptional instanceof Join)
				{
					RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
					Var rightUnion=new Var("rightoptional");
					
					List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftoptional)).stream()
					.filter(var -> var.getName().startsWith("fprov"))
					.distinct() 
					.collect(Collectors.toList());
					
					List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftoptional)).stream()
					.filter(var -> !var.getName().startsWith("fprov"))
					.distinct() 
					.collect(Collectors.toList());
					
					//System.out.println("fprovVars: " + rightfprovVars);
					//System.out.println("remainingVars: " + rightremainingVars);
					//System.out.println("Join number: " + join_number);
					
					try {
						 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftoptional.clone(),join_number);
						QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftoptional.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
						 //System.out.println("this opmeis jnjhfshffssafasfa");
							//System.out.println(grp); 
							//System.out.println(leftoptional); 
							oprtion.setLeftArg((TupleExpr) grp);
						    // leftoptional.replaceWith(grp);
							 join_number=rewritesp.getJoin_number();
							 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
							 optionalrightleftfinalvars.add(rewritesp.unionrightleft);
						
					} catch (VisitorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
			*/
			
			
		/*	if((insideunionjoinleftleftArg instanceof Join)&&(insideunionjoinleftrightArg instanceof Join))
			{
				Join insideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftleftArg;
				TupleExpr insideunionjoinleftjoinleftleftArgleftArg = insideunionjoinleftjoinleftleftArg.getLeftArg();
				TupleExpr insideunionjoinleftjoinleftleftArgrightArg = insideunionjoinleftjoinleftleftArg.getRightArg();
				
				if((insideunionjoinleftjoinleftleftArgleftArg instanceof TripleRef)&&(insideunionjoinleftjoinleftleftArgrightArg instanceof StatementPattern))
				{
					mainjoin=processjoinhandletopunion((LeftJoin) leftArg ,rightUnion);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin); 
					
				}
		
				
			}
			//add kiya hai
			if((insideunionjoinleftleftArg instanceof Join)&&(insideunionjoinleftrightArg instanceof Join))
			{
				Join insideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftleftArg;
				TupleExpr insideunionjoinleftjoinleftleftArgleftArg = insideunionjoinleftjoinleftleftArg.getLeftArg();
				TupleExpr insideunionjoinleftjoinleftleftArgrightArg = insideunionjoinleftjoinleftleftArg.getRightArg();
				
				if((insideunionjoinleftjoinleftleftArgleftArg instanceof Join)&&(insideunionjoinleftjoinleftleftArgrightArg instanceof Join))
				{
					Join ainsideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftjoinleftleftArgleftArg;
					TupleExpr ainsideunionjoinleftjoinleftleftArgleftArg = ainsideunionjoinleftjoinleftleftArg.getLeftArg();
					TupleExpr ainsideunionjoinleftjoinleftleftArgrightArg = ainsideunionjoinleftjoinleftleftArg.getRightArg();
				
				if((ainsideunionjoinleftjoinleftleftArgleftArg instanceof TripleRef)&&(ainsideunionjoinleftjoinleftleftArgrightArg instanceof StatementPattern))
				{
					mainjoin=processjoinhandletopunion((LeftJoin) leftArg ,rightUnion);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin); 
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBBIIIIIIIIIIIIII");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBBIIIIIIIIIIIIII");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBBIIIIIIIIIIIIIII");
					 //System.out.println(variablesfi);
					 
					 
					
				}
		
				}
			}
			//yahan tk    
			 * List<Var> vx=processupperjoinhandletopunion(leftArg);
			 * 
			 * */
			if((insideunionjoinleftleftArg instanceof Union)&&(insideunionjoinleftrightArg instanceof Join))
			{
				Var provvariable=new Var("funion"+union_number);
				union_number=union_number+1;
				Var rightjoin=new Var("rightjoin"+right_join_number);
				right_join_number=right_join_number+1;
				QueryModelNode xyz=handletopestunion(insideunionjoinleftleftArg.clone(),provvariable);
				insideunionjoinleftleftArg.replaceWith(xyz);
				mainjoin=provvariable;
				 variablesfi.add(mainjoin);
				
				Join insideunionjoinleftjoinleftleftArg=(Join) insideunionjoinleftrightArg;
				TupleExpr insideunionjoinleftjoinleftleftArgleftArg = insideunionjoinleftjoinleftleftArg.getLeftArg();
				TupleExpr insideunionjoinleftjoinleftleftArgrightArg = insideunionjoinleftjoinleftleftArg.getRightArg();
				
				if((insideunionjoinleftjoinleftleftArgleftArg instanceof TripleRef)&&(insideunionjoinleftjoinleftleftArgrightArg instanceof StatementPattern))
				{
					mainjoin=processjoinhandletopunion((Join) insideunionjoinleftrightArg ,rightjoin);
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB2");
					 //System.out.println(mainjoin);
					 variablesfi.add(mainjoin);
					
				}
				
				
				
			}
			
			return variablesfi;
			
		}
		
	private QueryModelNode topppesetunionwithprov(QueryModelNode topestunion, List<Var> unionrightleftfinalvars,Var finalprovennacevariable2)
	{
		QueryModelNode grp=null;
		//System.out.println(topestunion);
		//System.out.println("danadanaanadanaaa");	
		//System.out.println(unionrightleftfinalvars);
		//Union un=(Union) topestunion.clone();

		try {
			 RewriteStatementPattern rewritesp=new RewriteStatementPattern(topestunion.clone(),union_number);
			grp=rewritesp.handlemultiplejoinBindAndGroup(topestunion.clone(),unionrightleftfinalvars,variablesstart,finalprovennacevariable2,union_number);

			//System.out.println("danadanaanadanaaaFFFFFFFFFFFFF");	
			
			//System.out.println(grp);	
			//System.out.println(topestunion);
			
			
				 union_number=rewritesp.getUnion_number();
				 totalunions.add(rewritesp.getTriple_patterns_union_varible());
			//return currentfinal;
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return grp;
			
	}
		
	  private QueryModelNode handletopestunion(QueryModelNode topestunion, Var provvariable) {
		  QueryModelNode xyz=null;
		  List<Var> unionrightleftfinalvars=new ArrayList<Var>();
		  if (topestunion instanceof Union) {
			 
			  //System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB");
				////System.out.println(topestunion);	
				unionrightleftfinalvars=new ArrayList<Var>();	
				Union union=(Union) topestunion;
				TupleExpr leftArg = union.getLeftArg();
				TupleExpr rightArg = union.getRightArg();
				//System.out.println("apply logic");
				
				if((leftArg instanceof Join)&&(rightArg instanceof LeftJoin))
				{
					
					List<Var> vx=processupperjoinhandletopunion(leftArg);
					////System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					////System.out.println(vx);
					unionrightleftfinalvars.addAll(vx);
					List<Var> vx2=processupperoptionalhandletopunion(rightArg);
					////System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					//System.out.println(vx2);
					unionrightleftfinalvars.addAll(vx2);
					
					xyz=topppesetunionwithprov(topestunion,unionrightleftfinalvars,provvariable);
					
					
					
					//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB3");
					
					////System.out.println(xyz);
					
					
				}
				
				
				
				if(rightArg instanceof Join)
				{
					
				}
			  
			  
			  
			  
			  
				/* QueryModelNode currentfinal;
				 QueryModelNode current=topestunion;
				
					 do {
					        
				        		currentfinal=current;
				        		
				        
				        	 current=current.getParentNode();
				        	
				        	
				        }while(!((current instanceof QueryRoot)||(current==null))); 
				 
					 currentfinal.replaceWith(grp);
					
						//System.out.println("hello topest topest topest node"+currentfinal);
				 
						
				
					 //System.out.println("done");
					 */
					 ////System.out.println(node);
			  
			  
			  
		
			  
			  
			  
			  
		  }
		  
		  
		  
		  
			return xyz;
		} 
	
		/*if(!(topestunion==null))
		{
			//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB");
			//System.out.println(topestunion);	
			unionrightleftfinalvars=new ArrayList<Var>();	
			Union union=(Union) topestunion;
			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();
			//System.out.println("apply logic");
			
			
	
			
							if(rightArg instanceof Join)
							{
								RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
							Var rightUnion=new Var("rightunion");
							
							List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
							.filter(var -> var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
							.filter(var -> !var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							//System.out.println("fprovVars: " + rightfprovVars);
							//System.out.println("remainingVars: " + rightremainingVars);
							//System.out.println("Join number: " + join_number);
							
							try {
								 RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightArg.clone(),join_number);
								QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(rightArg.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
								 //System.out.println("this opmeis jnjhfshffssafasfa");
									////System.out.println(grp); 
									
									rightArg.replaceWith(grp);
									 join_number=rewritesp.getJoin_number();
									 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
									 unionrightleftfinalvars.add(rewritesp.unionrightleft);
								
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
							}
							if(leftArg instanceof Join)
							{
									
								
								Join insidejoin=(Join) leftArg;
								TupleExpr insidejoinleftArg = insidejoin.getLeftArg();
								TupleExpr insidejoinrightArg = insidejoin.getRightArg();
								//System.out.println("apply logic");
								
								if((insidejoinleftArg instanceof Join)&&(insidejoinrightArg instanceof Join))
										{
								 RewriteStatementPattern rewritesp2=new RewriteStatementPattern(leftArg.clone(),join_number);
								Var leftUnion=new Var("leftunion");	
							List<Var> leftfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
							.filter(var -> var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							List<Var> leftremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
							.filter(var -> !var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							//System.out.println("fprovVars: " + leftfprovVars);
							//System.out.println("remainingVars: " + leftremainingVars);
							//System.out.println("Join number: " + join_number);
							try {
								 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),join_number);
								QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftArg.clone(),leftfprovVars,leftremainingVars,leftUnion,join_number);
								 //System.out.println("this opmeis jnjhfshffsdfhdf");
									////System.out.println(grp); 
									
									leftArg.replaceWith(grp);
									 join_number=rewritesp.getJoin_number();
									 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
									 unionrightleftfinalvars.add(rewritesp.unionrightleft);
							
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							}
							}
							if(rightArg instanceof LeftJoin)
							{
								
								
								
								List<Var> optionalrightleftfinalvars=new ArrayList<Var>();
								 //System.out.println("&&&&&&&&&&&&&&&&&&HHHHHHHHHHHHHHH&&&&&&&&&&&&&&");	
								 ////System.out.println(rightArg);	
								LeftJoin oprtion=(LeftJoin) rightArg;
								TupleExpr leftoptional = oprtion.getLeftArg();
								TupleExpr rightoptional = oprtion.getRightArg();
								//System.out.println("apply logic");
								
								if(leftoptional instanceof Join)
								{
									RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
									Var rightUnion=new Var("rightoptional");
									
									List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftoptional)).stream()
									.filter(var -> var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									
									List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftoptional)).stream()
									.filter(var -> !var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									
									//System.out.println("fprovVars: " + rightfprovVars);
									//System.out.println("remainingVars: " + rightremainingVars);
									//System.out.println("Join number: " + join_number);
									
									try {
										 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftoptional.clone(),join_number);
										QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftoptional.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
										 //System.out.println("this opmeis jnjhfshffssafasfa");
											//System.out.println(grp); 
											//System.out.println(leftoptional); 
											oprtion.setLeftArg((TupleExpr) grp);
										    // leftoptional.replaceWith(grp);
											 join_number=rewritesp.getJoin_number();
											 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
											 optionalrightleftfinalvars.add(rewritesp.unionrightleft);
										
									} catch (VisitorException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									
								}
								if(rightoptional instanceof Join)
								{
									 RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightoptional.clone(),join_number);
										Var leftUnion=new Var("leftoptional");	
									List<Var> leftfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightoptional)).stream()
									.filter(var -> var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									
									List<Var> leftremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightoptional)).stream()
									.filter(var -> !var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									//System.out.println("fprovVars: " + leftfprovVars);
									//System.out.println("remainingVars: " + leftremainingVars);
									//System.out.println("Join number: " + join_number);
									try {
										 RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightoptional.clone(),join_number);
										QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(rightoptional.clone(),leftfprovVars,leftremainingVars,leftUnion,join_number);
										 //System.out.println("this opmeis jnjhfshffsdfhdf");
											////System.out.println(grp); 
										 oprtion.setRightArg((TupleExpr) grp);
										// rightoptional.replaceWith(grp);
											 join_number=rewritesp.getJoin_number();
											 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
											 optionalrightleftfinalvars.add(rewritesp.unionrightleft);
									
									} catch (VisitorException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}	
								}
								//System.out.println("AMMMMMMMMMMMMMMMMMMMMMMMMMMIIIIIIIIIIIIIIIIIIIIII");
								//System.out.println(oprtion);
								//System.out.println(optionalrightleftfinalvars);
								
								Var unionlft=new Var();
								unionlft.setName("unionright");
								 RewriteStatementPattern rewritespx=new RewriteStatementPattern(oprtion.clone(),optional_number);
								QueryModelNode grp;
								try {
									grp = rewritespx.handlemultiplejoinBindAndGroup(oprtion.clone(),optionalrightleftfinalvars,variablesstart,unionlft,optional_number);
								
									//System.out.println("AMMMMMMMMMMMMMMMMMMMMMMMMMMIIIIIIIIIIIIIIIIIIIIII2");
									//System.out.println(grp);
									 unionrightleftfinalvars.add(unionlft);
									 if(grp!=null )
									 {
										 rightArg.replaceWith(grp);
									 }
								} catch (VisitorException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
									
					
							
							}


//System.out.println("danadanaanadanaaa");	
//System.out.println(unionrightleftfinalvars);
//Union un=(Union) topestunion.clone();

try {
	 RewriteStatementPattern rewritesp=new RewriteStatementPattern(topestunion.clone(),union_number);
	QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(topestunion.clone(),unionrightleftfinalvars,variablesstart,finalprovennacevariable,union_number);

	//System.out.println("danadanaanadanaaaFFFFFFFFFFFFF");	
	
	//System.out.println(grp);	
	//System.out.println(topestunion);
	
	 QueryModelNode currentfinal;
	 QueryModelNode current=topestunion;
	
		 do {
		        
	        		currentfinal=current;
	        		
	        
	        	 current=current.getParentNode();
	        	
	        	
	        }while(!((current instanceof QueryRoot)||(current==null))); 
	 
		 currentfinal.replaceWith(grp);
		
			//System.out.println("hello topest topest topest node"+topestunion);
	 
			
	
		 //System.out.println("done");
		 
		 union_number=rewritesp.getUnion_number();
		 totalunions.add(rewritesp.getTriple_patterns_union_varible());
	
} catch (VisitorException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	
	
			
			
		} */
	
	  public Union leftjoinrecursivemethod(QueryModelNode node) {
		  
		  Union unionhandle=null;
		  
		  if (node instanceof LeftJoin) {
			  LeftJoin leftJoin = (LeftJoin) node.clone();
	            TupleExpr leftArg = leftJoin.getLeftArg();
	            TupleExpr rightArg = leftJoin.getRightArg();
	           
	            if (leftArg instanceof StatementPattern && rightArg instanceof StatementPattern) {
			  
			          unionhandle=processNode(node.clone()); 
	            }
	            
	            if (leftArg instanceof LeftJoin && rightArg instanceof StatementPattern) {
	            	LeftJoin insideleftJoin = (LeftJoin) leftArg.clone();
	 	            TupleExpr insideleftJoinleftArg = insideleftJoin.getLeftArg();
	 	            TupleExpr insideleftJoinrightArg = insideleftJoin.getRightArg();
	 	           if (insideleftJoinleftArg instanceof StatementPattern && insideleftJoinrightArg instanceof StatementPattern) {
	 				  
				          unionhandle=processNode(node.clone()); 
		            }
	            	
	            }
	            if (leftArg instanceof LeftJoin && rightArg instanceof StatementPattern) {
	            	LeftJoin insideleftJoin = (LeftJoin) leftArg.clone();
	 	            TupleExpr insideleftJoinleftArg = insideleftJoin.getLeftArg();
	 	            TupleExpr insideleftJoinrightArg = insideleftJoin.getRightArg(); 
	            
	            if (insideleftJoinleftArg instanceof LeftJoin && insideleftJoinrightArg instanceof StatementPattern) {
	            	LeftJoin insideinsideleftJoinleftArg = (LeftJoin) insideleftJoinleftArg.clone();
	 	            TupleExpr ininsideinsideleftJoinleftArg = insideinsideleftJoinleftArg.getLeftArg();
	 	            TupleExpr ininsideinsideleftJoinrightArg = insideinsideleftJoinleftArg.getRightArg();
	 	           if (ininsideinsideleftJoinleftArg instanceof StatementPattern && ininsideinsideleftJoinrightArg instanceof StatementPattern) {
	 				  
				          unionhandle=processNode(node.clone()); 
		            }
	            	
	            }
	            }
	            if (leftArg instanceof LeftJoin && rightArg instanceof StatementPattern) {
	            	LeftJoin insideleftJoin = (LeftJoin) leftArg.clone();
	 	            TupleExpr insideleftJoinleftArg = insideleftJoin.getLeftArg();
	 	            TupleExpr insideleftJoinrightArg = insideleftJoin.getRightArg(); 
	 	           if (insideleftJoinleftArg instanceof LeftJoin && insideleftJoinrightArg instanceof StatementPattern) {
		            	LeftJoin insideinsideleftJoinleftArg = (LeftJoin) insideleftJoinleftArg.clone();
		 	            TupleExpr ininsideinsideleftJoinleftArg = insideinsideleftJoinleftArg.getLeftArg();
		 	            TupleExpr ininsideinsideleftJoinrightArg = insideinsideleftJoinleftArg.getRightArg();
	            
	            if (ininsideinsideleftJoinleftArg instanceof LeftJoin && ininsideinsideleftJoinrightArg instanceof StatementPattern) {
	            	LeftJoin ainsideinsideleftJoinleftArg = (LeftJoin) ininsideinsideleftJoinleftArg.clone();
	 	            TupleExpr aininsideinsideleftJoinleftArg = ainsideinsideleftJoinleftArg.getLeftArg();
	 	            TupleExpr aininsideinsideleftJoinrightArg = ainsideinsideleftJoinleftArg.getRightArg();
	 	           if (aininsideinsideleftJoinleftArg instanceof StatementPattern && aininsideinsideleftJoinrightArg instanceof StatementPattern) {
	 				  
				          unionhandle=processNode(node.clone()); 
		            }
	            	
	            }}
	            }
	            if (leftArg instanceof Join && rightArg instanceof StatementPattern) {
	            	 unionhandle=processNode(node.clone()); 
	            }
	            
	           
	           
	            
	            
	            
	            
		  }
		  
		  else {
              // Continue visiting the tree without changes
              node.visit(this);
          }
		  
		  
		  
		  
		  return unionhandle;
		  
		  
		/*	Union unionhandle=new Union();
			//System.out.println("i am inside main LeftJoin");
			//System.out.println(statement_number);
			if (node instanceof LeftJoin) {
				
	            // Handle LeftJoin logic
	            LeftJoin leftJoin = (LeftJoin) node;
	            TupleExpr leftArg = leftJoin.getLeftArg();
	            TupleExpr rightArg = leftJoin.getRightArg();
	            
	            // Check if the LeftJoin has the specific structure you want to process
	            if (leftArg instanceof LeftJoin && rightArg instanceof StatementPattern) {
	             
	            	 LeftJoin insideleftJoin = (LeftJoin) leftArg;
	 	            TupleExpr insideleftJoinleftArg = insideleftJoin.getLeftArg();
	 	            TupleExpr insideleftJoinrightArg = insideleftJoin.getRightArg();
	 	            
	 	           if (insideleftJoinleftArg instanceof StatementPattern && insideleftJoinrightArg instanceof StatementPattern) {
	 	        	   
	 	        	  unionhandle=processNode(node);
	 	        	   
	 	        	   
	 	           }
	 	            
	                     
	            }   
	            if (leftArg instanceof StatementPattern && rightArg instanceof StatementPattern) {
		             
	            	
	 	        	   
	 	        	  unionhandle=processNode(node);
	 	        	   
	 	        	   
	 	           
	 	            
	                     
	            }   
	            
	            } else {
	                // Continue visiting the tree without changes
	                node.visit(this);
	            }
			return null;
	     
			*/
			}
	public Union processNode(QueryModelNode node) {
		
		//System.out.println("i am inside main LeftJoin");
		//System.out.println(statement_number);
		if (node instanceof LeftJoin) {
			Union newunion=new Union();
            // Handle LeftJoin logic
            LeftJoin leftJoin = (LeftJoin) node;
            TupleExpr leftArg = leftJoin.getLeftArg();
            TupleExpr rightArg = leftJoin.getRightArg();
            
            // Check if the LeftJoin has the specific structure you want to process
            if (leftArg instanceof LeftJoin && rightArg instanceof StatementPattern) {
                // Create and process a new structure as needed
            	LeftJoin optdifssts=new LeftJoin();
				Join optsts=new Join();
				
				optdifssts.setLeftArg(processSpecificStructure(leftArg));
				optsts.setLeftArg(processSpecificStructure(leftArg));
				optdifssts.setRightArg(processSpecificStatementPattern(rightArg));
				optsts.setRightArg(processSpecificStatementPattern(rightArg));
               
    
				 
				 
					newunion.setLeftArg(optsts);
					newunion.setRightArg(optdifssts);
					////System.out.println("lalalalalalalalla"+newunion);
					//node.replaceWith(newunion);
					//newunion.visit(this); 
                     
            }   
            if (leftArg instanceof LeftJoin && rightArg instanceof LeftJoin) {
            	
            	
            	
                // Create and process a new structure as needed
            	LeftJoin optdifssts=new LeftJoin();
				Join optsts=new Join();
				
				optdifssts.setLeftArg(processSpecificStructure(leftArg));
				optsts.setLeftArg(processSpecificStructure(leftArg));
				optdifssts.setRightArg(processSpecificStructure(rightArg));
				optsts.setRightArg(processSpecificStructure(rightArg));
               
    
				 
				 
					newunion.setLeftArg(optsts);
					newunion.setRightArg(optdifssts);
				//	//System.out.println("lalalalalalalalla"+newunion);
					//node.replaceWith(newunion);
					//newunion.visit(this); 
                     
            } 
            
            if (leftArg instanceof StatementPattern && rightArg instanceof StatementPattern) {
                // Create and process a new structure as needed
            	LeftJoin optdifssts=new LeftJoin();
				Join optsts=new Join();
				
				optdifssts.setLeftArg(processSpecificStatementPattern(leftArg));
				optsts.setLeftArg(processSpecificStatementPattern(leftArg));
				optdifssts.setRightArg(processSpecificStatementPattern(rightArg));
				optsts.setRightArg(processSpecificStatementPattern(rightArg));
               
    
				 
				 
					newunion.setLeftArg(optsts);
					newunion.setRightArg(optdifssts);
					////System.out.println("lalalalalalalalla"+newunion);
					//node.replaceWith(newunion);
					//newunion.visit(this); 
                     
            } 
            if (leftArg instanceof Join && rightArg instanceof StatementPattern) {
                // Create and process a new structure as needed
            	LeftJoin optdifssts=new LeftJoin();
				Join optsts=new Join();
				Join subjoin=(Join) leftArg.clone();
				//System.out.println("******************************"+node.clone());
				
				subjoin.setLeftArg(processSpecificStatementPattern(((Join) subjoin).getLeftArg()));
				
				subjoin.setRightArg(processSpecificStatementPattern(((Join) subjoin).getRightArg()));
				//System.out.println("******************************"+subjoin);
				
				
				
				optdifssts.setLeftArg(subjoin);
				optsts.setLeftArg(subjoin);
		
				optdifssts.setRightArg(processSpecificStatementPattern(rightArg));
				optsts.setRightArg(processSpecificStatementPattern(rightArg));
               
				//System.out.println("******************************"+optdifssts);
				//System.out.println("******************************"+optsts); 
				 
					newunion.setLeftArg(optsts);
					newunion.setRightArg(optdifssts);
					////System.out.println("lalalalalalalalla"+newunion);
					//node.replaceWith(newunion);
					//newunion.visit(this); 
                     
            }  
            
            
            
            return newunion;  
            } else {
                // Continue visiting the tree without changes
                node.visit(this);
            }
		return null;
     
		
		}

	
	
	
	
	@Override
	protected void meetNode(QueryModelNode node) throws RuntimeException {
		QueryModelNode singlepattern = null;
		QueryModelNode topjoin = null;
		QueryModelNode topestjoin = null;

		
		 QueryModelNode bindExtension = null;
		 QueryModelNode topunion = null;
			QueryModelNode topestunion = null;
			QueryModelNode subtopestjoin = null;
			QueryModelNode subtopestunion = null;
			QueryModelNode topoptional=null;
			QueryModelNode topestoptional=null;
		
		
		if((node instanceof Projection)&&(node.getParentNode() instanceof QueryRoot))
					{
				Set<String> bindings = ((Projection) node).getBindingNames();
				//initialBindingNames=((ProjectionElemList) node).getElements();
				//initialBindingNames.addAll(bindings);
				for(String s:bindings)
				{
					initialBindingNames.add(new ProjectionElem(s));
				}
			//	//System.out.println(initialBindingNames.size());
				for(ProjectionElem pel:initialBindingNames)
				{
					variablesstart.add(new Var(pel.getSourceName()));
				}
				// //System.out.println("initial bindings are="+variablesstart);
				
					}
	
	
/*

		if(node instanceof StatementPattern)
		{
			if(totalstPatterns.size()==1)
			{
				singlepattern=node;
			}
			/*String ns=node.toString();
			if (ns.contains("http://example.org/occurrenceOf"))
			{
				
			}
			else
			{
			RewriteStatementPattern rewritesp=new RewriteStatementPattern(node.clone(),statement_number);
			QueryModelNode stmntpatrn;
			if(totalstPatterns.size()==1)
			{
			
			try {
				stmntpatrn = rewritesp.rewrite(node.clone());
				
				//System.out.println(rewritesp.triple_pattern_varibles);
				//System.out.println(stmntpatrn);
				//stmntpatrn.visit(this);
				node.replaceWith(stmntpatrn);
				stmntpatrn.visit(this);
				//node.visit(this);
				statement_number=rewritesp.getStatement_number();
			} catch (VisitorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/
			//}
		/*	else
			{
				try {
					stmntpatrn = rewritesp.rewritemore(node);
				
					//System.out.println(rewritesp.triple_pattern_varibles);
					//System.out.println(stmntpatrn);
					//stmntpatrn.visit(this);
					node.replaceWith(stmntpatrn);
					//node.visit(this);
					statement_number=rewritesp.getStatement_number();
				} catch (VisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
			//}
	//	}
		if(node instanceof StatementPattern)
		{
			if(totalstPatterns.size()==1)		
			{
				String ns=node.toString();
				if (ns.contains("fprov"))
				{
					
				}
				else
				{
				RewriteStatementPattern rewritesp=new RewriteStatementPattern(node.clone(),statement_number);
				QueryModelNode stmntpatrn;
				if(totalstPatterns.size()==1)
				{
				
				try {
					stmntpatrn = rewritesp.rewrite(node.clone());
					
					//System.out.println(rewritesp.triple_pattern_varibles);
					//System.out.println(stmntpatrn);
					//stmntpatrn.visit(this);
					node.replaceWith(stmntpatrn);
					stmntpatrn.visit(this);
					//node.visit(this);
					statement_number=rewritesp.getStatement_number();
					totaljoinstatementsprovenanceVariables.add(rewritesp.getTriple_pattern_provenenace_varible());
					
					//System.out.println("abcdefghijklmnopqrstuvwxyz");
					//System.out.println(totaljoinstatementsprovenanceVariables);
					
					
					
				} catch (VisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
				//singlepattern=node;
			}
				}
			}
			
			
			/*
					RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),statement_number);
					QueryModelNode stmntpatrn;
					
					
					try {
						
						
						stmntpatrn = rewritesp.rewrite(leftArg.clone());
						
						//System.out.println(rewritesp.triple_pattern_varibles);
						//System.out.println("node"+leftArg);
						//System.out.println("rewritten node"+stmntpatrn);
						
						
						//stmntpatrn.visit(this);
					//	leftArg.replaceWith(stmntpatrn);
						join.setLeftArg((TupleExpr) stmntpatrn);
						//stmntpatrn.visit(this);
						//node.visit(this);
						statement_number=rewritesp.getStatement_number();
						//System.out.println("p"+rewritesp.getTriple_pattern_provenenace_varible());
						totaljoinstatementsprovenanceVariables.add(rewritesp.getTriple_pattern_provenenace_varible());
					} catch (VisitorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} */
			
			
			
			/*String ns=node.toString();
			if (ns.contains("http://example.org/occurrenceOf"))
			{
				
			}
			else
			{
			RewriteStatementPattern rewritesp=new RewriteStatementPattern(node.clone(),statement_number);
			QueryModelNode stmntpatrn;
			if(totalstPatterns.size()==1)
			{
			
			try {
				stmntpatrn = rewritesp.rewrite(node.clone());
				
				//System.out.println(rewritesp.triple_pattern_varibles);
				//System.out.println(stmntpatrn);
				//stmntpatrn.visit(this);
				node.replaceWith(stmntpatrn);
				stmntpatrn.visit(this);
				//node.visit(this);
				statement_number=rewritesp.getStatement_number();
			} catch (VisitorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/
			//}
		/*	else
			{
				try {
					stmntpatrn = rewritesp.rewritemore(node);
				
					//System.out.println(rewritesp.triple_pattern_varibles);
					//System.out.println(stmntpatrn);
					//stmntpatrn.visit(this);
					node.replaceWith(stmntpatrn);
					//node.visit(this);
					statement_number=rewritesp.getStatement_number();
				} catch (VisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
			//}
		}		
		if(node instanceof Difference)
		{
			Difference diff=(Difference) node;
		    TupleExpr leftArg = diff.getLeftArg();
	        TupleExpr rightArg = diff.getRightArg();

	        // Check if the left and right arguments are suitable for a LeftJoin
	        // You can add your own conditions here

	        // Create a new LeftJoin node
	        LeftJoin leftJoin = new LeftJoin(leftArg, rightArg);

	        // Replace the current node with the new LeftJoin
	        node.replaceWith(leftJoin);
	        
			
			//System.out.println(node);
		//	leftJoin.visit(this);
	        // Continue visiting the tree
	       // super.meet(leftJoin);
		}
		if(node instanceof LeftJoin)
		{
		
			
			
		
			if(node.getParentNode() instanceof Union)
			{
				
			}
			else
			{
		//  Union rewrittenleftJoin=processNode(node.clone());
				Union rewrittenleftJoin=leftjoinrecursivemethod(node.clone());
				node.replaceWith(rewrittenleftJoin);
			rewrittenleftJoin.visit(this);
			//System.out.println("+++++++++++++_____________+++++++++++++++");
			//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			////System.out.println(rewrittenleftJoin);
			
			////System.out.println(node);
			
			//System.out.println("+++++++++++++_____________+++++++++++++++");
			//System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
			
	
		}
		
		
		if(node instanceof Union)
		{
			
			Union union=(Union) node;
			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();
			List<StatementPattern> unionrightpatterns=new ArrayList<StatementPattern>() ; 
			List<StatementPattern> unionleftpatterns=new ArrayList<StatementPattern>() ; 
			unionrightpatterns=StatementPatternCollector.process(rightArg);
			unionleftpatterns=StatementPatternCollector.process(leftArg);
		
			
			
			
			
			if((leftArg instanceof Join)&&(rightArg instanceof LeftJoin))
			{
				//System.out.println("lets handdle union baby");
				
				//System.out.println("+++++++++++++_____________+++++++++++++++");
				//System.out.println(unionrightpatterns);
				//System.out.println(unionleftpatterns);
				
				
				
				
				
			}
			
			
			
			
			
			//RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),union_number);
			
			
		
			if(unionrightpatterns.size()==1)
			{

				String ns=rightArg.toString();
				if (ns.contains("http://example.org/occurrenceOf"))
				{
					
				}
				else
				{
				RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightArg.clone(),statement_number);
				QueryModelNode stmntpatrn;
				
				//singlepattern=rightArg;
				try {
					stmntpatrn = rewritesp.rewrite(rightArg.clone());
					
					//System.out.println(rewritesp.triple_pattern_varibles);
					//System.out.println(stmntpatrn);
					//stmntpatrn.visit(this);
					rightArg.replaceWith(stmntpatrn);
				//	stmntpatrn.visit(this);
					//node.visit(this);
					statement_number=rewritesp.getStatement_number();
				} catch (VisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}
			else if(unionleftpatterns.size()==1)
			{
				String ns=leftArg.toString();
				if (ns.contains("http://example.org/occurrenceOf"))
				{
					
				}
				else
				{
				RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),statement_number);
				QueryModelNode stmntpatrn;
				//singlepattern=leftArg;
				try {
					stmntpatrn = rewritesp.rewrite(leftArg.clone());
					
					//System.out.println(rewritesp.triple_pattern_varibles);
					//System.out.println(stmntpatrn);
					//stmntpatrn.visit(this);
					leftArg.replaceWith(stmntpatrn);
					//stmntpatrn.visit(this);
					//node.visit(this);
					statement_number=rewritesp.getStatement_number();
				} catch (VisitorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			}
			
		
		}
		if(node instanceof Join)
		{
			Join join=(Join) node;
			TupleExpr leftArg = join.getLeftArg();
			TupleExpr rightArg = join.getRightArg();
			if(totalstPatterns.size()==1)
			{
				
				
				if((leftArg instanceof TripleRef)&&(rightArg instanceof StatementPattern))
				{
					// //System.out.println("Lo single"+leftArg);
					// //System.out.println("Lo single"+rightArg);
					QueryModelNode current=node.clone();
					do {
						if((current instanceof Join)&&(current!=null)&&(current.getParentNode() instanceof Projection)&&((current.getParentNode()).getParentNode() instanceof QueryRoot))
			        	{
							
							singlepattern=current;
			        	}
						// //System.out.println("Lo single"+current);
						 current=current.getParentNode();
						// //System.out.println("Lo single"+current);
					  }while(!((current instanceof QueryRoot)||(current==null))); 
				//	//System.out.println("yes single"+singlepattern);	
					
				}
				
			}
			else if((leftArg instanceof TripleRef)&&(rightArg instanceof StatementPattern)&&(((StatementPattern) rightArg).getObjectVar().getName().equals("fprov"+(totalstPatterns.size()-1))))
				{
					
				
				
				QueryModelNode current=node.clone();
				// //System.out.println("UNION UNION UNION UNION UNION UNION"+current);
			
				 do {
					// //System.out.println("curr"+current);
					   if((current instanceof Union))
			        	{
					//	   //System.out.println("UNION UNION UNION UNION UNION UNION"+current);
			        		topunion=current;
			        		
			        	}
			        	if((current instanceof Join)&&(current!=null))
			        	{
			        	//	//System.out.println("helljoin"+current);
			        		topjoin=current;
			        		
			        	}
			          	if((current instanceof LeftJoin)&&(current!=null))
			        	{
			        		////System.out.println("helljoin"+current.getParentNode());
			        		topoptional=current;
			        		
			        	}
			         
			        	 current=current.getParentNode();
			        	// //System.out.println("tutuptuptip"+current);
			        	
			        }while(!((current instanceof QueryRoot)||(current==null))); 
				
				
				
				
	
					
				
				//	//System.out.println(totalstPatterns.size());
				//	//System.out.println(totaljoinstatementsprovenanceVariables.size());
				
				
				//	 //System.out.println("current join is"+topjoin);
				     
					
						if((topjoin instanceof Join)&&(topjoin.getParentNode() instanceof Projection)&&((topjoin.getParentNode()).getParentNode() instanceof QueryRoot))
						 {
					 topestjoin=topjoin;
			
				
				     }
						if((topunion instanceof Union)&&(topunion.getParentNode() instanceof Projection)&&((topunion.getParentNode()).getParentNode() instanceof QueryRoot))
						 {
					 topestunion=topunion;
					// //System.out.println("Hello mR Jophn"+topestunion);
					 
				
				     }
						if((topoptional instanceof LeftJoin)&&(topoptional.getParentNode() instanceof Projection)&&((topoptional.getParentNode()).getParentNode() instanceof QueryRoot))
						 {
							
							topestoptional=topoptional;
					
					// //System.out.println("current topest is"+topestjoin);
						 }	
						
						
				}
				
				if((leftArg instanceof StatementPattern)&&(rightArg instanceof StatementPattern))
				{
					
					
					RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),statement_number);
					QueryModelNode stmntpatrn;
					
					
					try {
						stmntpatrn = rewritesp.rewrite(leftArg.clone());
						
						//System.out.println(rewritesp.triple_pattern_varibles);
					//	//System.out.println("node"+leftArg);
						////System.out.println("rewritten node"+stmntpatrn);
						
						
						//stmntpatrn.visit(this);
					//	leftArg.replaceWith(stmntpatrn);
						join.setLeftArg((TupleExpr) stmntpatrn);
						//stmntpatrn.visit(this);
						//node.visit(this);
						statement_number=rewritesp.getStatement_number();
						//System.out.println("p"+rewritesp.getTriple_pattern_provenenace_varible());
						totaljoinstatementsprovenanceVariables.add(rewritesp.getTriple_pattern_provenenace_varible());
					} catch (VisitorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
					RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),statement_number);
					QueryModelNode stmntpatrn2 = null;
					
					
					try {
						stmntpatrn2 = rewritesp2.rewrite(rightArg.clone());
						
						//System.out.println(rewritesp2.triple_pattern_varibles);
						//System.out.println(stmntpatrn2);
						//stmntpatrn.visit(this);
						//rightArg.replaceWith(stmntpatrn2);
						join.setRightArg((TupleExpr) stmntpatrn2);
						//stmntpatrn2.visit(this);
						//node.visit(this);
						//System.out.println("node"+rightArg);
						//System.out.println("rewritten node"+stmntpatrn2);
						statement_number=rewritesp2.getStatement_number();
						//System.out.println("p2"+rewritesp2.getTriple_pattern_provenenace_varible());
						totaljoinstatementsprovenanceVariables.add(rewritesp2.getTriple_pattern_provenenace_varible());
					} catch (VisitorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				//System.out.println("AB ye hai JOIN"+join);
				//System.out.println("AB ye hai JOIN"+leftArg);
				//System.out.println("AB ye hai JOIN"+rightArg);
					
				}
				if((leftArg instanceof Join)&&(rightArg instanceof StatementPattern))
				{
					
					RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),statement_number);
					QueryModelNode stmntpatrn2 = null;
					
					
					try {
						stmntpatrn2 = rewritesp2.rewrite(rightArg.clone());
						
						//System.out.println(rewritesp2.triple_pattern_varibles);
						//System.out.println(stmntpatrn2);
						//stmntpatrn.visit(this);
						rightArg.replaceWith(stmntpatrn2);
						//stmntpatrn2.visit(this);
						//node.visit(this);
						//System.out.println("node"+rightArg);
						//System.out.println("rewritten node"+stmntpatrn2);
						statement_number=rewritesp2.getStatement_number();
						//System.out.println("p2"+rewritesp2.getTriple_pattern_provenenace_varible());
						totaljoinstatementsprovenanceVariables.add(rewritesp2.getTriple_pattern_provenenace_varible());
					} catch (VisitorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
			
					
					
				}
				
				
			
			

	
		
			
			
		}
		
		if(!(topestunion==null))
		{
			//System.out.println("hahahahahhahahahaha i am the topest union"+topestunion);
			
			QueryModelNode xyz=handletopestunion(topestunion.clone(),finalprovennacevariable);
			//System.out.println("hahahahahhahahahaha i am the topest union"+topestunion.getParentNode());
			QueryModelNode currentfinal;
			 QueryModelNode current=topestunion;
			
				 do {
				        
			        		currentfinal=current;
			        		
			        
			        	 current=current.getParentNode();
			        	
			        	
			        }while(!((current instanceof QueryRoot)||(current==null))); 
			 
				 currentfinal.replaceWith(xyz);
				
					//System.out.println("hello topest topest topest node"+topestunion);
			 
					
			
				 //System.out.println("done");
			
			
			
		}
		
		
		//iss ko oper change kr rahi
		/*if(!(topestunion==null))
		{
			//System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEBBBBBBBBBBBBBBBBB");
			//System.out.println(topestunion);	
			unionrightleftfinalvars=new ArrayList<Var>();	
			Union union=(Union) topestunion;
			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();
			//System.out.println("apply logic");
			
			
	
			
							if(rightArg instanceof Join)
							{
								RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
							Var rightUnion=new Var("rightunion");
							
							List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
							.filter(var -> var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
							.filter(var -> !var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							//System.out.println("fprovVars: " + rightfprovVars);
							//System.out.println("remainingVars: " + rightremainingVars);
							//System.out.println("Join number: " + join_number);
							
							try {
								 RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightArg.clone(),join_number);
								QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(rightArg.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
								 //System.out.println("this opmeis jnjhfshffssafasfa");
									////System.out.println(grp); 
									
									rightArg.replaceWith(grp);
									 join_number=rewritesp.getJoin_number();
									 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
									 unionrightleftfinalvars.add(rewritesp.unionrightleft);
								
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
							}
							if(leftArg instanceof Join)
							{
									
								
								Join insidejoin=(Join) leftArg;
								TupleExpr insidejoinleftArg = insidejoin.getLeftArg();
								TupleExpr insidejoinrightArg = insidejoin.getRightArg();
								//System.out.println("apply logic");
								
								if((insidejoinleftArg instanceof Join)&&(insidejoinrightArg instanceof Join))
										{
								 RewriteStatementPattern rewritesp2=new RewriteStatementPattern(leftArg.clone(),join_number);
								Var leftUnion=new Var("leftunion");	
							List<Var> leftfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
							.filter(var -> var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							List<Var> leftremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
							.filter(var -> !var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							//System.out.println("fprovVars: " + leftfprovVars);
							//System.out.println("remainingVars: " + leftremainingVars);
							//System.out.println("Join number: " + join_number);
							try {
								 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),join_number);
								QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftArg.clone(),leftfprovVars,leftremainingVars,leftUnion,join_number);
								 //System.out.println("this opmeis jnjhfshffsdfhdf");
									////System.out.println(grp); 
									
									leftArg.replaceWith(grp);
									 join_number=rewritesp.getJoin_number();
									 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
									 unionrightleftfinalvars.add(rewritesp.unionrightleft);
							
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							}
							}
							if(rightArg instanceof LeftJoin)
							{
								
								
								
								List<Var> optionalrightleftfinalvars=new ArrayList<Var>();
								 //System.out.println("&&&&&&&&&&&&&&&&&&HHHHHHHHHHHHHHH&&&&&&&&&&&&&&");	
								 ////System.out.println(rightArg);	
								LeftJoin oprtion=(LeftJoin) rightArg;
								TupleExpr leftoptional = oprtion.getLeftArg();
								TupleExpr rightoptional = oprtion.getRightArg();
								//System.out.println("apply logic");
								
								if(leftoptional instanceof Join)
								{
									RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
									Var rightUnion=new Var("rightoptional");
									
									List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftoptional)).stream()
									.filter(var -> var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									
									List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftoptional)).stream()
									.filter(var -> !var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									
									//System.out.println("fprovVars: " + rightfprovVars);
									//System.out.println("remainingVars: " + rightremainingVars);
									//System.out.println("Join number: " + join_number);
									
									try {
										 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftoptional.clone(),join_number);
										QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftoptional.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
										 //System.out.println("this opmeis jnjhfshffssafasfa");
											//System.out.println(grp); 
											//System.out.println(leftoptional); 
											oprtion.setLeftArg((TupleExpr) grp);
										    // leftoptional.replaceWith(grp);
											 join_number=rewritesp.getJoin_number();
											 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
											 optionalrightleftfinalvars.add(rewritesp.unionrightleft);
										
									} catch (VisitorException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									
								}
								if(rightoptional instanceof Join)
								{
									 RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightoptional.clone(),join_number);
										Var leftUnion=new Var("leftoptional");	
									List<Var> leftfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightoptional)).stream()
									.filter(var -> var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									
									List<Var> leftremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightoptional)).stream()
									.filter(var -> !var.getName().startsWith("fprov"))
									.distinct() 
									.collect(Collectors.toList());
									//System.out.println("fprovVars: " + leftfprovVars);
									//System.out.println("remainingVars: " + leftremainingVars);
									//System.out.println("Join number: " + join_number);
									try {
										 RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightoptional.clone(),join_number);
										QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(rightoptional.clone(),leftfprovVars,leftremainingVars,leftUnion,join_number);
										 //System.out.println("this opmeis jnjhfshffsdfhdf");
											////System.out.println(grp); 
										 oprtion.setRightArg((TupleExpr) grp);
										// rightoptional.replaceWith(grp);
											 join_number=rewritesp.getJoin_number();
											 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
											 optionalrightleftfinalvars.add(rewritesp.unionrightleft);
									
									} catch (VisitorException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}	
								}
								//System.out.println("AMMMMMMMMMMMMMMMMMMMMMMMMMMIIIIIIIIIIIIIIIIIIIIII");
								//System.out.println(oprtion);
								//System.out.println(optionalrightleftfinalvars);
								
								Var unionlft=new Var();
								unionlft.setName("unionright");
								 RewriteStatementPattern rewritespx=new RewriteStatementPattern(oprtion.clone(),optional_number);
								QueryModelNode grp;
								try {
									grp = rewritespx.handlemultiplejoinBindAndGroup(oprtion.clone(),optionalrightleftfinalvars,variablesstart,unionlft,optional_number);
								
									//System.out.println("AMMMMMMMMMMMMMMMMMMMMMMMMMMIIIIIIIIIIIIIIIIIIIIII2");
									//System.out.println(grp);
									 unionrightleftfinalvars.add(unionlft);
									 if(grp!=null )
									 {
										 rightArg.replaceWith(grp);
									 }
								} catch (VisitorException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
									
					
							
							}


//System.out.println("danadanaanadanaaa");	
//System.out.println(unionrightleftfinalvars);
//Union un=(Union) topestunion.clone();

try {
	 RewriteStatementPattern rewritesp=new RewriteStatementPattern(topestunion.clone(),union_number);
	QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(topestunion.clone(),unionrightleftfinalvars,variablesstart,finalprovennacevariable,union_number);

	//System.out.println("danadanaanadanaaaFFFFFFFFFFFFF");	
	
	//System.out.println(grp);	
	//System.out.println(topestunion);
	
	 QueryModelNode currentfinal;
	 QueryModelNode current=topestunion;
	
		 do {
		        
	        		currentfinal=current;
	        		
	        
	        	 current=current.getParentNode();
	        	
	        	
	        }while(!((current instanceof QueryRoot)||(current==null))); 
	 
		 currentfinal.replaceWith(grp);
		
			//System.out.println("hello topest topest topest node"+topestunion);
	 
			
	
		 //System.out.println("done");
		 
		 union_number=rewritesp.getUnion_number();
		 totalunions.add(rewritesp.getTriple_patterns_union_varible());
	
} catch (VisitorException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	
	
			
			
		} */
		if(!(topestoptional==null))
		{
			
			
			unionrightleftfinalvars=new ArrayList<Var>();	
			
			LeftJoin union=(LeftJoin) topestoptional;
			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();
			//System.out.println("apply logic");
			
			
	
			
							if(rightArg instanceof Join)
							{
								RewriteStatementPattern rewritesp2=new RewriteStatementPattern(rightArg.clone(),join_number);	
							Var rightUnion=new Var("rightunion"+right_union_number);
							
							right_union_number=right_union_number+1;
							List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
							.filter(var -> var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							List<Var> rightremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(rightArg)).stream()
							.filter(var -> !var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							//System.out.println("fprovVars: " + rightfprovVars);
							//System.out.println("remainingVars: " + rightremainingVars);
							//System.out.println("Join number: " + join_number);
							
							try {
								 RewriteStatementPattern rewritesp=new RewriteStatementPattern(rightArg.clone(),join_number);
								QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(rightArg.clone(),rightfprovVars,rightremainingVars,rightUnion,join_number);
								 //System.out.println("this opmeis jnjhfshffssafasfa");
									////System.out.println(grp); 
									
									rightArg.replaceWith(grp);
									 join_number=rewritesp.getJoin_number();
									 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
									 unionrightleftfinalvars.add(rewritesp.unionrightleft);
								
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
							}
							if(leftArg instanceof Join)
							{
								 RewriteStatementPattern rewritesp2=new RewriteStatementPattern(leftArg.clone(),join_number);
								Var leftUnion=new Var("leftunion"+left_union_number);	
								left_union_number=left_union_number+1;
							List<Var> leftfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
							.filter(var -> var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							
							List<Var> leftremainingVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
							.filter(var -> !var.getName().startsWith("fprov"))
							.distinct() 
							.collect(Collectors.toList());
							//System.out.println("fprovVars: " + leftfprovVars);
							//System.out.println("remainingVars: " + leftremainingVars);
							//System.out.println("Join number: " + join_number);
							try {
								 RewriteStatementPattern rewritesp=new RewriteStatementPattern(leftArg.clone(),join_number);
								QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(leftArg.clone(),leftfprovVars,leftremainingVars,leftUnion,join_number);
								 //System.out.println("this opmeis jnjhfshffsdfhdf");
									////System.out.println(grp); 
									
									leftArg.replaceWith(grp);
									 join_number=rewritesp.getJoin_number();
									 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
									 unionrightleftfinalvars.add(rewritesp.unionrightleft);
							
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							}
							//System.out.println("danadanaanadanaaa");	
							//System.out.println(unionrightleftfinalvars);
							

							LeftJoin un=(LeftJoin) topestoptional.clone();
							//System.out.println(un);
							try {
							 RewriteStatementPattern rewritesp=new RewriteStatementPattern(un,optional_number);
							 QueryModelNode grp = rewritesp.handlemultiplejoinBindAndGroup(topestoptional.clone(),unionrightleftfinalvars,variablesstart,finalprovennacevariable,optional_number);
								
							 //System.out.println("lalallalalalalalallalalaa");	
							 
							 //System.out.println(grp);	
							 if(grp!=null )
							 {
								 topestoptional.replaceWith(grp); 
								 optional_number=rewritesp.getOptional_number();
								 totalunions.add(rewritesp.getTriple_patterns_union_varible());
							 }
							} catch (VisitorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
/*
try {
	 RewriteStatementPattern rewritesp=new RewriteStatementPattern(un,optional_number);
	//QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(un,unionrightleftfinalvars,variablesstart,finalprovennacevariable,optional_number);
	 QueryModelNode grp = rewritesp.handlemultiplejoinBindAndGroup(topestoptional.clone(),unionrightleftfinalvars,variablesstart,finalprovennacevariable,optional_number);
		
	//System.out.println("danadanaanadanaaa");
	//System.out.println(grp);
	//System.out.println(topestoptional);
	topestoptional.replaceWith(grp);
	//System.out.println(un);
	 union_number=rewritesp.getUnion_number();
	 totalunions.add(rewritesp.getTriple_patterns_union_varible());
	
} catch (VisitorException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
*/	
	////System.out.println(topestunion.getParentNode());

							
/*ye ha msla */
/*//System.out.println( "UNION UNION UNION UNION");
//System.out.println( unionrightleftfinalvars);

try {
	 RewriteStatementPattern rewritesp=new RewriteStatementPattern(topestunion.clone(),union_number);
	QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(topestunion,unionrightleftfinalvars,variablesstart,finalprovennacevariable,union_number);
	 //System.out.println("this opmeis jnjhfshffstsdfs");
		////System.out.println(grp); 
		
		
		 QueryModelNode currexyzntfinal;
		 QueryModelNode current=node.clone();
		
			 do {
			        
				 currexyzntfinal=current;
		        		
		        
		        	 current=current.getParentNode();
		        	
		        	
		        }while(!((current instanceof QueryRoot)||(current==null))); 
		 
			// //System.out.println("hello topest topest topest node"+currentfinal);
				
				////System.out.println("hello topest topest topest node"+grp);
		 
			 currexyzntfinal.replaceWith(grp);
		// grp.visit(this);
			 union_number=rewritesp.getUnion_number();
			 totalunions.add(rewritesp.getTriple_patterns_union_varible());
			// grp.visit(this);

		//System.out.println("danadanaanadanaaa");
		//System.out.println(currexyzntfinal);
		//System.out.println(grp);
		
	//	node.replaceWith(grp);
			
	//	 grp.visit(this);
} catch (VisitorException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
*/
/*
//System.out.println("fprovVars: " + fprovVars);
//System.out.println("remainingVars: " + remainingVars);


List<String> distinctNames2 = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars(leftArg)).stream()
.map(Var::getName) // Extract the name field
.distinct() // Get distinct values
.collect(Collectors.toList()); // Collect them into a list

//System.out.println(distinctNames2);

	*/		
			
			
		}
		if(!(topestjoin==null))
		{
			
			
try {
	 RewriteStatementPattern rewritesp=new RewriteStatementPattern(topjoin.clone(),join_number);
	QueryModelNode grp=rewritesp.handlemultiplejoinBindAndGroup(topjoin,totaljoinstatementsprovenanceVariables,variablesstart,finalprovennacevariable,join_number);
	 //System.out.println("this opmeis jnjhfshffsfsfsaf");
		////System.out.println(grp); 
		
		
		 QueryModelNode currentfinal;
		 QueryModelNode current=node.clone();
		
			 do {
			        
		        		currentfinal=current;
		        		
		        
		        	 current=current.getParentNode();
		        	
		        	
		        }while(!((current instanceof QueryRoot)||(current==null))); 
		 
			 //System.out.println("hello topest topest topest node"+currentfinal);
				
				//System.out.println("hello topest topest topest node"+grp);
		 
			 currentfinal.replaceWith(grp);
			 join_number=rewritesp.getJoin_number();
			 totaljoins.add(rewritesp.getTriple_patterns_join_varible());
			// grp.visit(this);

		
		
		
		
	//	node.replaceWith(grp);
			
	//	 grp.visit(this);
} catch (VisitorException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

}
		if(singlepattern!=null)
		{
			
		
			RewriteStatementPattern rewritesp2=new RewriteStatementPattern(singlepattern.clone());	
	
			
			List<Var> rightfprovVars = rewritesp2.getVariableVars(rewritesp2.getStatementPatternAndTriplePatternVars((TupleExpr) singlepattern)).stream()
			.filter(var -> var.getName().startsWith("fprov"))
			.distinct() 
			.collect(Collectors.toList());
			
			
			Var avscd=new Var("abcds");
			QueryModelNode sing=singlepattern.clone();
		//System.out.println("yes single"+sing);	
		//System.out.println(rightfprovVars);
		Var concatname=rewritesp2.getTriple_pattern_group_varible();
		////System.out.println(rewritesp2.getTriple_o));handlemul
		RewriteStatementPattern rewritesp=new RewriteStatementPattern(sing,statement_number);
		try {
			//QueryModelNode grp=rewritesp.handlesinglepatternBindAndGroup(sing,variablesstart,finalprovennacevariable,rightfprovVars,concatname);
		
			QueryModelNode grp=rewritesp.handlesinglepatternBindAndGroup(sing,rightfprovVars,variablesstart,finalprovennacevariable);
			
			
			 QueryModelNode currentfinal;
			 QueryModelNode current=node.clone();
			
				 do {
				        
			        		currentfinal=current;
			        		
			        
			        	 current=current.getParentNode();
			        	
			        	
			        }while(!((current instanceof QueryRoot)||(current==null))); 
			 
		 
			 
				 currentfinal.replaceWith(grp);
			
				// grp.visit(this);		
		
		
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//bindExtension=applyBind(singlepattern.clone(),totalstatementsprovenanceVariables,join_final_variable);
		}
		
	/*	changes in wikidata
	 * if(singlepattern!=null)
		{
			QueryModelNode sing=singlepattern.clone();
		//System.out.println("yes single"+sing);	
		RewriteStatementPattern rewritesp=new RewriteStatementPattern(sing,statement_number);
		try {
			QueryModelNode grp=rewritesp.handlesinglepatternBindAndGroup(sing,variablesstart,finalprovennacevariable);
		
			 QueryModelNode currentfinal;
			 QueryModelNode current=node.clone();
			
				 do {
				        
			        		currentfinal=current;
			        		
			        
			        	 current=current.getParentNode();
			        	
			        	
			        }while(!((current instanceof QueryRoot)||(current==null))); 
			 
		 
			 
				 currentfinal.replaceWith(grp);
			
				// grp.visit(this);		
		
		
		} catch (VisitorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//bindExtension=applyBind(singlepattern.clone(),totalstatementsprovenanceVariables,join_final_variable);
		}
		*/
		
		
		super.meetNode(node);
	}
	

}
