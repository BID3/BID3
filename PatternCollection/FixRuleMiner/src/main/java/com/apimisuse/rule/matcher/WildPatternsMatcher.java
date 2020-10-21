package com.apimisuse.rule.matcher;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.apimisuse.rule.RawChangeRule;
import com.apimisuse.rule.node.API;
import com.apimisuse.rule.node.ChangeTags;
import com.apimisuse.rule.node.NodeType;
import com.apimisuse.rule.node.utils.NodeUtils;
import com.apimisuse.rule.node.utils.NodeUtils.TypeCmp;
import com.apimisuse.utils.Reflection;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.utils.Pair;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtArrayReadImpl;
import spoon.support.reflect.code.CtArrayWriteImpl;
import spoon.support.reflect.code.CtAssignmentImpl;
import spoon.support.reflect.code.CtBinaryOperatorImpl;
import spoon.support.reflect.code.CtConstructorCallImpl;
import spoon.support.reflect.code.CtFieldReadImpl;
import spoon.support.reflect.code.CtFieldWriteImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtLocalVariableImpl;
import spoon.support.reflect.code.CtTryImpl;
import spoon.support.reflect.code.CtVariableReadImpl;
import spoon.support.reflect.code.CtVariableWriteImpl;
import spoon.support.reflect.declaration.CtElementImpl;
import spoon.support.reflect.declaration.CtMethodImpl;

public class WildPatternsMatcher {
	PrintStream printer = System.out;
	List<RawChangeRule> updates;
	List<RawChangeRule> deletes;
	List<RawChangeRule> inserts;

	public void match(List<Operation> ops, Diff result) {
		updates = matchUpdate(ops, result);
		deletes = matchDelete(ops, result);
		inserts = matchInsert(ops, result);
	}

	public void clear() {
		updates.clear();
		deletes.clear();
		inserts.clear();
	}

	public List<RawChangeRule> getUpdates() {
		return this.updates;
	}

	public List<RawChangeRule> getInserts() {
		return inserts;
	}

	public List<RawChangeRule> getDeletes() {
		return deletes;
	}

