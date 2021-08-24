package com.stintAnalyzer.dto.stint

class Stint
{
	String trackName
	String setupName
	List<Float> stintLapTimes
	String leftFrontTread
	String rightFrontTread
	String leftRearTread
	String rightRearTread

	Stint()
	{
		trackName = null
		setupName = null
		stintLapTimes = new ArrayList<>(20)
		leftFrontTread = null
		rightFrontTread = null
		leftRearTread = null
		rightRearTread = null
	}
}
