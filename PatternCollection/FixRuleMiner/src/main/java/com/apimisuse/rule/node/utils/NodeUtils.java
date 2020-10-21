package com.apimisuse.rule.node.utils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.apimisuse.rule.ChangeRule;
import com.apimisuse.rule.RawChangeRule;
import com.apimisuse.rule.node.API;
import com.apimisuse.utils.Configure;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.utils.Pair;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtConstructorCallImpl;
import spoon.support.reflect.code.CtDoImpl;
import spoon.support.reflect.code.CtForEachImpl;
import spoon.support.reflect.code.CtForImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtWhileImpl;
import spoon.support.reflect.declaration.CtConstructorImpl;
import spoon.support.reflect.declaration.CtMethodImpl;
import spoon.support.reflect.declaration.CtPackageImpl;
import spoon.support.reflect.declaration.CtTypeImpl;

public class NodeUtils {
	
	private final static Boolean FALSE = Boolean.valueOf(false);

	public static enum TypeCmp {
		SAME, NOT, FAIL
	}

	public static Pair<String, Boolean> getUpdateContent(CtElement src, CtElement dst) {
		StringBuilder sb = new StringBuilder();
		Pair<String, Boolean> srcSignature = getElementSignature(src);
		Pair<String, Boolean> dstSignature = getElementSignature(dst);
		String[] Signatrues = { srcSignature.getFirst(), dstSignature.getFirst() };
//		Signatrues = Utils.compareElementUpdate(src, dst, Signatrues);
//		if (Signatrues != null) {
			sb.append(Signatrues[0]);
			sb.append(Configure.TAG_TO);
			sb.append(Signatrues[1]);
//		}
		sb.append(ChangeRule.SEP + getLine(src) + Configure.TAG_TO + getLine(dst));
		return new Pair<String, Boolean>(sb.toString(), srcSignature.getSecond() || dstSignature.getSecond());
	}

	public static Pair<String, Boolean> getDeleteContent(CtElement src) {
		StringBuilder sb = new StringBuilder();
		if (src instanceof CtFieldWrite) {
			CtElement parent = src.getParent();
			int count = 1;
			while (!(parent instanceof CtConstructor) && !(parent instanceof CtMethod) && parent != null
					&& count < 10) {
				parent = src.getParent();
				count++;
			}
			if (!(parent instanceof CtConstructor)) {
				return new Pair<String, Boolean>("", FALSE);
			}
		}
		Pair<String, Boolean> pair = getElementSignature(src);
		sb.append(pair.getFirst());
		sb.append(Configure.TAG_TO);
		sb.append(ChangeRule.SEP + getLine(src) + Configure.TAG_TO);
		return new Pair(sb.toString(), pair.getSecond());
	}

	public static Pair<String, Boolean> getInsertContent(CtElement dst) {
		StringBuilder sb = new StringBuilder();
		Pair<String, Boolean> dstSignature = getElementSignature(dst);
		// String dstTag = Utils.compareElementInsert(dst);
		if (dstSignature.getFirst() != null) {
			sb.append(Configure.TAG_TO);
			sb.append(dstSignature.getFirst() + ChangeRule.SEP + Configure.TAG_TO + getLine(dst));
			// sb.append(dstSignature + ChangeRule.SEP + TO + getLine(dst) + ChangeRule.SEP
			// + dstTag);
		}
		return new Pair<String, Boolean>(sb.toString(), dstSignature.getSecond());
	}

	public static Pair<String, Boolean> getContent(RawChangeRule rule) {
		Operation srcOp = rule.getSrcOp();
		Operation dstOp = rule.getDstOp();
		if (rule.isDelete()) {
			return getDeleteContent(srcOp.getSrcNode());
		} else if (rule.isInsert()) {
			return getInsertContent(dstOp.getSrcNode());
		} else {
			StringBuilder sb = new StringBuilder();
			CtElement src = srcOp.getSrcNode();
			CtElement dst = null;
			if (srcOp.equals(dstOp)) {
				dst = dstOp.getDstNode();
			} else {
				dst = dstOp.getSrcNode();
			}

			Pair<String, Boolean> srcSignature = getElementSignature(src);
			Pair<String, Boolean> dstSignature = getElementSignature(dst);
			String[] Signatrues = { srcSignature.getFirst(), dstSignature.getFirst() };
//			Signatrues = Utils.compareElementUpdate(src, dst, Signatrues);
//			if (Signatrues != null) {
				sb.append(Signatrues[0]);
				sb.append(Configure.TAG_TO);
				sb.append(Signatrues[1]);
//			}
			sb.append(ChangeRule.SEP + getLine(src) + Configure.TAG_TO + getLine(dst));
			return new Pair<String, Boolean>(sb.toString(), srcSignature.getSecond() || dstSignature.getSecond());
		}
	}

