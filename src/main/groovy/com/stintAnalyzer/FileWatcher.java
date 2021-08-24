package com.stintAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;

public class FileWatcher
{
	private long timeStamp;
	private final File file;

	public FileWatcher(File file) throws FileNotFoundException
	{
		this.file = file;
		if (file.lastModified() != 0L)
			this.timeStamp = file.lastModified();
		else
		{
			System.out.println("File could not be opened! Be prepared for fireworks!");
			throw new FileNotFoundException("file.lastModified() timestamp returned 0L");
		}
	}

	public boolean fileChanged()
	{
		long timeStamp = file.lastModified();

		if (this.timeStamp != timeStamp)
		{
			this.timeStamp = timeStamp;
			return true;
		}
		else
			return false;
	}
}
