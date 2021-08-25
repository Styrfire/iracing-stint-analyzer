package com.stintAnalyzer;

import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;
import com.stintAnalyzer.dto.stint.Stint;
import com.stintAnalyzer.processor.StintProcessor;
import com.stintAnalyzer.ui.Console;
import org.springframework.beans.factory.annotation.Value;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;

import static com.stintAnalyzer.utils.Parser.parseLiveDataFile;
import static com.stintAnalyzer.utils.Parser.parseSessionFile;

public class StintAnalyzer
{
//	@Value("${sessionStrFilePath}")
//	String sesssionStrFilePath; // = "C:\\Users\\g_n_r\\source\\repos\\irsdk\\irsdk_lapTiming\\sessionStr.txt"
//	@Value("${liveStrFilePath}")
//	String liveStrFilePath; // = "C:\\Users\\g_n_r\\source\\repos\\irsdk\\irsdk_lapTiming\\sessionStr.txt"

	public static void main(String[] args)
	{
		File sessionStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk\\irsdk_lapTiming\\sessionStr.txt");
//		File sessionStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk\\irsdk_lapTiming\\sessionStr Example.txt");
		File liveStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk\\irsdk_lapTiming\\liveStr.txt");
//		File liveStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk\\irsdk_lapTiming\\liveStr Example.txt");
		FileWatcher sessionStrFileWatcher;
		FileWatcher liveStrFileWatcher;

		try
		{
			sessionStrFileWatcher = new FileWatcher(sessionStrFile);
			liveStrFileWatcher = new FileWatcher(liveStrFile);
		}
		catch (FileNotFoundException exception)
		{
			System.out.println(sessionStrFile.getPath() + " could not be found! Noping out of the program!");
			return;
		}

		Console console = new Console();
		console.showConsole();

		// get starting session and live data
		Session currSession = parseSessionFile(sessionStrFile);
		LiveData currLiveData = parseLiveDataFile(liveStrFile);
		if (currSession == null || currLiveData == null)
		{
			System.out.println("Something went wrong parsing the files. Exiting");
			System.exit(2);
		}

		// initialize the stint
		StintProcessor stintProcessor = new StintProcessor();
		stintProcessor.initializeStint(currSession, currLiveData);

		// check for file updates and process the changes
		long lastSec = 0;
		while (true)
		{
			long sec = System.currentTimeMillis() / 5000;
			if (sec != lastSec) {
				boolean fileChanged = false;
				if (liveStrFileWatcher.fileChanged())
				{
					currLiveData = parseLiveDataFile(liveStrFile);
					fileChanged = true;
				}

				if (sessionStrFileWatcher.fileChanged())
				{
					currSession = parseSessionFile(sessionStrFile);
					fileChanged = true;
				}

				if (currSession == null || currLiveData == null)
				{
					System.out.println("Something went wrong parsing the files. Exiting");
					System.exit(2);
				}

				if (fileChanged)
					if (stintProcessor.progressStint(currSession, currLiveData))
					{
						//update google with stintProcessor.getStint();
						Stint stint = stintProcessor.getStint();
						System.out.println("Yay, a stint completed!");
					}

				lastSec = sec;
			}
		}
	}
}
