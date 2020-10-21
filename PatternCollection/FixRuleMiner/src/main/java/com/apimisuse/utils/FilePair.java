package com.apimisuse.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class FilePair
{
	private File oldFile;
	private File newFile;

	public FilePair(File oldFile, File newFile) {
		this.oldFile = oldFile;
		this.newFile = newFile;
	}
	
	public File getOldFile() {
		return oldFile;
	}
	
	public File getNewFile() {
		return newFile;
	}
	
	@Override
	public boolean equals(Object o) {
	    if (o == null) {
	        return false;
	    }
	    if (o == this) {
	        return true;
	    }
	    final FilePair other = (FilePair) o;
	    if (other.oldFile.equals(this.oldFile) && other.newFile.equals(this.newFile)) {
	        return true;
	    }
	    try {
	        final List<String> oldContents = Files.readAllLines(this.oldFile.toPath());
	        final List<String> newContents = Files.readAllLines(this.newFile.toPath());
	        if (!Files.readAllLines(other.oldFile.toPath()).equals(oldContents)) {
	            return false;
	        }
	        if (!Files.readAllLines(other.newFile.toPath()).equals(newContents)) {
                return false;
            }
	    } catch (Exception e) {
	        return false;
	    }
	    return false;
	}
	
	@Override
    public int hashCode() {
	    try {
            final List<String> oldContents = Files.readAllLines(this.oldFile.toPath());
            return oldContents.hashCode();
	    } catch (Exception e) {
	        
	    }
	    return System.identityHashCode(this);
	}
}
