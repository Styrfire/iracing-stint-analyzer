package com.stintAnalyzer.dto.session

class Tires
{
	LeftFront leftFront
	LeftRear leftRear
	RightFront rightFront
	RightRear rightRear

	// Returns true if the set of tires are equal to each other
	boolean compareTo(Tires tires)
	{
		return Objects.equals(leftFront.getColdPressure(), tires.leftFront.getColdPressure()) &&
				Objects.equals(leftFront.getLastHotPressure(), tires.leftFront.getLastHotPressure()) &&
				Objects.equals(leftFront.getLastTempsOMI(), tires.leftFront.getLastTempsOMI()) &&
				Objects.equals(leftFront.getTreadRemaining(), tires.leftFront.getTreadRemaining()) &&
				Objects.equals(rightFront.getColdPressure(), tires.rightFront.getColdPressure()) &&
				Objects.equals(rightFront.getLastHotPressure(), tires.rightFront.getLastHotPressure()) &&
				Objects.equals(rightFront.getLastTempsIMO(), tires.rightFront.getLastTempsIMO()) &&
				Objects.equals(rightFront.getTreadRemaining(), tires.rightFront.getTreadRemaining()) &&
				Objects.equals(leftRear.getColdPressure(), tires.leftRear.getColdPressure()) &&
				Objects.equals(leftRear.getLastHotPressure(), tires.leftRear.getLastHotPressure()) &&
				Objects.equals(leftRear.getLastTempsOMI(), tires.leftRear.getLastTempsOMI()) &&
				Objects.equals(leftRear.getTreadRemaining(), tires.leftRear.getTreadRemaining()) &&
				Objects.equals(rightRear.getColdPressure(), tires.rightRear.getColdPressure()) &&
				Objects.equals(rightRear.getLastHotPressure(), tires.rightRear.getLastHotPressure()) &&
				Objects.equals(rightRear.getLastTempsIMO(), tires.rightRear.getLastTempsIMO()) &&
				Objects.equals(rightRear.getTreadRemaining(), tires.rightRear.getTreadRemaining())
	}
}