	public static Pair<String, Boolean> getElementSignature(CtElement element) {
		if (element instanceof CtWhileImpl) {
			CtWhileImpl loop = (CtWhileImpl) element;
			return new Pair<String, Boolean>(loop.getLoopingExpression().toString(), FALSE);
		} else if (element instanceof CtDoImpl) {
			CtDoImpl loop = (CtDoImpl) element;
			return new Pair<String, Boolean>(loop.getLoopingExpression().toString(), FALSE);
		} else if (element instanceof CtForImpl) {
			CtForImpl loop = (CtForImpl) element;
			return new Pair<String, Boolean>(loop.getForInit().toString() + ";" + loop.getExpression() + ";" + loop.getForUpdate(), FALSE);
		} else if (element instanceof CtForEachImpl) {
			CtForEachImpl loop = (CtForEachImpl) element;
			return new Pair<String, Boolean>(loop.getExpression().toString(), FALSE);
		} else if (element instanceof CtMethodImpl) {
			CtMethodImpl method = (CtMethodImpl) element;
			return new Pair<String, Boolean>(method.getModifiers() + " " + method.getSignature(), FALSE);
		} else if (element instanceof CtConstructorImpl) {
			CtConstructorImpl method = (CtConstructorImpl) element;
			return new Pair<String, Boolean>(method.getModifiers() + " " + method.getSignature(), FALSE);
		} else if (element instanceof CtTypeImpl) {
			CtTypeImpl type = (CtTypeImpl) element;
			return new Pair<String, Boolean>(type.getQualifiedName(), FALSE);
		} else if (element instanceof CtPackageImpl) {
			CtPackageImpl type = (CtPackageImpl) element;
			return new Pair<String, Boolean>(type.getQualifiedName(), FALSE);
		} else if (element instanceof CtInvocationImpl) {
			return extractInvoc(element);
		}

		return new Pair<String, Boolean>(element.toString(), FALSE);
	}
	
	public static boolean showType(CtTypeReference type) {
		if (type == null) {
			return false;
		}
		return showType(type.toString());
	}
	
	public static boolean showType(String typeStr) {
		if (typeStr == null || typeStr.isEmpty() 
				|| "void".equals(typeStr) || "?".equals(typeStr)) {
			return false;
		}
		return true;
	}

	public static Pair<String, Boolean> extractInvoc(CtElement element) {
		CtInvocationImpl invocation = (CtInvocationImpl) element;
		if (invocation == null)
			return new Pair("", FALSE);

		String recever = null;
		boolean parseTypeFailed = false;
		if (invocation.getTarget() != null) {
			CtTypeReference typeReference = invocation.getTarget().getType();
			if (showType(typeReference)) {
				recever = typeReference.toString();
			} else {
				parseTypeFailed = true;
				recever = invocation.getTarget().toString();
			}
		}

		String name = getMethodName(invocation);

		StringBuffer args = new StringBuffer();
		List<CtExpression> argList = invocation.getArguments();
		if (argList.size() > 0) {
			CtExpression expression = argList.get(0);
			if (showType(expression.getType())) {
				args.append(expression.getType().toString());
			} else {
				parseTypeFailed = true;
				args.append(expression.toString());
			}
			for (int i = 1; i < argList.size(); i++) {
				expression = argList.get(i);
				if (showType(expression.getType())) {
					args.append(',').append(expression.getType().toString());
				} else {
					parseTypeFailed = true;
					args.append(',').append(expression.toString());
				}
			}
		}

//		String[] cases = { "equals(java.lang.String)", "equalsIgnoreCase(java.lang.String)",
//				"contains(java.lang.String)", "replace(java.lang.String,java.lang.String)",
//				"replaceAll(java.lang.String,java.lang.String)", "startsWith(java.lang.String)",
//				"indexOf(java.lang.String)", "lastIndexOf(java.lang.String)", "toLowerCase()",
//				"replaceFirst(java.lang.String,java.lang.String)", "endsWith(java.lang.String)",
//				"matches(java.lang.String)", "indexOf(char)", "lastIndexOf(char)", "toLowerCase()", "toUpperCase()",
//				"trim()", "split(java.lang.String)", "substring(int,int)" };
//		List<String> caseList = Arrays.asList(cases);
//
//		if (caseList.contains(nameAndargus)) {
//			returnString = "*." + onlyMethodName + SPLITARGS + argumes;
//		}

		StringBuffer complete = new StringBuffer();
		complete.append(recever == null ? "" : recever + ".").append(name).append('(').append(args).append(')');

		return new Pair(complete.toString(), Boolean.valueOf(parseTypeFailed));
	}

	public static int getLine(CtElement src) {
		return src.getPosition().getLine();

	}

