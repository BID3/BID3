package com.apimisuse.rule;

import java.util.HashSet;
import java.util.Set;

import com.apimisuse.rule.node.API;
import com.apimisuse.rule.node.ChangeTags;
import com.apimisuse.rule.node.NodeType;
import com.apimisuse.rule.node.utils.NodeUtils;
import com.apimisuse.utils.Configure;
import com.apimisuse.utils.Reflection;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.utils.Pair;

import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;

public class RawChangeRule
{
	
	/*   NOTE : one rule may consist of two gumtree operations:
	 *   e.g., one operation delete while the other may insert
	 */
	// store the operation that related to current rule
	public Operation srcOp;
	// store the other operation that related to the current rule
	public Operation dstOp;
	
	// store the diff result from gumtree
	public Diff result;

	// key of source node type if the rule is delete or update
	// if the rule is insert, this field is null
	public String src;
	// key of the target node type
	public String dst;
	
	public String content;
	
	private boolean parseTypeFail = false; 
	
	// add if may belongs more than one category
	private Set<ChangeTags> category = new HashSet<>();
	
	private Set<API> retAPIs = new HashSet<>();
	
	private Set<API> apiInBody = new HashSet<>();

	
	public RawChangeRule(Operation srcOp, Operation dstOp, Diff result) {
		this.srcOp = srcOp;
		this.dstOp = dstOp;
		this.result = result;
		this.src = NodeType.UNKNOWN;
		this.dst = NodeType.UNKNOWN;
	}

	public RawChangeRule(Operation srcOp, Operation dstOp, Diff result,
			String src, String dst) {
		this.srcOp = srcOp;
		this.dstOp = dstOp;
		this.result = result;
		this.src = src;
		this.dst = dst;
	}
	
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getDst() {
		return dst;
	}

	public void setDst(String dst) {
		this.dst = dst;
	}
	
	public void setSrcOrDst(String content, boolean isSrc) {
		if (isSrc) {
			this.src = content;
		} else {
			this.dst = content;
		}
	}
	
	public void setSrcDst(String src, String dst) {
		this.src = src;
		this.dst = dst;
	}
	
	public Operation getSrcOp() {
		return this.srcOp;
	}
	
	public Operation getDstOp() {
		return this.dstOp;
	}

	public void setCategory(Set<ChangeTags> catgs) {
		category = catgs;
	}
	
	public void setRetAPI(Set<API> apis) {
		this.retAPIs = apis;
	}
	
	public void setAPIsInBody(Set<API> apis) {
		this.apiInBody = apis;
	}
	
	public boolean isDelete() {
		return dstOp == null;
	}

	public boolean isInsert() {
		return srcOp == null;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setParseTypeFailed(boolean failed) {
		this.parseTypeFail = failed;
	}
	
	public OperationKind getChangeType() {
		if (isDelete())
			return OperationKind.Delete;
		else if (isInsert())
			return OperationKind.Insert;
		else
			return OperationKind.Update;
	}
	
	
	public boolean sameParent(Operation op) {
		MappingStore mapping = Reflection.getMapping(result);
		if (NodeUtils.getParent(this.srcOp) == null
				|| NodeUtils.getParent(op) == null)
			return false;
		if (op.getAction().getClass().getSimpleName()
				.equals(OperationKind.Delete.name())) {
			return mapping.getSrc(NodeUtils.getParent(this.srcOp))
					.equals(NodeUtils.getParent(op));
		} else if (op.getAction().getClass().getSimpleName()
				.equals(OperationKind.Insert.name())) {
			return NodeUtils.getParent(this.srcOp)
					.equals(mapping.getSrc(NodeUtils.getParent(op)));
		}
		return false;
	}

	public String getContext() {
		if (srcOp != null) {
			return NodeUtils.getNodeType(NodeUtils.getParent(srcOp), result);
		} else {
			return NodeUtils.getNodeType(NodeUtils.getParent(dstOp), result);
		}
	}

	public ChangeRule toChangeRule(String fileName,String[] archivInfor) {
		if (this.content == null) {
			Pair<String, Boolean> pair = NodeUtils.getContent(this);
			this.content =  pair.getFirst();
			this.parseTypeFail = pair.getSecond();
		}
		ChangeRule rule = new ChangeRule(getChangeType(), toString(), category, this.parseTypeFail,
				this.content, fileName, archivInfor);
		rule.setRetApis(retAPIs);
		rule.setBodyUseAPI(apiInBody);
		return rule;
	}

	public String toString() {
		// return getContext() + CONTEXT + src + TO + dst;
		return src + Configure.TAG_TO + dst;
	}
}