	public List<RawChangeRule> matchUpdate(List<Operation> ops, Diff result) {
		Iterator<Operation> it = ops.iterator();
		List<RawChangeRule> matched = new ArrayList<RawChangeRule>();
		Set<CtElement> avoidDuplicate = new HashSet<>();
		while (it.hasNext()) {
			Operation op = it.next();
			if (NodeUtils.getAction(op).equals(OperationKind.Update.name())) {
				CtElement src = op.getSrcNode();
				CtElement tgt = op.getDstNode();
				if (avoidDuplicate.contains(src)) {
					it.remove();
					continue;
				}
				String nodeType = NodeUtils.getNodeType(op, result);
				RawChangeRule rule = new RawChangeRule(op, op, result);
				if (nodeType.equals(NodeType.Invocation)) {
					processMethodReplace(rule, src, tgt, NodeType.Method);
					it.remove();
				} else if (nodeType.equals(NodeType.ConstructorCall)) {
					processMethodReplace(rule, src, tgt, NodeType.Constructor);
					it.remove();
				} else if (nodeType.equals(NodeType.BinaryOperator)) {
					if (isConditionalOp(NodeUtils.getLabel(op))) {
						rule.setSrcDst(NodeType.ConditionalOp, NodeType.ConditionalOp);
						it.remove();
					} else if (isArithmeticOp(NodeUtils.getLabel(op))) {
						rule.setSrcDst(NodeType.ArithmeticOp, NodeType.ArithmeticOp);
						it.remove();
					}
				} else if (nodeType.equals(NodeType.UnaryOperator)) {
					if (isIncrement(NodeUtils.getLabel(op))) {
						rule.setSrcDst(NodeType.IncrementOp, NodeType.IncrementOp);
						it.remove();
					} else if (isNegPos(NodeUtils.getLabel(op))) {
						rule.setSrcDst(NodeType.NegPosOp, NodeType.NegPosOp);
						it.remove();
					} else if (isNot(NodeUtils.getLabel(op))) {
						rule.setSrcDst(NodeType.NotOp, NodeType.NotOp);
						it.remove();
					}
				} else {
					boolean nonApi = false;
					if (src.getParent() instanceof CtInvocationImpl && tgt.getParent() instanceof CtInvocationImpl) {
						CtInvocationImpl parent = (CtInvocationImpl) src.getParent();
						if (!avoidDuplicate.contains(parent)) {
							Boolean parseTypeFailed = processMethodReplace(rule, src.getParent(), tgt.getParent(),
									NodeType.Method);
							if (parseTypeFailed == null) {
								nonApi = true;
							} else {
								avoidDuplicate.add(parent);
								if (parent.getTarget() != null) {
									avoidDuplicate.add(parent.getTarget());
								}
								avoidDuplicate.addAll(parent.getArguments());
								Pair<String, Boolean> content = NodeUtils.getUpdateContent(src.getParent(), tgt.getParent());
								rule.setContent(content.getFirst());
								rule.setParseTypeFailed(content.getSecond());
								rule.setParseTypeFailed(parseTypeFailed.booleanValue());
							}
						}
					} else if (src.getParent() instanceof CtConstructorCallImpl
							&& tgt.getParent() instanceof CtConstructorCallImpl) {
						CtConstructorCallImpl parent = (CtConstructorCallImpl) src.getParent();
						if (!avoidDuplicate.contains(parent)) {
							Boolean parseTypeFailed = processMethodReplace(rule, src.getParent(), tgt.getParent(),
									NodeType.Method);
							if (parseTypeFailed == null) {
								nonApi = true;
							} else {
								avoidDuplicate.add(parent);
								avoidDuplicate.addAll(parent.getArguments());
								Pair<String, Boolean> content = NodeUtils.getUpdateContent(src.getParent(), tgt.getParent());
								rule.setContent(content.getFirst());
								rule.setParseTypeFailed(content.getSecond());
								rule.setParseTypeFailed(parseTypeFailed.booleanValue());
							}
						}
					}
					if (nonApi) { // exclude MethodImpl
						rule.setSrcDst(nodeType, nodeType);
					}
					it.remove();
				}
				matched.add(rule);
			}
		}
		return matched;
	}

	private Boolean processMethodReplace(RawChangeRule rule, CtElement src, CtElement tgt, String type) {
		NodeUtils.TypeCmp receiver = TypeCmp.SAME;
		boolean sameName = true;
		NodeUtils.TypeCmp arg = TypeCmp.SAME;
		if (src instanceof CtInvocationImpl && tgt instanceof CtInvocationImpl) {
			CtInvocationImpl srcInv = (CtInvocationImpl) src;
			CtInvocationImpl tarInv = (CtInvocationImpl) tgt;
			receiver = NodeUtils.sameType(srcInv.getTarget(), tarInv.getTarget());
			sameName = NodeUtils.getMethodName(src).equals(NodeUtils.getMethodName(tgt));
			arg = NodeUtils.sameType(srcInv.getArguments(), tarInv.getArguments());
		} else if (src instanceof CtConstructorCallImpl && tgt instanceof CtConstructorCallImpl) {
			CtConstructorCallImpl srcInv = (CtConstructorCallImpl) src;
			CtConstructorCallImpl tarInv = (CtConstructorCallImpl) tgt;
			receiver = NodeUtils.sameType(srcInv.getTarget(), tarInv.getTarget());
			sameName = NodeUtils.getMethodName(src).equals(NodeUtils.getMethodName(tgt));
			arg = NodeUtils.sameType(srcInv.getArguments(), tarInv.getArguments());
		}

		rule.setSrcDst(type, type);
		rule.setParseTypeFailed(arg == TypeCmp.FAIL);

		Boolean result = false;
		Set<ChangeTags> categories = new HashSet<>();
		if (receiver == TypeCmp.NOT) {
			categories.add(ChangeTags.INV_RECEIVER);
		} else {
			if (!sameName && arg != TypeCmp.SAME) {
				categories.add(ChangeTags.INV_CHG_METHOD);
			} else if (sameName && arg == TypeCmp.NOT) {
				categories.add(ChangeTags.INV_CHG_ARG);
			} else if (!sameName) {
				categories.add(ChangeTags.INV_CHG_NAME);
			} else if (sameName && receiver == TypeCmp.SAME && arg == TypeCmp.SAME) {
				return null;
			} else {
				result = true;
			}
		}
		rule.setCategory(categories);
		return result;
	}

