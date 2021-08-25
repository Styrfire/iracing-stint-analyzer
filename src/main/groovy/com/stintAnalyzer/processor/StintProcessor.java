package com.stintAnalyzer.processor;

import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;
import com.stintAnalyzer.dto.stint.Stint;

import java.util.ArrayList;
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
		lastCompletedLap = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLapCompleted();
		stint.setTrackName(session.getWeekendInfo().getTrackName());
		stint.setSetupName(session.getDriverInfo().getDriverSetupName());
		stint.setStintLapTimes(new ArrayList<>(20));
		stint.setTires(session.getCarSetup().getTires());
	}

	public boolean progressStint(Session session, LiveData liveData)
	{
		int stintSize = 5;

		// if track name or setup changed, refresh the stint
		if (!Objects.equals(stint.getTrackName(), session.getWeekendInfo().getTrackName()) ||
				!Objects.equals(stint.getSetupName(), session.getDriverInfo().getDriverSetupName()))
		{
			System.out.println("Track name or setup changed. Stint reset.");
			initializeStint(session, liveData);
			return false;
		}

		int liveDataLastCompletedLap = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLapCompleted();
		float liveDataLastLapTime = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLastLapTime();

		// new session has been started and a lap hasn't been completed yet
		if (lastCompletedLap == -1)
		{
			lastCompletedLap = liveDataLastCompletedLap;
			System.out.println("lastCompletedLap == -1");
			// if stint has 20 completed laps
			if (stint.getStintLapTimes().size() == stintSize)
			{
				// check to see if the tire wear has changed (indicating the user left the car or pitted) If so,
				// update the tire treads and return true (the stint has completed)
				if (!Objects.equals(stint.getTires(), session.getCarSetup().getTires()))
				{
					stint.setTires(session.getCarSetup().getTires());
					return true;
				}
			}
		}
		// completed a successful lap
		else if (liveDataLastCompletedLap == lastCompletedLap + 1 && liveDataLastLapTime != -1)
		{
			lastCompletedLap = liveDataLastCompletedLap;
			stint.getStintLapTimes().add(liveDataLastLapTime);
			System.out.println("Added a lap to the stint!");
		}
		// started a new lap but the car was off the track last lap
		else if (liveDataLastCompletedLap == lastCompletedLap + 1)
		{
			lastCompletedLap = liveDataLastCompletedLap;
			System.out.println("different lap but -1 for last lap time");
			System.out.println("Exited the car last lap. Stint reset.");
			initializeStint(session, liveData);
		}
		// user is out of the car
		else if (liveDataLastCompletedLap == -1)
		{
			System.out.println("liveDataLastCompletedLap == -1");
			// if stint has 20 completed laps
			if (stint.getStintLapTimes().size() == stintSize)
			{
				// check to see if the tire wear has changed (indicating the user left the car or pitted) If so,
				// update the tire treads and return true (the stint has completed)
				if (!Objects.equals(stint.getTires(), session.getCarSetup().getTires()))
				{
					stint.setTires(session.getCarSetup().getTires());
					return true;
				}
			}
		}
		// if the lap is two or more laps ahead or one behind, reset the stint
		else if (liveDataLastCompletedLap != lastCompletedLap)
		{
			System.out.println("liveDataLastCompletedLap = " + liveDataLastCompletedLap + "\nlastCompletedLap = " + lastCompletedLap);
			System.out.println("Live lap is two or more laps ahead or one behind. Stint reset");
			initializeStint(session, liveData);
		}
		// stint process is caught up to the current lap
		else
		{
			System.out.println("liveDataLastCompletedLap == lastCompletedLap");
			// if stint has 20 completed laps
			if (stint.getStintLapTimes().size() == stintSize)
			{
				// check to see if the tire wear has changed (indicating the user left the car or pitted) If so,
				// update the tire treads and return true (the stint has completed)
				if (!Objects.equals(stint.getTires(), session.getCarSetup().getTires()))
				{
					stint.setTires(session.getCarSetup().getTires());
					return true;
				}
			}

		}

		return false;
	}

	public Stint getStint()
	{
		return stint;
	}
}
