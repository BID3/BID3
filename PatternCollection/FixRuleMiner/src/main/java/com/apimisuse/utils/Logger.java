package com.apimisuse.utils;

public class Logger
{
	static final int DEBUG = 1;
	static final int INFO = 2;
	static final int WARN = 3;
	static final int FATAL = 4;
	static int level = DEBUG;

	public static void debug(String info) {
		if (level <= DEBUG)
			System.out.println("DEBUG: " + info);
	}

	public static void info(String info) {
		if (level <= INFO)
			System.out.println("INFO: " + info);
	}

	public static void warn(String info) {
		if (level <= WARN)
			System.out.println("WARN: " + info);
	}

	public static void fatal(String info) {
		if (level <= FATAL)
			System.out.println("FATAL: " + info);
	}

}