	public static String getAction(Operation op) {
		return op.getAction().getClass().getSimpleName();
	}

	public static String getLabel(Operation op) {
		return op.getAction().getNode().getLabel();
	}

	public static ITree getParent(Operation op) {
		return op.getAction().getNode().getParent();
	}

	public static TreeContext getContext(Diff result) {
		try {
			Field f = result.getClass().getDeclaredField("context"); // NoSuchFieldException
			f.setAccessible(true);
			return (TreeContext) f.get(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getNodeType(Operation op, Diff result) {
		return getContext(result).getTypeLabel(op.getAction().getNode());
	}

	public static String getNodeType(ITree node, Diff result) {
		return getContext(result).getTypeLabel(node);
	}

	public static List<CtInvocationImpl> getMethodInv(CtElement element) {
		return element.getElements(new TypeFilter<>(CtInvocationImpl.class));
	}

	public static TypeCmp sameType(List<CtExpression> srcs, List<CtExpression> tars) {
		if (srcs.size() != tars.size()) {
			return TypeCmp.NOT;
		}
		TypeCmp result = TypeCmp.SAME;
		for (int i = 0; i < srcs.size(); i++) {
			switch (sameType(srcs.get(i), tars.get(i))) {
			case NOT:
				result = TypeCmp.NOT;
				break;
			case FAIL:
				result = TypeCmp.FAIL;
				break;
			case SAME:
				break;
			}
			if (result == TypeCmp.NOT) {
				break;
			}
		}
		return result;
	}

	public static TypeCmp sameType(CtExpression src, CtExpression tar) {
		if (src == null) {
			return tar == null ? TypeCmp.SAME : TypeCmp.NOT;
		}
		CtTypeReference srcType = src.getType();
		CtTypeReference tarType = tar.getType();
		if (srcType != null && tarType != null) {
			String srcTypeStr = srcType.toString();
			String tarTypeStr = tarType.toString();
			if ("void".equals(srcTypeStr)) {
				srcTypeStr = src.toString();
			}
			if ("void".equals(tarTypeStr)) {
				tarTypeStr = tar.toString();
			}
			return srcTypeStr.equals(tarTypeStr) ? TypeCmp.SAME : TypeCmp.NOT;
		} else if (src.toString().equals(tar.toString())) {
				if (srcType != null) {
					tar.setType(srcType.clone());
				} else if(tarType != null) {
					src.setType(tarType.clone());
				}
			return TypeCmp.SAME;
		}

		return TypeCmp.FAIL;
	}

	public static String getReceiver(CtElement node) {
		String receiver = "";
		if (node instanceof CtInvocationImpl) {
			CtInvocationImpl ctInvocationImpl = (CtInvocationImpl) node;
			receiver = ctInvocationImpl.getTarget() == null ? "" : ctInvocationImpl.getTarget().toString();
		}
		return receiver;
	}

	public static String getMethodName(CtElement node) {
		String invocation = "";
		if (node instanceof CtInvocationImpl)
			invocation = ((CtInvocationImpl) node).getExecutable().toString();
		else
			invocation = ((CtConstructorCallImpl) node).getExecutable().toString();
		int index = invocation.lastIndexOf("(");
		return invocation.substring(0, index);
	}

	public static String getMethodArgs(CtElement node) {
		if (node instanceof CtInvocationImpl)
			return ((CtInvocationImpl) node).getArguments().toString();
		else
			return ((CtConstructorCallImpl) node).getArguments().toString();
	}

	public static String getMethodArgTypes(CtElement node) {
		if (node instanceof CtInvocationImpl)
			return ((CtInvocationImpl) node).getActualTypeArguments().toString();
		else
			return ((CtConstructorCallImpl) node).getActualTypeArguments().toString();
	}
	
	public static API parseAPI(CtInvocation invocationImpl) {
		Pair<String, String> retPair = null;
		String name = getMethodName(invocationImpl);
		Pair<String, String> clazz = null;
		CtExpression expressionImpl = invocationImpl.getTarget(); 
		if (expressionImpl != null) {
			String type = expressionImpl.getType() == null ? null : expressionImpl.getType().toString();
			String expr = expressionImpl.toString().isEmpty() ? "this" : expressionImpl.toString();
			clazz = new Pair<String, String>(type, expr);
		}
		List<Pair<String, String>> args = new LinkedList<>();
		List<CtExpression> arguments = invocationImpl.getArguments();
		for(int i = 0; i < arguments.size(); i ++) {
			CtExpression expression = arguments.get(i);
			String type = expression.getType() == null ? null : expression.getType().toString();
			args.add(new Pair<String, String>(type, expression.toString()));
		}
		return new API(null, clazz, name, args);
	}
}
