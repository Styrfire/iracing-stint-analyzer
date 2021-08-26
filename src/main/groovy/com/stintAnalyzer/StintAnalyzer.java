package com.stintAnalyzer;

import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;
import com.stintAnalyzer.dto.stint.Stint;
import com.stintAnalyzer.processor.StintProcessor;
import com.stintAnalyzer.service.GoogleSheetsService;
import com.stintAnalyzer.ui.Console;
import org.springframework.beans.factory.annotation.Value;

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

		// initialize file watchers
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

		// initialize Google spreadsheet service
		GoogleSheetsService googleSheetsService;
		try
		{
			googleSheetsService = new GoogleSheetsService();
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong initializing the Google spreadsheet service!");
			e.printStackTrace();
			return;
		}

		Console console = new Console();
		console.showConsole();

		// get starting session and live data
		StintProcessor stintProcessor = new StintProcessor();
		Session currSession = parseSessionFile(sessionStrFile);
		LiveData currLiveData = parseLiveDataFile(liveStrFile);

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
				{
					// check if stint is initialized. if not, initialize it
					// throwing it in this loop because session data could cause some values to be null
					// thus, failing the initialization, and then we'll have to try again
					if (!stintProcessor.getStintInitialized())
					{
						System.out.println("Attempting stint initialization");
						stintProcessor.initializeStint(currSession, currLiveData);
					}
					else if (stintProcessor.progressStint(currSession, currLiveData))
					{
						System.out.println("Yay, a stint completed! Sending data to Google Spreadsheet!");
						//update google with stintProcessor.getStint();
						//googleSheetsService.sendStintDataToSpreadsheet(stintProcessor.getStint());
					}
				}
				lastSec = sec;
			}
		}
	}
}