	public List<RawChangeRule> matchDelete(List<Operation> ops, Diff result) {
		Iterator<Operation> it = ops.iterator();
		List<RawChangeRule> matched = new ArrayList<RawChangeRule>();
		while (it.hasNext()) {
			Operation op = it.next();
			if (NodeUtils.getAction(op).equals(OperationKind.Delete.name())) {
				CtElement src = op.getSrcNode();
				String nodeType = NodeUtils.getNodeType(op, result);
				RawChangeRule rule = new RawChangeRule(op, null, result);
				setRules(it, op, nodeType, rule, true);
				matched.add(rule);
			}
		}
		return matched;
	}

	public void setRules(Iterator<Operation> it, Operation op, String nodeType, RawChangeRule rule, boolean setSrc) {
		if (nodeType.equals(NodeType.Invocation)) {
			rule.setSrcOrDst(NodeType.Method, setSrc);
		} else if (nodeType.equals(NodeType.ConstructorCall)) {
			rule.setSrcOrDst(NodeType.Constructor, setSrc);
		} else if (nodeType.equals(NodeType.BinaryOperator)) {
			if (isConditionalOp(NodeUtils.getLabel(op))) {
				rule.setSrcOrDst(NodeType.ConditionalOp, setSrc);
			} else if (isArithmeticOp(NodeUtils.getLabel(op))) {
				rule.setSrcOrDst(NodeType.ArithmeticOp, setSrc);
			} else {
				rule.setSrcOrDst(nodeType, setSrc);
			}
		} else if (nodeType.equals(NodeType.UnaryOperator)) {
			if (isIncrement(NodeUtils.getLabel(op))) {
				rule.setSrcOrDst(NodeType.IncrementOp, setSrc);
			} else if (isNegPos(NodeUtils.getLabel(op))) {
				rule.setSrcOrDst(NodeType.NegPosOp, setSrc);
			} else if (isNot(NodeUtils.getLabel(op))) {
				rule.setSrcOrDst(NodeType.NotOp, setSrc);
			} else {
				rule.setSrcOrDst(nodeType, setSrc);
			}
		} else if (nodeType.equals(NodeType.If)) {
			CtIf ctIf = (CtIf) op.getSrcNode();
			processIfRelatedRule(rule, ctIf);
			rule.setSrcOrDst(NodeType.If, setSrc);
		} else if (nodeType.equals(NodeType.TRY)) {
			CtTryImpl ctTryImpl = ((CtTryImpl) op.getSrcNode());
			List<CtInvocationImpl> invs = NodeUtils.getMethodInv(ctTryImpl.getBody());
			Set<ChangeTags> catgories = new HashSet<>();
			if (!invs.isEmpty()) {
				catgories.add(ChangeTags.TRY);
				Set<API> apis = new HashSet<>();
				for(CtInvocationImpl invocationImpl : invs) {
					apis.add(NodeUtils.parseAPI(invocationImpl));
				}
				rule.setAPIsInBody(apis);
			}
			if (ctTryImpl.getCatchers().size() > 0) {
				if (!invs.isEmpty()) {
					catgories.add(ChangeTags.CATCH);
				} else {
					boolean containInvs = ctTryImpl.getCatchers().stream()
							.anyMatch(cc -> NodeUtils.getMethodInv(cc.getBody()).size() > 0);
					if (containInvs) {
						catgories.add(ChangeTags.CATCH);
					}
				}
			}
			rule.setCategory(catgories);
			rule.setSrcOrDst(NodeType.TRY, setSrc);
		} else if (nodeType.equals(NodeType.CATCH)) {
			CtCatch ctCatch = (CtCatch) op.getSrcNode();
			CtTryImpl parent = (CtTryImpl) ctCatch.getParent();
			Set<ChangeTags> categories = new HashSet<>();
			List<CtInvocationImpl> invs = NodeUtils.getMethodInv(parent.getBody());
			if (NodeUtils.getMethodInv(ctCatch.getBody()).size() > 0
					|| invs.size() > 0) {
				categories.add(ChangeTags.CATCH);
				Set<API> apis = new HashSet<>();
				for(CtInvocationImpl invocationImpl : invs) {
					apis.add(NodeUtils.parseAPI(invocationImpl));
				}
				rule.setAPIsInBody(apis);
			}
			
			rule.setCategory(categories);
			rule.setSrcOrDst(NodeType.CATCH, setSrc);
		} else if (nodeType.equals(NodeType.SYNC)) {
			CtSynchronized sync = (CtSynchronized) op.getSrcNode();
			Map<String, Set<API>> varUsedInApi = getUsedVars(sync.getBlock(), true);
			Set<Set<API>> intersection = getUsedVars(sync.getExpression(), false).keySet()
					.stream().filter(s -> varUsedInApi.get(s) != null)
					.map(s -> varUsedInApi.get(s))
					.collect(Collectors.toSet());
			Set<API> apis = new HashSet<>();
			for(Set<API> set : intersection) {
				apis.addAll(set);
			}
			Set<ChangeTags> categories = new HashSet<>();
			if (!apis.isEmpty()) {
				categories.add(ChangeTags.SYNC);
			}
			rule.setAPIsInBody(apis);
			rule.setCategory(categories);
			rule.setSrcOrDst(NodeType.SYNC, setSrc);
		} else {
			rule.setSrcOrDst(nodeType, setSrc);
		}
		it.remove();
	}

