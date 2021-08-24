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
		Float liveDataLastLapTime = liveData.getCarStatus().getCars().get(session.getDriverInfo().getDriverCarIdx()).getCarIdxLastLapTime();

		// if a lap was completed, add lap to the stint and check to see if the stint has been completed
		if (liveDataLastCompletedLap == lastCompletedLap + 1)
		{
			stint.getStintLapTimes().add(liveDataLastLapTime);
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
		// if the lap is two or more laps ahead or one behind, reset the stint
		else if (liveDataLastCompletedLap != lastCompletedLap)
			initializeStint(session, liveData);

		return false;
	}

	public Stint getStint()
	{
		return stint;
	}
}