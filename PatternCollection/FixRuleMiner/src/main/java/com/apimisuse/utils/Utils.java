package com.apimisuse.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.apimisuse.rule.ChangeRule;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtBinaryOperatorImpl;
import spoon.support.reflect.code.CtInvocationImpl;

public class Utils
{
	public static final String SEPARATOR = "MUTATORCHECKPOINT";
	public static final String VOID_METHOD_CALL_GUARD_MUTATOR = SEPARATOR + "[Void Method Call Guard Mutator]" + SEPARATOR; //Method=>If 
	public static final String RETURNING_METHOD_CALL_GUARD_MUTATOR = SEPARATOR + "[Returning Method Call Guard Mutator]" + SEPARATOR; // =>If
	public static final String RETURNING_DEREFERENCE_GUARD_MUTATOR = SEPARATOR + "[Returning Dereference Guard Mutator]" + SEPARATOR;
	public static final String NON_VOID_METHOD_CALL_GUARD_MUTATOR = SEPARATOR + "[Non-Void Method Call Guard Mutator]" + SEPARATOR; //Method=>Conditional
	
	public static final String RELATIONAL_OPERATION_REPLACEMENT = SEPARATOR + "[Relational Operation Replacement]" + SEPARATOR; //check "Operation Replacement with ConditionalOp=>ConditionalOp"
	public static final String ARITHMETIC_OPRATION_REPLACEMENT = SEPARATOR + "[Arithmetic Operation Replacement]" + SEPARATOR; //check "Operation Replacement with ArithmeticOp=>ArithmeticOp"
	public static final String OPERATION_REPLACEMENT = SEPARATOR + "[Operation Replacement]" + SEPARATOR; // 
	
	public static final String METHOD_NAME_MUTATOR = SEPARATOR + "[Method Name Mutator]" + SEPARATOR;  //MethodName=>MethodName
	//  Local To Field Access Mutator : 		Variable => field
	//  Field To Local Access Mutator : 		field => Variable
	// 	Local Name Mutator: 					Variable => Variable
	// 	Field Name Mutator: 					Field => Field
	//	Argument List Mutator: 					MethodArgument => MethodArgument
	//	Field Access To Method Call Mutator: 	Filed to Method
	
	public static final String ARITHMETIC_OPERATION_DELETION = SEPARATOR + "[Arithmetic Operation Deletion]" + SEPARATOR; //ArithmeticOp=>VariableRead
	public static final String CASE_BREAKER_MUTATOR = SEPARATOR + "[Case Breaker Mutator]" + SEPARATOR;   // =>Break 
	public static final String METHOD_CALL_RESULT_GUARD_MUTATOR = SEPARATOR + "[Method Call Result Guard Mutator]" + SEPARATOR; // =>If
	public static final String DEREFERENCE_GUARD_MUTATOR = SEPARATOR + "[Dereference Guard Mutator]" + SEPARATOR;

	////----------------------------
	public static final String NON_VOID_METHOD_CALL_MUTAOTR = SEPARATOR + "[Non-Void Method Call Mutator]" + SEPARATOR;
	public static final String INVERT_NEGS_MUTATOR = SEPARATOR + "[Invert Negs Mutator]" + SEPARATOR;
	public static final String INLINE_CONSTANTS_MUTATOR = SEPARATOR + "[InlineConstantMutator]" + SEPARATOR;
	public static final String RETURN_VALS_MUTATOR = SEPARATOR + "[Return Vals Mutator]" + SEPARATOR;
	public static final String INCREMENT_MUTATOR = SEPARATOR + "[Increments Mutator]" + SEPARATOR;
	public static final String CONSTRUCTOR_CALL_MUTATOR = SEPARATOR + "[Constructor Call Mutator]" + SEPARATOR;
	public static final String ARG_PROP_MUTATOR = SEPARATOR + "[Argument Propagation Mutator]" + SEPARATOR;
	public static final String NAKED_REC_MUTATOR = SEPARATOR + "[Naked Receiver Mutator]" + SEPARATOR;
	public static final String SWITCH_MUTATOR = SEPARATOR + "[Switch Mutator]" + SEPARATOR;
	
	public static final String NEGATE_CONDITIONAL = SEPARATOR + "[Negate Conditionals Mutator]" + SEPARATOR;
	public static final String CONDITIONAL_BOUNDARY = SEPARATOR + "[Conditionals Boundary Mutator]" + SEPARATOR;
	public static final String REMOVE_CONDITIONAL_IF = SEPARATOR + "[Remove Conditional Mutator If]" + SEPARATOR;
	public static final String REMOVE_CONDITIONAL_ELSE = SEPARATOR + "[Remove Conditional Mutator Else]" + SEPARATOR;
	public static final String MATH_MUTATOR = SEPARATOR + "[Math Mutator]" + SEPARATOR;
	