	/**
	 * Must be invoked after matchDelete
	 * 
	 * @param ops
	 * @param result
	 * @return
	 */
	public List<RawChangeRule> matchInsert(List<Operation> ops, Diff result) {
		Iterator<Operation> it = ops.iterator();
		List<RawChangeRule> matched = new ArrayList<RawChangeRule>();
		Map<CtMethodImpl, Map<Integer, Set<Var>>> method2line2callRetValuesMap = new HashMap<>();
		while (it.hasNext()) {
			Operation op = it.next();
			if (NodeUtils.getAction(op).equals(OperationKind.Insert.name())) {
				String nodeType = NodeUtils.getNodeType(op, result);
				RawChangeRule rule = findMatchingRule(op, result, deletes);
				boolean isUpdate = !(rule.srcOp == null);
				setRules(it, op, nodeType, rule, false);
				if (isUpdate)
					updates.add(rule);
				else
					matched.add(rule);
			}
		}
		return matched;
	}

	public CtMethodImpl getMethodDecl(CtElement element) {
		while (element != null) {
			if (element instanceof CtMethodImpl) {
				return (CtMethodImpl) element;
			}
			element = element.getParent();
		}
		return null;
	}

	public RawChangeRule findMatchingRule(Operation op, Diff result, List<RawChangeRule> dels) {
		Iterator<RawChangeRule> it = dels.iterator();
		while (it.hasNext()) {
			RawChangeRule del = it.next();
			if (del.sameParent(op)) {
				del.dstOp = op;
				it.remove();
				return del;
			}
		}
		return new RawChangeRule(null, op, result);
	}

	public static void debug(List<Operation> ops, Diff result) {
		// System.out.println("=========Debug===========");
		for (Operation op : ops) {
			if (op.getAction().getClass().getSimpleName().equals(OperationKind.Move.name()))
				continue;
			System.out.println(op);
			System.out.println(NodeUtils.getLabel(op));
			System.out.println(NodeUtils.getNodeType(op, result));
			// for (ITree child : op.getAction().getNode().getChildren()) {
			// System.out.println("Child: " + child.getLabel());
			// }
			MappingStore mapping = Reflection.getMapping(result);
			System.out.println(NodeUtils.getParent(op));
			System.out.println(NodeUtils.getNodeType(NodeUtils.getParent(op), result));
			System.out
					.println(mapping.hasSrc(op.getAction().getNode()) + " " + mapping.hasDst(op.getAction().getNode()));
			System.out.println(mapping.hasSrc(NodeUtils.getParent(op)) + " " + mapping.hasDst(NodeUtils.getParent(op)));
			// System.out.println(mapping.getSrc(getParent(op)).hashCode());
			if (op.getAction().getClass().getSimpleName().equals(OperationKind.Delete.name()))
				System.out.println(">>Del: " + (NodeUtils.getParent(op)).hashCode());
			else if (op.getAction().getClass().getSimpleName().equals(OperationKind.Insert.name()))
				System.out.println(">>Ins: " + mapping.getSrc(NodeUtils.getParent(op)).hashCode());
		}
	}

