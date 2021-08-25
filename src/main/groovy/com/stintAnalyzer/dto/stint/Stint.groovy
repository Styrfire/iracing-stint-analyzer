package com.stintAnalyzer.dto.stint

import com.stintAnalyzer.dto.session.Tires

class Stint
{
	String trackName
	String setupName
	List<Float> stintLapTimes
	Tires tires

	Stint()
	{
		trackName = null
		setupName = null
		stintLapTimes = new ArrayList<>(20)
		tires = null
	}
}
