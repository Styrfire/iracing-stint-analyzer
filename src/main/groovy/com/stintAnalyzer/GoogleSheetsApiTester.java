package com.stintAnalyzer;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.stintAnalyzer.service.GoogleSheetsService;

import java.io.IOException;

public class GoogleSheetsApiTester
{
	public static void main(String[] args) throws IOException
	{
		GoogleSheetsService googleSheetsService = null;
		try
		{
			googleSheetsService = new GoogleSheetsService();
		}
		catch (Exception e)
		{
			System.out.println("Something went wrong initializing the google sheets service!");
			e.printStackTrace();
			System.exit(2);
		}

		Sheets sheetsService = googleSheetsService.getSheetsService();
		Spreadsheet spreadsheet = sheetsService.spreadsheets().get("spreadsheet id").execute();
		if (spreadsheet != null)
			System.out.println(spreadsheet.getProperties().getTitle());
	}
}