	public static boolean isIncrement(String label) {
		return label.equals("POSTDEC") || label.equals("POSTINC") || label.equals("PREDEC") || label.equals("PREINC");
	}

	public static boolean isNegPos(String label) {
		return label.equals("NEG") || label.equals("POS");
	}

	public static boolean isNot(String label) {
		return label.equals("NOT");
	}

	public static boolean isLocalVariableAccess(String label) {
		return label.equals(NodeType.VariableRead) || label.equals(NodeType.VariableWrite)
				|| label.equals(NodeType.VariableAccess);
	}

	public static boolean isFieldAccess(String label) {
		return label.equals(NodeType.FieldRead) || label.equals(NodeType.FieldWrite)
				|| label.equals(NodeType.FieldAccess);
	}

	public static boolean isConditionalOp(String label) {
		return label.equals(BinaryOperatorKind.LE.name()) || label.equals(BinaryOperatorKind.LT.name())
				|| label.equals(BinaryOperatorKind.GE.name()) || label.equals(BinaryOperatorKind.GT.name())
				|| label.equals(BinaryOperatorKind.EQ.name()) || label.equals(BinaryOperatorKind.NE.name());
	}

	public static boolean isArithmeticOp(String label) {
		return label.equals(BinaryOperatorKind.PLUS.name()) || label.equals(BinaryOperatorKind.MINUS.name())
				|| label.equals(BinaryOperatorKind.MUL.name()) || label.equals(BinaryOperatorKind.DIV.name())
				|| label.equals(BinaryOperatorKind.MOD.name()) || label.equals(BinaryOperatorKind.BITOR.name())
				|| label.equals(BinaryOperatorKind.BITAND.name()) || label.equals(BinaryOperatorKind.BITXOR.name())
				|| label.equals(BinaryOperatorKind.SL.name()) || label.equals(BinaryOperatorKind.SR.name())
				|| label.equals(BinaryOperatorKind.USR.name());
	}

	static class Var {
		private String name;
		private int line;
		private boolean fromApi;
		private API api;

		public Var(String name, int line, boolean fromApi) {
			this(name, line, fromApi, null);
		}

		public Var(String name, int line, boolean fromApi, API api) {
			this.name = name;
			this.line = line;
			this.fromApi = fromApi;
			this.api = api;
		}

		public Var self() {
			return this;
		}
		
		public void setAPI(API api) {
			this.api = api;
		}

		public API getRelatedApis() {
			return this.api;
		}

		public String getName() {
			return name;
		}

		public int getLine() {
			return this.line;
		}