	public static boolean isNullCheck(Operation op, Diff result) {
		List<ITree> children = op.getAction().getNode().getChildren();
		//System.out.println("Parent: " + op);
		for (ITree child : children) {
			// System.out.println(child.getLabel());
			if (child.getLabel().equals(BinaryOperatorKind.NE.name())) {
				return (child.getChild(1).getLabel().equals("null"));
			}
		}
		return false;
	}
	
	public static boolean isZeroCheck(Operation op, Diff result) {
		List<ITree> children = op.getAction().getNode().getChildren();
		//System.out.println("Parent: " + op);
		for (ITree child : children) {
			//System.out.println("Child: "+child.getChild(1).getLabel());
			if (child.getLabel().equals(BinaryOperatorKind.NE.name())) {
				return (child.getChild(1).getLabel().equals("0"));
			}
		}
		return false;
	}

	public static boolean isInstanceCheck(Operation op, Diff result) {
		List<ITree> children = op.getAction().getNode().getChildren();
		//System.out.println("Parent: " + op);
		for (ITree child : children) {
			// System.out.println(child.getLabel());
			if (child.getLabel().equals(BinaryOperatorKind.INSTANCEOF.name())) {
				return true;
			}
		}
		return false;
	}
	
	
	public static void  outFile(String file, String content, boolean append) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(content);
		writer.newLine();
		writer.flush();
		writer.close();
	}

	public static boolean isRangeCheck(Operation op, Diff result) {
		List<ITree> children = op.getAction().getNode().getChildren();
		//System.out.println("Parent: " + op);
		for (ITree child : children) {
			String label = child.getLabel();
			// System.out.println(child.getLabel());
			if (label.equals(BinaryOperatorKind.LE.name())
					|| label.equals(BinaryOperatorKind.LT.name())
					|| label.equals(BinaryOperatorKind.GE.name())
					|| label.equals(BinaryOperatorKind.GT.name()))
				return true;
		}
		return false;
	}
	
	public static Boolean whatToPrint(ChangeRule rule){
		//return Utils.ifPrint(rule.pattern) && !rule.content.equals("");
		return !rule.getContent().equals("");
	}
	
	
	public static void StoretoFile(List<ChangeRule> allRules,String path) throws IOException {
		//BufferedWriter bw = new BufferedWriter(new FileWriter(path,true));
		for(ChangeRule rule:allRules) {
			if(whatToPrint(rule)) {
				rule.toJSON(path);
				/*bw.write(rule.toString());
				bw.write("\n");
				*/
			}			
		}
		//bw.flush();
		//bw.close();
	}
	public static ArrayList<CtBinaryOperatorImpl> getSomeConds(CtBinaryOperatorImpl conds,String eq) {
		ArrayList<CtExpression> totalConds = getCondList(conds);
		ArrayList<CtBinaryOperatorImpl> returnList = new ArrayList<>();
		for(CtExpression cte : totalConds) {
			if(cte instanceof CtBinaryOperatorImpl) {
				CtBinaryOperatorImpl ctee = (CtBinaryOperatorImpl) cte;
				if(ctee.getKind().toString().equals(eq) && ctee.getRightHandOperand().toString().equals("null")){
					returnList.add(ctee);
				}
			}
		}
		return returnList;
	}
	public static boolean containsOnce(String source, String target) {
		int length = source.split(target).length;
		if(source.contains("return")) {
			if(length > 2) {
				return false;
			}else
				return true;
		}else
			return false;
	}
//	public static String compareElementInsert(CtElement dst) {
//		String tag = "";
//		if(dst instanceof CtIfImpl) {
//			CtIfImpl dstIf = (CtIfImpl) dst;
//			CtExpression ifCond = dstIf.getCondition();			
//			if(ifCond.toString().contains("null")) {
//				tag = "Add If null checker";				
//			}
//			return tag;
//		}
//		return tag;
//	}
	
	
	
