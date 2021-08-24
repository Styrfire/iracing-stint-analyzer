package com.stintAnalyzer.processor;

import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;
import com.stintAnalyzer.dto.stint.Stint;

import java.util.Objects;

public class StintProcessor
{
	int lastCompletedLap;
	Stint stint;

	public StintProcessor()
	{
		stint = new Stint();
	}

	public void initializeStint(Session session, LiveData liveData)
	{
		stint.setTrackName(session.getWeekendInfo().getTrackName());
		stint.setSetupName(session.getDriverInfo().getDriverSetupName());
		lastCompletedLap = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLapCompleted();
		stint.setLeftFrontTread(session.getCarSetup().getTires().getLeftFront().getTreadRemaining());
		stint.setRightFrontTread(session.getCarSetup().getTires().getRightFront().getTreadRemaining());
		stint.setLeftRearTread(session.getCarSetup().getTires().getLeftRear().getTreadRemaining());
		stint.setRightRearTread(session.getCarSetup().getTires().getRightRear().getTreadRemaining());
	}

	public boolean progressStint(Session session, LiveData liveData)
	{
		// if track name or setup changed, refresh the stint
		if (!Objects.equals(stint.getTrackName(), session.getWeekendInfo().getTrackName()) ||
				!Objects.equals(stint.getSetupName(), session.getDriverInfo().getDriverSetupName()))
		{
			initializeStint(session, liveData);
			return false;
		}

		int liveDataLastCompletedLap = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLapCompleted();
		float liveDataLastLapTime = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLastLapTime();

		// if a lap was completed, add lap to the stint and check to see if the stint has been completed
		if (lastCompletedLap == -1)
		{
			System.out.println("lastCompletedLap == -1");
		}
		else if (liveDataLastCompletedLap == lastCompletedLap + 1 && liveDataLastLapTime != -1)
		{
			stint.getStintLapTimes().add(liveDataLastLapTime);
			System.out.println("Added a lap to the stint!");
			// if stint has 20 completed laps
			if (stint.getStintLapTimes().size() == 20)
			{
				// check to see if the tire wear has changed (indicating the user left the car or pitted) If so,
				// update the tire treads and return true (the stint has completed)
				if (!Objects.equals(stint.getLeftFrontTread(), session.getCarSetup().getTires().getLeftFront().getTreadRemaining()) ||
						!Objects.equals(stint.getRightFrontTread(), session.getCarSetup().getTires().getRightFront().getTreadRemaining()) ||
						!Objects.equals(stint.getLeftRearTread(), session.getCarSetup().getTires().getLeftRear().getTreadRemaining()) ||
						!Objects.equals(stint.getRightRearTread(), session.getCarSetup().getTires().getRightRear().getTreadRemaining()))
				{
					stint.setLeftFrontTread(session.getCarSetup().getTires().getLeftFront().getTreadRemaining());
					stint.setRightFrontTread(session.getCarSetup().getTires().getRightFront().getTreadRemaining());
					stint.setLeftRearTread(session.getCarSetup().getTires().getLeftRear().getTreadRemaining());
					stint.setRightRearTread(session.getCarSetup().getTires().getRightRear().getTreadRemaining());
					return true;
				}
			}
		}
		else if (liveDataLastCompletedLap == lastCompletedLap + 1)
		{
			System.out.println("different lap but -1 for last lap time");
			lastCompletedLap = liveDataLastCompletedLap;
		}
		// if the lap is two or more laps ahead or one behind, reset the stint
		else if (liveDataLastCompletedLap != lastCompletedLap)
		{
			System.out.println("Lap is two or more laps ahead or one behind. Stint reset");
			initializeStint(session, liveData);
		}
		// stint process is caught up to the current lap
		else
		{
			System.out.println("liveDataLastCompletedLap == lastCompletedLap");
		}

		return false;
	}

	public Stint getStint()
	{
		return stint;
	}
}
