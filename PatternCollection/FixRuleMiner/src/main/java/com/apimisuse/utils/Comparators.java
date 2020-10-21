package com.apimisuse.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.apimisuse.rule.ChangeRule;

public class Comparators
{

	public static <K, V extends Comparable<? super V>> Comparator getNumberComparator() {
		return new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		};
	}

	public static Comparator getListComparator() {
		return new Comparator<Entry<String, List<ChangeRule>>>() {
			@Override
			public int compare(Entry<String, List<ChangeRule>> e1,
					Entry<String, List<ChangeRule>> e2) {
				Integer size2 = e2.getValue().size();
				return size2.compareTo(e1.getValue().size());
			}
		};
	}
}
