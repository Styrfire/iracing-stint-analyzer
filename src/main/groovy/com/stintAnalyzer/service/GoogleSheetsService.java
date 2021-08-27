package com.stintAnalyzer.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.stintAnalyzer.dto.stint.Stint;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleSheetsService
{
	Sheets sheetsService;
	JsonFactory jsonFactory;

	public GoogleSheetsService() throws IOException, GeneralSecurityException
	{
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		jsonFactory = GsonFactory.getDefaultInstance();

		sheetsService = new Sheets.Builder(httpTransport, jsonFactory, getCredentials(httpTransport))
				.setApplicationName("iRacing Stint Analyzer")
				.build();
	}

	private Credential getCredentials(NetHttpTransport httpTransport) throws IOException
	{
		// global instance of the scopes required by this quickstart
		// if modifying these scopes, delete your previously saved tokens/ folder.
		List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);

		// load client secrets
		String credentialsFilePath = "/client_secret.json";
		InputStream in = GoogleSheetsService.class.getResourceAsStream(credentialsFilePath);
		if (in == null)
		{
			throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
		}
		GoogleClientSecrets  clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

		// build flow and trigger user authorization request
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, clientSecrets, scopes)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public void sendStintDataToSpreadsheet(Stint stint, String spreadsheetId) throws Exception
	{
		Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
		List<Sheet> sheets = spreadsheet.getSheets();

		Sheet trackSheet = findSheetByTitle(sheets, stint.getTrackName());
		if (trackSheet != null)
			System.out.println("Found existing track sheet!");
		else
		{
			System.out.println("Didn't find an existing track sheet. Creating one now.");
			trackSheet = createNewTrackSheet(spreadsheet, stint.getTrackName());
		}

		updateTrackSheetLapTimes(stint, spreadsheet, trackSheet);
	}

	private Sheet createNewTrackSheet(Spreadsheet spreadsheet, String sheetTitle) throws Exception
	{
		List<Sheet> sheets = spreadsheet.getSheets();

		Sheet baseSheet = findSheetByTitle(sheets, "Base");
		if (baseSheet == null)
		{
			System.out.println("baseSheet = null!");
			throw new Exception("baseSheet = null");
		}

		System.out.println("Created new track sheet!");
		CopySheetToAnotherSpreadsheetRequest copySheetRequestBody = new CopySheetToAnotherSpreadsheetRequest();
		copySheetRequestBody.setDestinationSpreadsheetId(spreadsheet.getSpreadsheetId());
		SheetProperties sheetProperties = sheetsService.spreadsheets().sheets().copyTo(spreadsheet.getSpreadsheetId(), baseSheet.getProperties().getSheetId(), copySheetRequestBody).execute();

		// create sheet properties
		SheetProperties updateSheetProperties = new SheetProperties();
		updateSheetProperties.setSheetId(sheetProperties.getSheetId());
		updateSheetProperties.setTitle(sheetTitle);

		// create update sheet request
		UpdateSheetPropertiesRequest updateSheetRequestBody = new UpdateSheetPropertiesRequest();
		updateSheetRequestBody.setProperties(updateSheetProperties);
		updateSheetRequestBody.setFields("title"); // which field(s) get(s) updated

		Request updateSheetRequest = new Request();
		updateSheetRequest.setUpdateSheetProperties(updateSheetRequestBody);

		List<Request> requests = new ArrayList<>();
		requests.add(updateSheetRequest);

		BatchUpdateSpreadsheetRequest batchUpdateRequestBody = new BatchUpdateSpreadsheetRequest();
		batchUpdateRequestBody.setRequests(requests);
		batchUpdateRequestBody.setIncludeSpreadsheetInResponse(true);
		BatchUpdateSpreadsheetResponse response = sheetsService.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), batchUpdateRequestBody).execute();
		sheets = response.getUpdatedSpreadsheet().getSheets();

		Sheet newTrackSheet = findSheetByTitle(sheets, sheetTitle);
		if (newTrackSheet == null)
		{
			System.out.println("newTrackSheet = null!");
			throw new Exception("newTrackSheet = null");
		}

		return newTrackSheet;
	}

	private Sheet findSheetByTitle(List<Sheet> sheets, String sheetTitle)
	{
		for (Sheet sheet: sheets)
			if (Objects.equals(sheet.getProperties().getTitle(), sheetTitle))
				return sheet;

		return null;
	}

	// if an empty column is available, use that for the stint, else return false
	private void updateTrackSheetLapTimes(Stint stint, Spreadsheet spreadsheet, Sheet trackSheet) throws Exception
	{
		boolean foundEmptyRow = false;

		// get grid data
		Spreadsheet spreadsheetWithGrid = sheetsService.spreadsheets().get(spreadsheet.getSpreadsheetId())
				.setIncludeGridData(true).execute();
		List<GridData> gridData = spreadsheetWithGrid.getSheets().get(trackSheet.getProperties().getIndex()).getData();

		// get row B2:I2
		RowData rowB2ToI2 = gridData.get(0).getRowData().get(1);
		for (int i = 1; i < rowB2ToI2.getValues().size(); i++)
		{
			// if this column is free, update its data!
			if (rowB2ToI2.getValues().get(i).getEffectiveValue() == null)
			{
				// get current column letter
				String column = String.valueOf("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(i));

				// get cell values and fill up to 23 with empty
				List<Object> cellValues = new ArrayList<>();
				cellValues.add(stint.getSetupName());
				cellValues.addAll(stint.getStintLapTimes());
				for (int j = cellValues.size(); j < 23; j++)
					cellValues.add("");
				cellValues.add(stint.getTires().getLeftFront().getTreadRemaining());
				cellValues.add(stint.getTires().getRightFront().getTreadRemaining());
				cellValues.add(stint.getTires().getLeftRear().getTreadRemaining());
				cellValues.add(stint.getTires().getRightRear().getTreadRemaining());

				// add cell values to 2d value array
				List<List<Object>> values = new ArrayList<>();
				for (Object cellValue : cellValues)
				{
					List<Object> item = new ArrayList<>();
					item.add(cellValue);
					values.add(item);
				}

				// shove the data in the spreadsheet at the appropriate column
				ValueRange valueRange = new ValueRange();
				valueRange.setValues(values);
				Sheets.Spreadsheets.Values.Update request = sheetsService.spreadsheets().values()
						.update(spreadsheet.getSpreadsheetId(),
								"'" + trackSheet.getProperties().getTitle() + "'!" + column + "2:" + column + "28", valueRange);
				request.setValueInputOption("RAW").execute();
				System.out.println("Added sprint on column " + column + " in the " + stint.getTrackName() + " sheet!");

				foundEmptyRow = true;
				break;
			}
		}

		if (!foundEmptyRow)
		{
			System.out.println("There was not a free row to put the data! Make more space on the spreadsheet!");
			throw new Exception("There was not a free row to put the data! Make more space on the spreadsheet!");
		}
	}
}