//	public static String[] compareElementUpdate(CtElement src, CtElement dst, String[] sigs) {
////		if(sigs[0].equals(sigs[1])) {
////			return null;
////		}
//		
//		/*
//		
//		
//		// NonVoidMethodCallMutator
//		if (src instanceof CtInvocation && dst instanceof CtLiteral) {
//		    if (dst.toString().matches("0|false|null")) {
//		        sigs[0] = NON_VOID_METHOD_CALL_MUTAOTR + src.toString();
//		        sigs[1] = dst.toString();
//		        return sigs;
//		    }
//		}
//		
//		if (src instanceof CtExpression && dst instanceof CtUnaryOperator) {
//		    if (((CtUnaryOperator<?>) dst).getKind() == UnaryOperatorKind.NOT
//		            && src.equals(((CtUnaryOperator<?>) dst).getOperand())) {
//		        sigs[0] = NEGATE_CONDITIONAL + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//		    }
//		}
//		
//		if (dst instanceof CtExpression && src instanceof CtUnaryOperator) {
//            if (((CtUnaryOperator<?>) src).getKind() == UnaryOperatorKind.NOT
//                    && dst.equals(((CtUnaryOperator<?>) src).getOperand())) {
//                sigs[0] = NEGATE_CONDITIONAL + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//        }
//		
//		if (src instanceof CtBinaryOperator && dst instanceof CtBinaryOperator) {
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.EQ
//		            && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.NE)
//		            || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.EQ
//		                    && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.NE)) {
//		        sigs[0] = NEGATE_CONDITIONAL + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//		    }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.LE
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.LT)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.LE
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.LT)) {
//		        sigs[0] = CONDITIONAL_BOUNDARY + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//		    }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.GE
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.GT)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.GE
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.GT)) {
//                sigs[0] = CONDITIONAL_BOUNDARY + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.PLUS
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.MINUS)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.PLUS
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.MINUS)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.MUL
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.DIV)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.MUL
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.DIV)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.BITOR
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.BITAND)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.BITOR
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.BITAND)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.MOD
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.MUL)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.MOD
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.MUL)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.BITXOR
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.BITAND)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.SR
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.SL)
//                    || (((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.SR
//                            && ((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.SL)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		    if ((((CtBinaryOperator<?>) src).getKind() == BinaryOperatorKind.USR
//                    && ((CtBinaryOperator<?>) dst).getKind() == BinaryOperatorKind.SL)) {
//                sigs[0] = MATH_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//		}
//		
//		if (src instanceof CtIf) {
//		    final CtElement thenBranch = ((CtIf) src).getThenStatement();
//		    final CtElement elseBranch = ((CtIf) src).getElseStatement();
//		    if (dst.equals(thenBranch)) {
//		        sigs[0] = REMOVE_CONDITIONAL_ELSE + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//		    } else if (dst.equals(elseBranch)) {
//		        sigs[0] = REMOVE_CONDITIONAL_IF + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//		    }
//		}
//		
//		// InvertNegsMutator
//		if (src instanceof CtLiteral && dst instanceof CtLiteral) {
//		    final String srcLit = ((CtLiteral<?>) src).toString();
//		    final String dstLit = ((CtLiteral<?>) dst).toString();
//		    if (srcLit.equals(String.format("-%s", dstLit)) || dstLit.equals(String.format("-%s", srcLit))) {
//		        sigs[0] = INVERT_NEGS_MUTATOR + srcLit;
//		        sigs[1] = dstLit;
//		        return sigs;
//		    } else if (dstLit.matches("0|1|-1")) {
//		        if (src.getParent() instanceof CtReturn && dst.getParent() instanceof CtReturn) {
//		            sigs[0] = RETURN_VALS_MUTATOR + src.toString();
//                    sigs[1] = dst.toString();
//                    return sigs;
//		        } else {
//    		        sigs[0] = INLINE_CONSTANTS_MUTATOR + src.toString();
//                    sigs[1] = dst.toString();
//                    return sigs;
//		        }
//		    } else if (srcLit.matches("[0-9]+") && dstLit.matches("[0-9]+")) {
//		        final int srcNum = Integer.parseInt(srcLit);
//		        final int dstNum = Integer.parseInt(dstLit);
//		        if (srcNum == dstNum - 1 || srcNum == dstNum + 1) {
//		            sigs[0] = INLINE_CONSTANTS_MUTATOR + src.toString();
//	                sigs[1] = dst.toString();
//	                return sigs;
//		        }
//		    }
//		}
//		if (dst.toString().equals("null") && dst.getParent() instanceof CtReturn && src.getParent() instanceof CtReturn) {
//		    sigs[0] = RETURN_VALS_MUTATOR + src.toString();
//            sigs[1] = dst.toString();
//            return sigs;
//		}
//        if (src instanceof CtUnaryOperator && ((CtUnaryOperator<?>) src).getOperand().equals(dst)) {
//            switch (((CtUnaryOperator<?>) src).getKind()) {
//            case NOT:
//                if (src.getParent() instanceof CtReturn && src.getParent() instanceof CtReturn) {
//                    sigs[0] = RETURN_VALS_MUTATOR + src.toString();
//                    sigs[1] = dst.toString();
//                    return sigs;
//                }
//                break;
//            case NEG:
//                sigs[0] = INVERT_NEGS_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            case PREINC:
//            case POSTINC:
//            case PREDEC:
//            case POSTDEC:
//                sigs[0] = INCREMENT_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            default:
//
//            }
//        }
//        if (dst instanceof CtUnaryOperator && ((CtUnaryOperator<?>) dst).getOperand().equals(src)) {
//            switch (((CtUnaryOperator<?>) dst).getKind()) {
//            case NOT:
//                if (src.getParent() instanceof CtReturn && src.getParent() instanceof CtReturn) {
//                    sigs[0] = RETURN_VALS_MUTATOR + src.toString();
//                    sigs[1] = dst.toString();
//                    return sigs;
//                }
//                break;
//            case NEG:
//                sigs[0] = INVERT_NEGS_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            case PREINC:
//            case POSTINC:
//            case PREDEC:
//            case POSTDEC:
//                sigs[0] = INCREMENT_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            default:
//
//            }
//        }
//        if (src instanceof CtNewClass || src instanceof CtNewArray || src instanceof CtLambda) {
//            if (dst instanceof CtLiteral && dst.toString().equals("null")) {
//                sigs[0] = CONSTRUCTOR_CALL_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            }
//        }
//
//        if (src instanceof CtInvocation) {
//            final CtElement tgt = ((CtInvocation<?>) src).getTarget();
//            if (tgt.equals(dst)) {
//                sigs[0] = NAKED_REC_MUTATOR + src.toString();
//                sigs[1] = dst.toString();
//                return sigs;
//            } else {
//                final List<CtExpression<?>> args = ((CtInvocation<?>) src).getArguments();
//                if (!args.isEmpty() && args.get(args.size() - 1).equals(dst)) {
//                    sigs[0] = ARG_PROP_MUTATOR + src.toString();
//                    sigs[1] = dst.toString();
//                    return sigs;
//                }
//            }
//        }
//        
//        if (src instanceof CtSwitch && dst instanceof CtSwitch) {
//            List<?> srcCases = ((CtSwitch<?>) src).getCases();
//            CtCase<?>  srcDef = null;
//            CtCase<?> srcFirst = null;
//            if (!srcCases.isEmpty()) {
//                srcDef = (CtCase<?>) srcCases.get(srcCases.size() - 1);
//                srcFirst = (CtCase<?>) srcCases.get(0);
//            }
//            List<?> dstCases = ((CtSwitch<?>) dst).getCases();
//            if (srcDef != null) {
//                boolean ok = true;
//                if (srcCases.size() == dstCases.size()) {
//                    for (int i = 0; i < dstCases.size() - 1; i++) {
//                        if (!srcDef.equals(dstCases.get(i))) {
//                            ok = false;
//                            break;
//                        }
//                    }
//                    if (!dstCases.get(dstCases.size() - 1).equals(srcFirst)) {
//                        ok = false;
//                    }
//                } else {
//                    ok = false;
//                }
//                if (ok) {
//                    sigs[0] = SWITCH_MUTATOR + src.toString();
//                    sigs[1] = dst.toString();
//                    return sigs;
//                }
//                ok = true;
//                for (int i = 0; i < srcCases.size() - 1; i++) {
//                    if (srcDef.equals(srcCases.get(i))) {
//                        ok = false;
//                        break;
//                    }
//                }
//                if (ok) {
//                    boolean found = false;
//                    for (int i = 0; i < dstCases.size() - 1; i++) {
//                        if (srcDef.equals(dstCases.get(i))) {
//                            found = true;
//                            break;
//                        }
//                    }
//                    if (!found) {
//                        ok = false;
//                    }
//                    if (ok) {
//                        sigs[0] = SWITCH_MUTATOR + src.toString();
//                        sigs[1] = dst.toString();
//                        return sigs;
//                    }
//                }
//            }
//        }
//		
//		*/
//		
//		
//		/*
//		//////////////////// 8888888888888888888888888888888888888888888888888888888888
//		if(src instanceof CtBinaryOperatorImpl) {
//			CtBinaryOperatorImpl srcE = (CtBinaryOperatorImpl)src;			
//			String LsrcE = srcE.getLeftHandOperand().toString();
//			String RsrcE = srcE.getRightHandOperand().toString();
//			
//			if(dst instanceof CtBinaryOperatorImpl) {				
//				CtElement srcParent = src.getParent();
//				CtElement dstParent = dst.getParent();
//												
//				CtBinaryOperatorImpl dstE = (CtBinaryOperatorImpl)dst;
//				String LdstE = dstE.getLeftHandOperand().toString();
//				String RdstE = dstE.getRightHandOperand().toString();
//				
//				if(LsrcE.equals(LdstE) && RsrcE.equals(RdstE)) {   
//					sigs[0] = OPERATION_REPLACEMENT + src.getParent().toString();
//					sigs[1] = dst.getParent().toString();				
//				}				
//			}else {
//				String dstStr = removeFirstandEnd(dst.toString());
//				if(removeFirstandEnd(LsrcE).equals(dstStr) || removeFirstandEnd(RsrcE).equals(dstStr)) {
//					sigs[0] = ARITHMETIC_OPERATION_DELETION + src.getParent().toString();
//					sigs[1] = dst.getParent().toString();
//				}
//			}
//			return sigs;
//		}
//		
//		*/
//		if(src instanceof CtInvocationImpl && dst instanceof CtInvocationImpl) {
//			String srcString = sigs[0];
//			String dstString = sigs[1];
//			String srcArg = srcString.split(Configure.TAG_SPLITARGS)[1];
//			String dstArg = dstString.split(Configure.TAG_SPLITARGS)[1];
//			String srcMethod = srcString.split(Configure.TAG_SPLITARGS)[0];
//			String dstMethod = dstString.split(Configure.TAG_SPLITARGS)[0];
//			/*
//			if (srcArg.equals(dstArg)){
//				//sigs[0] = METHOD_NAME_MUTATOR + srcMethod;
//				sigs[0] = srcMethod;
//				sigs[1] = dstMethod;
//			}
//			*/
//			return sigs;			
//		}
//		
//		/*
//		// Void Method Call Guard Mutator "Method=>If" in results
//		if(dst instanceof CtIfImpl){
//			
//			String srcmethod = src.toString();	 //full method
//			
//			CtIfImpl dstIf = (CtIfImpl)dst;
//			String ifThen = trimBody(dstIf.getThenStatement());//if then body
//			String elseBody = "";
//			if(dstIf.getElseStatement() != null)
//				elseBody = trimBody(dstIf.getElseStatement()); //else body
//			
//			if(dstIf.getCondition() instanceof CtBinaryOperatorImpl) {
//				CtBinaryOperatorImpl full_cond = (CtBinaryOperatorImpl) (dstIf.getCondition()); //condition
//							
//				ArrayList<CtInvocationImpl> srcInvocs =  getFirstInvocation(src); //invocations
//				for(CtInvocationImpl srcInvoc : srcInvocs) {    										
//					String target = srcInvoc.getTarget().toString();
//					boolean matchNotNull = nullChecker(full_cond,target,"NE");
//					boolean matchNull = nullChecker(full_cond,target,"EQ");								
//					if((srcmethod.equals(ifThen) && matchNotNull)||(srcmethod.equals(elseBody) && matchNull)){ //match mutator
//						sigs[0] = VOID_METHOD_CALL_GUARD_MUTATOR + srcmethod;
//						sigs[1] = dstIf.toString();
//						break;
//					}
//				}
//			}
//			return sigs;
//		}
//		
//		*/
//		
//		/*
//		if(dst instanceof CtConditionalImpl) {
//			
//			CtConditionalImpl dstCond = (CtConditionalImpl) dst;
//			String elseString = dstCond.getElseExpression().toString();			
//			String thenString = dstCond.getThenExpression().toString();
//			CtExpression dstCondExp = dstCond.getCondition();
//						
//			String srcString = src.toString();
//			
//			
//			if (dstCondExp instanceof CtBinaryOperator) {
//					CtBinaryOperator condExp = (CtBinaryOperator) dstCondExp;
//					String leftExp = condExp.getLeftHandOperand().toString();
//					String operand = condExp.getKind().toString();
//					String rightExp = condExp.getRightHandOperand().toString();
//				
//					
//				if(rightExp.equals("null")) {
//					if((operand.equals("EQ") && srcString.equals(elseString)) || 
//								(operand.equals("NE") && srcString.equals(thenString))) {
//						if(src instanceof CtInvocationImpl) {
//							CtInvocationImpl scrInvoc = (CtInvocationImpl) src;
//							if(scrInvoc.getTarget() != null){
//								String target = scrInvoc.getTarget().toString();
//								if(removeFirstandEnd(leftExp).equals(removeFirstandEnd(target))) {
//									sigs[0] = NON_VOID_METHOD_CALL_GUARD_MUTATOR + srcString;
//									sigs[1] = dst.toString();
//								}
//							}
//						}
//						if(src instanceof CtFieldWrite || src instanceof CtFieldRead) {
//							CtElement targetE = null;
//							if(src instanceof CtFieldWrite){
//								CtFieldWrite scrInvoc = (CtFieldWrite) src;
//								targetE = scrInvoc.getTarget();
//							}else if (src instanceof CtFieldRead){
//								CtFieldRead scrInvoc = (CtFieldRead) src;
//								targetE = scrInvoc.getTarget();
//							}
//							if(targetE != null){
//								String target = targetE.toString();
//								if(removeFirstandEnd(leftExp).equals(removeFirstandEnd(target))) {
//									sigs[0] = DEREFERENCE_GUARD_MUTATOR + srcString;
//									sigs[1] = dst.toString();
//								}
//							}
//						}
//					}		
//				}	
//			}		
//		}
//		*/
//		return sigs;
//	}

	public static ArrayList<CtInvocationImpl> getFirstInvocation(CtElement src) {
		ArrayList<CtElement> argues = new ArrayList<CtElement>();
		
		ArrayList<CtInvocationImpl> allInvocs = (ArrayList<CtInvocationImpl>) src.getElements(new TypeFilter(CtInvocationImpl.class));
		
		for(CtInvocationImpl cte : allInvocs) {
			if (cte.getArguments().size() > 0) {
				argues.addAll(cte.getArguments());
			}
		}
		
		for(CtElement cte : allInvocs) {
			if (cte instanceof CtInvocationImpl) {
				((CtInvocationImpl) cte).setArguments(null);
			}
		}
		ArrayList<CtInvocationImpl> returnInvoc = new ArrayList<>();
		for(CtInvocationImpl cte: allInvocs) {
			if (!argues.contains(cte)) {
				returnInvoc.add(cte);
			}
		}
		return returnInvoc;
		
	}
	public static String trimBody(CtElement body) {
		return body.toString().replace("{","").replaceAll("}", "").trim().replace(";", "");
	}
	public static boolean ifPrint(String rule){		
		ArrayList<String> rulesForPrint = 
					new ArrayList<String>(Arrays.asList(Configure.rulesForPrint));
		ArrayList<String> printRule = new ArrayList<String>();
		for(String srcR : rulesForPrint) {
			for(String dstR : rulesForPrint) {
				if (!(srcR.equals("") && dstR.equals("")))
					printRule.add(srcR + "=>" + dstR);				
			}			
		}
		if(printRule.contains(rule)) {
			return true;
		}		
		return false;		
	}
	//check if a conditional contains one ==null or !=null
	public static boolean nullChecker(CtBinaryOperatorImpl fullExp,String target,String eq) {  	
		ArrayList<CtExpression> conds = getCondList(fullExp);		
		for(CtExpression cte:conds) {
			if(cte instanceof CtBinaryOperatorImpl) {
				CtBinaryOperatorImpl ctee = (CtBinaryOperatorImpl) cte;
				if(removeFirstandEnd(target).equals(removeFirstandEnd(ctee.getLeftHandOperand().toString())) && ctee.getKind().toString().equals(eq) 
						&& ctee.getRightHandOperand().toString().equals("null")){
					return true;
				}
			}
		}
		return false;
	}
	public static String removeFirstandEnd(String str) {
		if(str.startsWith("(") && str.endsWith(")")){
			String s  = str.substring(1,str.length() - 1);
			return s;
		}else
			return str;
	}

	public static ArrayList<CtExpression> getCondList(CtBinaryOperatorImpl fullExp){
		ArrayList<CtExpression> conds = new ArrayList<CtExpression>();
		while(fullExp.getLeftHandOperand() instanceof CtBinaryOperatorImpl) {
			conds.add(fullExp.getRightHandOperand());
			fullExp = (CtBinaryOperatorImpl)fullExp.getLeftHandOperand();			
		}
		conds.add(fullExp);
		return conds;
	}
}


