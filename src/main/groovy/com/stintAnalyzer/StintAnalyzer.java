package com.stintAnalyzer;

import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;
import com.stintAnalyzer.processor.StintProcessor;
import com.stintAnalyzer.service.GoogleSheetsService;
import com.stintAnalyzer.ui.Console;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;

import static com.stintAnalyzer.utils.Parser.parseLiveDataFile;
import static com.stintAnalyzer.utils.Parser.parseSessionFile;

@Service
public class StintAnalyzer
{
	@Value("${sessionStrFilePath}")
	String sessionStrFilePath;
	@Value("${liveStrFilePath}")
	String liveStrFilePath;
	@Value("${spreadsheetId}")
	String spreadsheetId;

	StintProcessor stintProcessor;

	@Inject
	public StintAnalyzer(StintProcessor stintProcessor)
	{
		this.stintProcessor = stintProcessor;
	}

	public void start()
	{
		System.out.println("sessionStr File Path: " + sessionStrFilePath);
		System.out.println("liveStr File Path: " + liveStrFilePath);
		File sessionStrFile = new File(sessionStrFilePath);
		File liveStrFile = new File(liveStrFilePath);

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
		System.out.println("Getting starting session and live data");
		Session currSession = parseSessionFile(sessionStrFile);
		LiveData currLiveData = parseLiveDataFile(liveStrFile);

		// check for file updates and process the changes
		System.out.println("Starting check for file changes and progress stint loop");
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
						// send stint data to google spreadsheet
						try
						{
							googleSheetsService.sendStintDataToSpreadsheet(stintProcessor.getStint(), spreadsheetId);
							System.out.println("Setting stint to not initialized after stint completion!");
							stintProcessor.setStintInitialized(false);

						}
						catch (Exception e)
						{
							e.printStackTrace();
							System.out.println("There was an issue sending the stint data to the google spreadsheet! :O");
							stintProcessor.setStintInitialized(false);
						}
					}
				}
				lastSec = sec;
			}
		}
	}
}
