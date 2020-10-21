package com.apimisuse.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jgrapht.alg.util.Pair;

import com.apimisuse.rule.ChangeRule;
import com.apimisuse.rule.RawChangeRule;
import com.apimisuse.rule.matcher.WildPatternsMatcher;
import com.apimisuse.rule.node.utils.NodeUtils;
import com.apimisuse.utils.Comparators;
import com.apimisuse.utils.FilePair;
import com.apimisuse.utils.FileTraverse;
import com.apimisuse.utils.Utils;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;

public class Main
{

	public static void main(String[] args) throws IOException {
		
		String pairFileDir = args[0];
		String writeRulePath = args[1];
		final int nThreads = Integer.parseInt(args[2]);
		List<FilePair> list = FileTraverse.getAllFiles(pairFileDir);
		list = list.stream().distinct().collect(Collectors.toList()); // remove duplicates
		long start = System.currentTimeMillis();
		System.out.println("Number of bug fixes: " + list.size());
		int toFilter = 0;

		List<ChangeRule> allRules = new ArrayList<ChangeRule>();
		int count = 0;
		Set<String> realFixCommitsSize = new HashSet<>();

		ExecutorService pool = Executors.newFixedThreadPool(nThreads);
		int currentThread = 0;
		
		List<Pair<File, Future<List<ChangeRule>>>> threads = new LinkedList<>();
		
		for (FilePair pair : list) {
			//System.out.println(pair.newFile.getAbsolutePath());
			File oldFile = pair.getOldFile();
			File newFile = pair.getNewFile();
			System.out.println(oldFile.getAbsolutePath());
			realFixCommitsSize.add(oldFile.getAbsolutePath().split("/buggy-version")[0]);
			if (currentThread >= nThreads) {
				for(Pair<File, Future<List<ChangeRule>>> p : threads) {
					try {
						List<ChangeRule> changeRules = p.getSecond().get(10, TimeUnit.MINUTES);
						if (changeRules.size() == 0) {
							toFilter ++;
						} else {
							allRules.addAll(changeRules);
						}
					} catch (Exception e) {
						p.getSecond().cancel(true);
						Utils.outFile("error.txt", "ERROR : " + p.getFirst().getAbsolutePath(), true);
					}
				}
				threads.clear();
				currentThread = 0;
			}
			
			threads.add(new Pair<File, Future<List<ChangeRule>>>(oldFile, pool.submit(new OperationExtraction(oldFile, newFile))))	;
			currentThread ++;
			count++;
		}
		
		for(Pair<File, Future<List<ChangeRule>>> p : threads) {
			try {
				List<ChangeRule> changeRules = p.getSecond().get(10, TimeUnit.MINUTES);
				if (changeRules.size() == 0) {
					toFilter ++;
				} else {
					allRules.addAll(changeRules);
				}
			} catch (Exception e) {
				p.getSecond().cancel(true);
				Utils.outFile("error.txt", "ERROR : " + p.getFirst().getAbsolutePath(), true);
			}
		}
		pool.shutdown();
		
		System.out.println("realFixCommitsSize " + realFixCommitsSize.size());
		System.out.println(realFixCommitsSize.toString());
		String writeToPath = writeRulePath + File.separator + "ExampleResult.json";
		Utils.StoretoFile(allRules,writeToPath);
		
		printRules(allRules);
		long end = System.currentTimeMillis();
		System.out.println("Number of fake bug fixes: " + toFilter);
		System.out.println("Time cost: " + (end - start) / 1000 + "s");
	}

	/*public static void addAllRules(List<ChangeRule> list,
			List<RawChangeRule> rules) {
		for (RawChangeRule rule : rules) {
			list.add(rule.toChangeRule());
		}
	}*/




	public static void printRules(List<ChangeRule> rules) {
		PrintStream printer = System.out;
		printer.println("==============" + rules.size() + "=============");
		// print type statistics
		rules.stream()
				.collect(Collectors.groupingBy(ChangeRule::getType,
						Collectors.counting()))
				.forEach((type, count) -> printer.println(type + " " + count));
		OperationKind[] kinds = { OperationKind.Update, OperationKind.Delete,
				OperationKind.Insert };
		for (OperationKind kind : kinds) {
			printer.println(">>>>" + kind);
			printRuleContentFreq(rules, kind, printer);
		}
	}

