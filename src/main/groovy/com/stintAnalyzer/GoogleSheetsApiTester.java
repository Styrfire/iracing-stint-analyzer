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
		leftFront.setLastTempsOMI("159c, 161c, 147c");
		leftFront.setTreadRemaining("94%, 93%, 99%");
		tires.setLeftFront(leftFront);
		RightFront rightFront = new RightFront();
		rightFront.setColdPressure("152 kPa");
		rightFront.setLastHotPressure("152 kPa");
		rightFront.setLastTempsIMO("172c, 167c, 152c");
		rightFront.setTreadRemaining("89%, 89%, 92%");
		tires.setRightFront(rightFront);
		LeftRear leftRear = new LeftRear();
		leftRear.setColdPressure("152 kPa");
		leftRear.setLastHotPressure("152 kPa");
		leftRear.setLastTempsOMI("156c, 158c, 143c");
		leftRear.setTreadRemaining("96%, 95%, 100%");
		tires.setLeftRear(leftRear);
		RightRear rightRear = new RightRear();
		rightRear.setColdPressure("152 kPa");
		rightRear.setLastHotPressure("152 kPa");
		rightRear.setLastTempsIMO("176c, 168c, 153c");
		rightRear.setTreadRemaining("88%, 89%, 92%");
		tires.setRightRear(rightRear);
		stint.setTires(tires);
		stint.setStintCompletedTimestamp(System.currentTimeMillis());

		googleSheetsService.sendStintDataToSpreadsheet(stint, spreadsheetId);
		System.out.println("Wow, we got here!");
	}
}