package com.stintAnalyzer.dto.stint

import com.stintAnalyzer.dto.session.Tires

class Stint
{
	String trackName
	String setupName
	List<Float> stintLapTimes
	Tires tires
	Long stintCompletedTimestamp

	Stint()
	{
		trackName = null
		setupName = null
		stintLapTimes = new ArrayList<>(20)
		tires = null
		stintCompletedTimestamp = null
	}

	String toString()
	{
		String stint = ""

		// add track and setup names
		stint = stint + "Track name: " + this.trackName +
				"\nSetup Name: " + this.setupName + "\n"

		// add lap times
		for (int i = 0; i < this.stintLapTimes.size(); i++) {
			stint = stint + "\nLap " + i+1 + ": " + this.stintLapTimes.get(i)
		}

		// add tire temps
		stint = stint + "\n\nLeft Front Temps OMI: " + this.tires.leftFront.lastTempsOMI +
				"\nRight Front Temps IMO: " + this.tires.rightFront.lastTempsIMO +
				"\nLeft Rear Temps OMI: " + this.tires.leftRear.lastTempsOMI +
				"\nRight Rear Temps IMO: " + this.tires.rightRear.lastTempsIMO

		// add tire tread
		stint = stint + "\n\nLeft Front Tread OMI: " + this.tires.leftFront.treadRemaining +
				"\nRight Front Tread IMO: " + this.tires.rightFront.treadRemaining +
				"\nLeft Rear Tread OMI: " + this.tires.leftRear.treadRemaining +
				"\nRight Rear Tread IMO: " + this.tires.rightRear.treadRemaining

		return stint;
	}
}
