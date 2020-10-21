package com.apimisuse.utils;

import java.lang.reflect.Field;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.TreeContext;

import gumtree.spoon.diff.Diff;

public class Reflection
{

	public static MappingStore getMapping(Diff result) {
		try {
			Field f = result.getClass().getDeclaredField("_mappingsComp"); // NoSuchFieldException
			f.setAccessible(true);
			return (MappingStore) f.get(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