	private static void printRulePatternFreq(List<ChangeRule> rules,
			OperationKind kind, PrintStream printer) {
		Map<String, Long> pattern_freq = rules.stream()
				.filter(rule -> rule.getType().equals(kind))
				.collect(Collectors.groupingBy(ChangeRule::getPattern,
						Collectors.counting()));
		Comparator<Entry<String, Long>> comp = Comparators
				.getNumberComparator();
		pattern_freq.entrySet().stream().sorted(comp)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue,
						(e1, e2) -> e1 + e2, LinkedHashMap::new))
				.forEach((rule, count) -> printer.println(rule + " " + count));
	}

	private static void printRuleContentFreq(List<ChangeRule> rules,
			OperationKind kind, PrintStream printer) {
		Map<String, List<ChangeRule>> pattern_rules = rules.stream()
				.filter(rule -> rule.getType().equals(kind))
				.collect(Collectors.groupingBy(ChangeRule::getPattern));
		Comparator<Entry<String, List<ChangeRule>>> comp = Comparators
				.getListComparator();
		pattern_rules.entrySet().stream().sorted(comp)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue,
						(e1, e2) -> e1, LinkedHashMap::new))
				.forEach((rule, list) -> {
					if(Utils.ifPrint(rule)) {
						printer.println(">>" + rule + " " + list.size());
						Map<String, Long> rule_freq = list.stream()
								.collect(Collectors.groupingBy(
										ChangeRule::getContent,
										Collectors.counting()));
						Comparator<Entry<String, Long>> numComp = Comparators
								.getNumberComparator();
						rule_freq.entrySet().stream().sorted(numComp)
								.collect(Collectors.toMap(Entry::getKey,
										Entry::getValue, (e1, e2) -> e1 + e2,
										LinkedHashMap::new))
								.forEach((content, count) -> printer
										.println(content + " " + count));
					}
				});
	}

	public static void printRuleContent(List<RawChangeRule> rules) {
		PrintStream printer = System.out;
		printer.println("==============" + rules.size() + "=============");
		rules.stream().forEach(
				rule -> printer.println(rule + " " + NodeUtils.getContent(rule)));
	}
}


class OperationExtraction implements Callable<List<ChangeRule>> {
	
	private File oldFile;
	private File newFile;
	
	public OperationExtraction(File oldFile, File newFile) {
		this.oldFile = oldFile;
		this.newFile = newFile;
	}
	
	@Override
	public List<ChangeRule> call() throws Exception {
		List<ChangeRule> allRules = new LinkedList<>();
		//System.out.println(pair.newFile.getAbsolutePath());
		System.out.println(oldFile.getAbsolutePath());
		try {
			final Diff result = new AstComparator().compare(oldFile, newFile);
			List<Operation> ops = result.getRootOperations();
			if (ops.size() != 0) {
				//System.out.println(
				//		(count++) + ": " + pair.oldFile.getCanonicalPath());
				String fileName = oldFile.getAbsolutePath();
				String archiveInforFile = fileName.split("/buggy-version")[0] + "/archiveInfo.txt";
				String[] projectUrl = read(archiveInforFile);
				List<Operation> actions = new ArrayList<Operation>();
				actions.addAll(ops);
				WildPatternsMatcher matcher = new WildPatternsMatcher();
				matcher.match(actions, result);
				allRules.addAll(matcher.getUpdates().stream()
						.map(rule -> rule.toChangeRule(fileName,projectUrl))
						.collect(Collectors.toList()));
				allRules.addAll(matcher.getDeletes().stream()         
						.map(rule -> rule.toChangeRule(fileName,projectUrl))
						.collect(Collectors.toList()));
				allRules.addAll(matcher.getInserts().stream()
						.map(rule -> rule.toChangeRule(fileName,projectUrl))
						.collect(Collectors.toList()));
				matcher.clear();
				//if (count >= 2000) break;
			}
		} catch (Exception e) {
		}
		return allRules;
	}
	
	private String[] read(String archiveInfor) throws IOException {
		String[] infors = new String[3];
		BufferedReader br = new BufferedReader(new FileReader(archiveInfor));
		String line = br.readLine();
		while(line!=null) {
			if (line.contains("Project url:"))
				infors[0] = line.split("Project url:")[1].trim();
			if (line.contains("Fixed-version:"))
				infors[1] = line.split("Fixed-version:")[1].trim();
			if (line.contains("Date:"))
				infors[2] = line.split("Date:")[1].trim();
			line = br.readLine();
		}
		br.close();
		return infors;
	}
	
}
