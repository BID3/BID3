package com.apimisuse.detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import com.apimisuse.main.Main;
import com.apimisuse.tokens.TokenUtils;
import com.apimisuse.utils.FileIO;
import com.apimisuse.utils.Utils;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtAssignmentImpl;
import spoon.support.reflect.code.CtBinaryOperatorImpl;
import spoon.support.reflect.code.CtBreakImpl;
import spoon.support.reflect.code.CtCatchImpl;
import spoon.support.reflect.code.CtConstructorCallImpl;
import spoon.support.reflect.code.CtContinueImpl;
import spoon.support.reflect.code.CtForEachImpl;
import spoon.support.reflect.code.CtForImpl;
import spoon.support.reflect.code.CtIfImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.code.CtLoopImpl;
import spoon.support.reflect.code.CtSynchronizedImpl;
import spoon.support.reflect.code.CtTryImpl;
import spoon.support.reflect.code.CtVariableReadImpl;
import spoon.support.reflect.code.CtVariableWriteImpl;
import spoon.support.reflect.code.CtWhileImpl;
import spoon.support.reflect.declaration.CtConstructorImpl;
import spoon.support.reflect.declaration.CtTypeImpl;

public class DetectBug {

	public static void detectBug(String fileName) throws NoSuchMethodException {
		CtTypeImpl ctti = Utils.prepareParse(fileName);		
		if(ctti != null){
			Set<CtMethod> allMethodDec = ctti.getMethods();
			for(CtMethod m : allMethodDec){	
				ArrayList<CtElement> allElements = 
						(ArrayList<CtElement>) m.getElements(new TypeFilter(CtElement.class));	
				
				CrtStatePattern crtStat = new CrtStatePattern("java.sql.Connection.createStatement()");//
				CrtStatePattern cachThrdPool = new CrtStatePattern("java.util.concurrent.Executors.newCachedThreadPool()");
				//MathRandomPattern mathRandom = new MathRandomPattern("java.lang.Math.random()");
				MkdirPattern mkdir = new MkdirPattern("java.io.File.mkdir()");
				ReplaceAllPattern replaceAll = new ReplaceAllPattern("java.lang.String.replaceAll(java.lang.String,java.lang.String)");
				
				FileWriterPattern fileWt = new FileWriterPattern("java.io.FileWriter.write");
				fileWt.hasBuffer(m);
				
				JSONObjectPattern getString = new JSONObjectPattern("org.json.JSONObject.getString(java.lang.String)");
				JSONObjectPattern getJSONArray = new JSONObjectPattern("org.json.JSONObject.getJSONArray(java.lang.String)");
				JSONObjectPattern getJSONObject = new JSONObjectPattern("org.json.JSONObject.getJSONObject(java.lang.String)");
				
				DateGetTimePattern dateGetTime = new DateGetTimePattern("java.util.Date.getTime()");
				dateGetTime.preDetect(m,allElements);
				
				StringEquals stringEqs = new StringEquals("java.lang.String.equals(java.lang.String)");
				stringEqs.preDetect(allElements);
																
				for(CtElement ct:allElements){
					
					mkdir.getDanerVariable(mkdir.dangerVariable, ct);
					
					dateGetTime.getObjAndUse(ct);
															
					stringEqs.getLocalV(ct);
																
					//String cleanedInvoc = "";
					SourcePosition srcPos = ct.getPosition();
																		
					if (ct instanceof CtInvocationImpl){
						String cleanedInvoc = Utils.cleanAPI(ct);
	
						CtInvocationImpl cti = (CtInvocationImpl) ct;						
						if (!srcPos.toString().equals("(unknown file)")){														
							crtStat.invoc(ct, srcPos, cleanedInvoc);
							cachThrdPool.invoc(ct, srcPos, cleanedInvoc);
							//mathRandom.invoc(srcPos, cleanedInvoc);	
							mkdir.invoc(ct,srcPos, cleanedInvoc);
							replaceAll.invoc(ct,srcPos, cleanedInvoc);
							fileWt.invoc(m,ct,srcPos, cleanedInvoc);
							
							getString.invoc(m, ct,srcPos, cleanedInvoc);
							getJSONArray.invoc(m, ct,srcPos, cleanedInvoc);
							getJSONObject.invoc(m, ct,srcPos, cleanedInvoc);
							
							dateGetTime.invoc(ct,srcPos, cleanedInvoc);	  //Case 2: directoly detect																																									
						}
					} 
										
					if(ct instanceof CtLoopImpl ){
						crtStat.updateLoop(ct);
						cachThrdPool.updateLoop(ct);
					}					
				}		
				crtStat.detect();
				cachThrdPool.detect();
				dateGetTime.detect(m);   //Case2: no other uses except date.getTime
				stringEqs.detect();
			}
		}	
	}

	
}
