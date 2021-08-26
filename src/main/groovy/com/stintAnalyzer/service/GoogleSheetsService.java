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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

	public Sheets getSheetsService()
	{
		return sheetsService;
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

		System.out.println("Found existing track sheet!");
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

	private void updateTrackSheetLapTimes(Stint stint, Spreadsheet spreadsheet, Sheet trackSheet)
	{
	}
}