		public boolean isFromApi() {
			return fromApi;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Var)) {
				return false;
			}
			Var var = (Var) obj;
			return name.equals(var.name);
		}

		@Override
		public String toString() {
			return name + ":" + line + ":" + fromApi;
		}
	}

	private void processIfRelatedRule(RawChangeRule rule, CtIf ctIf) {
		CtMethodImpl<?> methodImpl = getMethodDecl(ctIf);
		Set<ChangeTags> categories = new HashSet<>();
		if (methodImpl != null) {
			Map<String, Set<API>> varUsedInBody = getUsedVars(ctIf.getThenStatement(), true);
			if (ctIf.getElseStatement() != null) {
				for(Entry<String, Set<API>> entry : getUsedVars(ctIf.getElseStatement(), true).entrySet()) {
					Set<API> set = varUsedInBody.get(entry.getKey());
					if (set == null) {
						set = entry.getValue();
					} else {
						set.addAll(entry.getValue());
					}
					varUsedInBody.put(entry.getKey(), set);
				}
			}
			Map<String, API> retVars = getApiReturnVars(methodImpl, ctIf.getPosition().getLine());

			boolean nullCheck = getNullCompExpr(ctIf.getCondition()).stream()
					.anyMatch(ele -> retVars.containsKey(ele) || varUsedInBody.containsKey(ele));
			
			Map<String, Set<API>> varReadInCnd = getUsedVars(ctIf.getCondition(), false);

			Set<API> retCheckAPIs = varReadInCnd.keySet().stream()
					.filter(ele -> retVars.get(ele) != null)
					.map(ele -> retVars.get(ele))
					.collect(Collectors.toSet());
			
			Set<Set<API>> depChecks = varReadInCnd.keySet().stream()
					.filter(ele -> varUsedInBody.get(ele) != null)
					.map(ele -> varUsedInBody.get(ele))
					.collect(Collectors.toSet());
			Set<API> depCheckAPIs = new HashSet<>();
			for(Set<API> item : depChecks) {
				depCheckAPIs.addAll(item);
			}

			if (nullCheck) {
				categories.add(ChangeTags.IF_NULL);
			}
			if (retCheckAPIs != null && retCheckAPIs.size() > 0) {
				categories.add(ChangeTags.IF_RET);
			}
			if (depCheckAPIs.size() > 0) {
				categories.add(ChangeTags.IF_DEPEND);
			}
			if (categories.isEmpty()) {
				categories.add(ChangeTags.IF_OTHER);
			}
			rule.setAPIsInBody(depCheckAPIs);
			rule.setCategory(categories);
			rule.setRetAPI(retCheckAPIs);
		}
	}

	public Map<String, API> getApiReturnVars(CtElementImpl element, int line) {
		List<Var> line2RetVars = new LinkedList<>();
		List<CtLocalVariableImpl> localVariableImpls = element.getElements(new TypeFilter<>(CtLocalVariableImpl.class));
		for (CtLocalVariableImpl localVariable : localVariableImpls) {
			if (localVariable.getPosition().getLine() < line) {
				if (localVariable.getDefaultExpression() != null
						&& localVariable.getDefaultExpression() instanceof CtInvocationImpl) {
					API api = NodeUtils.parseAPI((CtInvocationImpl) localVariable.getDefaultExpression());
					String type = localVariable.getType() == null ? null : localVariable.getType().toString();
					api.setRetValue(type, localVariable.getSimpleName());
					Var var = new Var(localVariable.getSimpleName(), localVariable.getPosition().getLine(), true);
					var.setAPI(api);
					line2RetVars.add(var);
				} else {
					line2RetVars.add(new Var(localVariable.getSimpleName(), localVariable.getPosition().getLine(), false));
				}
			}
		}
		List<CtAssignmentImpl> assignmentImpls = element.getElements(new TypeFilter<>(CtAssignmentImpl.class));
		for (CtAssignmentImpl assignement : assignmentImpls) {
			if (assignement.getPosition().getLine() < line) {
				if (assignement.getAssignment() instanceof CtInvocationImpl) {
					line2RetVars.add(
							new Var(assignement.getAssigned().toString(), assignement.getPosition().getLine(), true));
				} else {
					line2RetVars.add(
							new Var(assignement.getAssigned().toString(), assignement.getPosition().getLine(), false));
				}
			}
		}

		List<Var> list = new ArrayList<>(line2RetVars);
		Collections.sort(list, new Comparator<Var>() {
			@Override
			public int compare(Var o1, Var o2) {
				return o2.getLine() - o1.getLine();
			}
		});

		Set<Var> retValues = new HashSet<>();
		Set<Var> set = null;
		Var var = null;
		for (int i = 0; i < list.size(); i++) {
			var = list.get(i);
			if (!retValues.contains(var)) {
				retValues.add(var);
			}
		}
		retValues = retValues.stream().filter(ele -> ele.isFromApi()).collect(Collectors.toSet());
		Map<String, API> map = new HashMap<>();
		for(Var v : retValues) {
			map.put(v.getName(), v.getRelatedApis());
		}
		return map;
	}

	private Set<String> getNullCompExpr(CtElement element) {
		Set<String> expression = new HashSet<>();
		List<CtBinaryOperatorImpl> binaryOperatorImpls = element
				.getElements(new TypeFilter<>(CtBinaryOperatorImpl.class));
		for (CtBinaryOperatorImpl operator : binaryOperatorImpls) {
			if (operator.getKind() == BinaryOperatorKind.EQ || operator.getKind() == BinaryOperatorKind.NE) {
				if ("null".equals(operator.getLeftHandOperand().toString())) {
					expression.add(operator.getRightHandOperand().toString());
				} else if ("null".equals(operator.getRightHandOperand().toString())) {
					expression.add(operator.getLeftHandOperand().toString());
				}
			}
		}
		return expression;
	}

	private Map<String, Set<API>> getUsedVars(CtElement element, boolean inApi) {
		Map<String, Integer> assigned = new HashMap<>();

		Integer line = null;
		String varName = null;
		for (CtFieldWriteImpl ctFieldWriteImpl : element.getElements(new TypeFilter<>(CtFieldWriteImpl.class))) {
			varName = ctFieldWriteImpl.toString();
			line = assigned.get(varName);
			if (line == null || line > ctFieldWriteImpl.getPosition().getLine()) {
				line = ctFieldWriteImpl.getPosition().getLine();
			}
			assigned.put(varName, line);
		}
		for (CtVariableWriteImpl ctVariableWriteImpl : element
				.getElements(new TypeFilter<>(CtVariableWriteImpl.class))) {
			varName = ctVariableWriteImpl.toString();
			line = assigned.get(varName);
			if (line == null || line > ctVariableWriteImpl.getPosition().getLine()) {
				line = ctVariableWriteImpl.getPosition().getLine();
			}
			assigned.put(varName, line);
		}
		for (CtArrayWriteImpl ctArrayWriteImpl : element.getElements(new TypeFilter<>(CtArrayWriteImpl.class))) {
			varName = ctArrayWriteImpl.toString();
			line = assigned.get(varName);
			if (line == null || line > ctArrayWriteImpl.getPosition().getLine()) {
				line = ctArrayWriteImpl.getPosition().getLine();
			}
			assigned.put(varName, line);
		}

		List<CtElement> elements = new LinkedList<>();
		if (inApi) {
			elements.addAll(element.getElements(new TypeFilter<>(CtInvocationImpl.class)));
		} else {
			elements.add(element);
		}
		Map<String, Set<API>> varsUsed = new HashMap<>();
		for (CtElement ele : elements) {
			API api = null;
			if (ele instanceof CtInvocation) {
				api = NodeUtils.parseAPI((CtInvocation) ele);
			}
			for (CtFieldReadImpl ctFieldReadImpl : ele.getElements(new TypeFilter<>(CtFieldReadImpl.class))) {
				varName = ctFieldReadImpl.toString();
				line = assigned.get(varName);
				int curline = ctFieldReadImpl.getPosition().getLine();
				if (line == null || line >= curline) {
					Set<API> set = varsUsed.get(varName);
					if (set == null) {
						set = new HashSet<>();
						varsUsed.put(varName, set);
					}
					if (api != null) {
						set.add(api);
					}
				}
			}
			for (CtVariableReadImpl ctVariableReadImpl : ele.getElements(new TypeFilter<>(CtVariableReadImpl.class))) {
				varName = ctVariableReadImpl.toString();
				line = assigned.get(varName);
				if (line == null || line >= ctVariableReadImpl.getPosition().getLine()) {
					Set<API> set = varsUsed.get(varName);
					if (set == null) {
						set = new HashSet<>();
						varsUsed.put(varName, set);
					}
					if (api != null) {
						set.add(api);
					}
				}
			}
			for (CtArrayReadImpl ctArrayReadImpl : ele.getElements(new TypeFilter<>(CtArrayReadImpl.class))) {
				varName = ctArrayReadImpl.toString();
				line = assigned.get(varName);
				if (line == null || line >= ctArrayReadImpl.getPosition().getLine()) {
					Set<API> set = varsUsed.get(varName);
					if (set == null) {
						set = new HashSet<>();
						varsUsed.put(varName, set);
					}
					if (api != null) {
						set.add(api);
					}
				}
			}
		}
		return varsUsed;
	}

}
