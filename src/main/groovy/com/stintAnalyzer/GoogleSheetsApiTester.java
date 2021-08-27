package com.stintAnalyzer;

import com.stintAnalyzer.dto.session.*;
import com.stintAnalyzer.dto.stint.Stint;
import com.stintAnalyzer.service.GoogleSheetsService;

import java.util.ArrayList;
import java.util.List;

public class GoogleSheetsApiTester
{
	public static void main(String[] args) throws Exception
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

		String spreadsheetId = "";
		Stint stint = new Stint();
		stint.setTrackName("darlington");
		stint.setSetupName("darlington.sto");
		List<Float> lapTimes = new ArrayList<>(5);
		lapTimes.add(24.1f);
		lapTimes.add(23.1f);
		lapTimes.add(23.3f);
		lapTimes.add(23.5f);
		lapTimes.add(23.6f);
		stint.setStintLapTimes(lapTimes);
		Tires tires = new Tires();
		LeftFront leftFront = new LeftFront();
		leftFront.setColdPressure("152 kPa");
		leftFront.setLastHotPressure("152 kPa");
		leftFront.setLastTempsOMI("38c, 38c, 38c");
		leftFront.setTreadRemaining("100%, 100%, 100%");
		tires.setLeftFront(leftFront);
		RightFront rightFront = new RightFront();
		rightFront.setColdPressure("152 kPa");
		rightFront.setLastHotPressure("152 kPa");
		rightFront.setLastTempsOMI("38c, 38c, 38c");
		rightFront.setTreadRemaining("100%, 100%, 100%");
		tires.setRightFront(rightFront);
		LeftRear leftRear = new LeftRear();
		leftRear.setColdPressure("152 kPa");
		leftRear.setLastHotPressure("152 kPa");
		leftRear.setLastTempsOMI("38c, 38c, 38c");
		leftRear.setTreadRemaining("100%, 100%, 100%");
		tires.setLeftRear(leftRear);
		RightRear rightRear = new RightRear();
		rightRear.setColdPressure("152 kPa");
		rightRear.setLastHotPressure("152 kPa");
		rightRear.setLastTempsOMI("38c, 38c, 38c");
		rightRear.setTreadRemaining("100%, 100%, 100%");
		tires.setRightRear(rightRear);
		stint.setTires(tires);
		stint.setStintCompletedTimestamp(System.currentTimeMillis());

		googleSheetsService.sendStintDataToSpreadsheet(stint, spreadsheetId);
		System.out.println("Wow, we got here!");
	}
}