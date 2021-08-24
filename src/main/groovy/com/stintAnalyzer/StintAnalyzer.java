package com.stintAnalyzer;

import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;
import com.stintAnalyzer.dto.stint.Stint;
import com.stintAnalyzer.processor.StintProcessor;

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
	public static void main(String[] args)
	{
//		File sessionStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk 1.15\\irsdk\\irsdk_lapTiming\\sessionStr.txt");
		File sessionStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk 1.15\\irsdk\\irsdk_lapTiming\\sessionStr Example.txt");
//		File liveStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk 1.15\\irsdk\\irsdk_lapTiming\\liveStr.txt");
		File liveStrFile = new File("C:\\Users\\g_n_r\\source\\repos\\irsdk 1.15\\irsdk\\irsdk_lapTiming\\liveStr Example.txt");
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

		// Scheduled executor in new thread implementation
//		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
//
//		ses.scheduleAtFixedRate(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if (sessionStrFileWatcher.fileChanged())
//					System.out.println("I'm running every second!");
//			}
//		}, 0, 1, TimeUnit.SECONDS);

		// Create JFrame and use it to exit the program
		JFrame frame = new JFrame("iRacing Stint Analyzer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WindowListener listener = new WindowAdapter()
		{
			public void windowClosing(WindowEvent event)
			{
				Frame frame = (Frame) event.getSource();
				System.out.println("Closing " + frame.getTitle());
				// Scheduled executor in new thread implementation
//				ses.shutdown();
				System.exit(0);
			}
		};
		Container container = frame.getContentPane();
		JTextPane pane = new JTextPane();
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attributeSet, Color.black);
		pane.setCharacterAttributes(attributeSet, true);
		pane.setText("Close this window to stop iRacing Stint Analyzer!");
		container.add(pane, BorderLayout.CENTER);
		frame.setSize(400, 80);
		frame.addWindowListener(listener);
		frame.setVisible(true);

		// Get starting session and live data
		Session currSession = parseSessionFile(sessionStrFile);
		LiveData currLiveData = parseLiveDataFile(liveStrFile);
		if (currSession == null || currLiveData == null)
		{
			System.out.println("Something went wrong parsing the files. Exiting");
			System.exit(2);
		}

		StintProcessor stintProcessor = new StintProcessor();
		stintProcessor.progressStint(currSession, currLiveData);

		// check for file updates and process the changes
		long lastSec = 0;
		while (true)
		{
			long sec = System.currentTimeMillis() / 1000;
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

				//code to run
				System.out.println("I'm running every second!");

				lastSec = sec;
			}
		}
	}
}
