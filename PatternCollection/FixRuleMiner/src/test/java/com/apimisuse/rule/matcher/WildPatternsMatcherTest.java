package com.apimisuse.rule.matcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.apimisuse.rule.ChangeRule;
import com.apimisuse.rule.matcher.WildPatternsMatcher;
import com.apimisuse.utils.Utils;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;

public class WildPatternsMatcherTest
{

//	@Test
//	public void test() throws Exception{
//		File fl = new File("src/test/resources/utd/examples/Base.java");
//
//		List<ChangeRule> allRules = new ArrayList<ChangeRule>();
//		String[] mutators = {
//				/*
//				 * "MethodName", "Conditional", "Arithmetic", "LocalVariable",
//				 * "ArgumentList", "FieldAccess", "InvertNeg", "MethodCall",
//				 * "Constructor", "Increment", "Constants", "Return",
//				 * "MemberVariable", "Switch", "ArgPropagation", "Guard",
//				 */ "LocalToMeth"};
//		for (String mutator : mutators) {
//			System.out.println(">>>>>>" + mutator);
//			File fr = new File("src/test/resources/utd/examples/Base-" + mutator
//					+ ".java");
//			AstComparator diff = new AstComparator();
//			Diff result = diff.compare(fl, fr);
//			List<Operation> ops = result.getRootOperations();
//			List<Operation> actions = new ArrayList<Operation>();
//			actions.addAll(ops);
//			WildPatternsMatcher matcher = new WildPatternsMatcher();
//			matcher.match(actions, result);
//		/*	
//			allRules.addAll(matcher.updates.stream()
//					.map(rule -> rule.toChangeRule())
//					.collect(Collectors.toList()));
//			allRules.addAll(matcher.deletes.stream()
//					.map(rule -> rule.toChangeRule())
//					.collect(Collectors.toList()));
//			allRules.addAll(matcher.inserts.stream()
//					.map(rule -> rule.toChangeRule())
//					.collect(Collectors.toList()));*/
//			matcher.clear();
//		}
//		Main.printRules(allRules);
//	}
	
	@Test
	public void test_insert_ifcondition() throws Exception {
		File srcFile = new File("src/test/resources/utd/Ori.java");
		File tarFile = new File("src/test/resources/utd/Tar.java");
		compare(srcFile, tarFile);
	}
	
	@Test
	public void test_delete_ifcondition() throws Exception {
		File srcFile = new File("src/test/resources/utd/Tar.java");
		File tarFile = new File("src/test/resources/utd/Ori.java");
		compare(srcFile, tarFile);
	}
	
	
	@Test
	public void test_method_nonrelated() throws Exception {
		String src = "public class Example {" + 
					"   public void method(Object o1, String o2) {" + 
					"	    o1.f(o2, 2);" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void method(Object o1, String o2) {" + 
					"	    o1.f(o2, 6);" + 
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_method_receiver() throws Exception {
		String src = "public class Example {" + 
					"   public void method(Object o1, String o2) {" + 
					"	    o1.f(o2);" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void method(Object o1, String o2) {" + 
					"	    o2.f(o1);" + 
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_method_name() throws Exception {
		String src = "public class Example {" + 
					"   public void method(Object o1, Object o2) {" + 
					"	    o1.f(o2);" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void method(Object o1, Object o2) {" + 
					"	    o1.m(o2);" + 
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_method_arg() throws Exception {
		String src = "public class Example {" + 
					"   public void method(Object o1, Object o2) {" + 
					"	    o1.f(o2, o1);" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void method(Object o1, Object o2) {" + 
					"	    o1.f(o2);" + 
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_method_name_and_arg() throws Exception {
		String src = "public class Example {" + 
					"   public void method(Object o1, Object o2) {" + 
					"	    o1.m(o2, o1);" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void method(Object o1, Object o2) {" + 
					"	    o1.f(o2);" + 
					"   }" +
					"}";
		compare(src, tar);
	}
	
	
	@Test
	public void test_addTry() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"	    m.method();" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try{" +
					"	    		m.method();" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_addTryAndCatch() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"	    m.method();" + 
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try{" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_addCatch() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_addCatchMultiMethod() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try(Object m) {" + 
					"		try {" +
					"	    		m.method();" +
					"	    		m.method2();" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try(Object m) {" + 
					"		try {" +
					"	    		m.method();" +
					"	    		m.method2();" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_addTryCatch() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					
					"	    		m.method();" +
					
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_addFinally() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		} finally {" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_addSync() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"	    	m.method();" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		synchronized(m) {" +
					"	    		m.method();" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_delTry() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"		try{" +
					"	    		m.method();" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"	    		m.method();" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_delCatch() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_delFinally() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"	    		m.method();" +
					"		} catch (Exception e) {" +
					"		} finally {" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"		try {" +
					"		} catch (Exception e) {" +
					"		}" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	@Test
	public void test_delSync() throws Exception {
		String src = "public class Example {" + 
					"   public void add_try() {" + 
					"		synchronized(m) {" +
					"	    		m.method();" +
					"		}" +
					"   }" +
					"}";
		String tar = "public class Example {" + 
					"   public void add_try() {" + 
					"	    	m.method();" +
					"   }" +
					"}";
		compare(src, tar);
	}
	
	protected void compare(File srcFile, File tarFile) throws Exception {
		final Diff result = new AstComparator().compare(srcFile, tarFile);
		List<ChangeRule> allRules = buildChangeRules(result, srcFile.getAbsolutePath());
		for(ChangeRule changeRule : allRules) {
			System.out.println(changeRule);
		}
	}
	
	protected void compare(String src, String tar) {
		String fileName = "Example.java";
		final Diff result = new AstComparator().compare(src, tar);
		List<ChangeRule> allRules = buildChangeRules(result, fileName);
		for(ChangeRule changeRule : allRules) {
			System.out.println(changeRule);
		}
	}
	
	protected List<ChangeRule> buildChangeRules(Diff result, String fileName) {
		List<ChangeRule> allRules = new ArrayList<ChangeRule>();
		List<Operation> ops = result.getRootOperations();
		String[] projInfo = {"A","B","C"};
		if (ops.size() == 0) {
		} else {
			List<Operation> actions = new ArrayList<Operation>();
			actions.addAll(ops);
			WildPatternsMatcher matcher = new WildPatternsMatcher();
			matcher.match(actions, result);
			allRules.addAll(matcher.getUpdates().stream()
					.map(rule -> rule.toChangeRule(fileName, projInfo))
					.collect(Collectors.toList()));
			allRules.addAll(matcher.getDeletes().stream()           //delete already include move
					.map(rule -> rule.toChangeRule(fileName, projInfo))
					.collect(Collectors.toList()));
			allRules.addAll(matcher.getInserts().stream()
					.map(rule -> rule.toChangeRule(fileName, projInfo))
					.collect(Collectors.toList()));
			matcher.clear();
		}
		try {
			Utils.StoretoFile(allRules,"test.json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allRules;
	}
	
	
}
