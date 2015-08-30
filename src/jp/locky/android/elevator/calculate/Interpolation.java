package jp.locky.android.elevator.calculate;

import java.util.ArrayList;

public class Interpolation {

	ArrayList<Double> idataList = new ArrayList<Double>();
	ArrayList<Double> iTimeList = new ArrayList<Double>();
	//this algorithm change low sampling frequency to 100Hz sampling frequency
	//--------0----------1-----------2---------3---------
	//this interpolation use simple linear interpolation
	
	public void linearInterpolation(double[] dataArray, double[] timeArray) {
		for (int i = 1; i < dataArray.length; i++) {
			double pTime = timeArray[i-1] * 100;// previous time x100
			double nTime = timeArray[i] * 100;// next time
			double pData = dataArray[i-1];
			double nData = dataArray[i];

			int pTimeInt = (int) pTime;
			int nTimeInt = (int) nTime;
			if (nTimeInt - pTimeInt > 0) {
				int tmp = nTimeInt - pTimeInt;
				for (int j = 0; j < tmp; j++) {
					double k = (nData - pData) / (nTime - pTime);
					double iTime = nTimeInt + j;// interpolated time
					double iData = k * (iTime - pTime) + pData;
					idataList.add(iData);
					iTimeList.add(iTime/100);
				}
			} else {
				System.out.println("calculate error");
			}
		}
		System.out.println("interpolation over");					
	}
}
