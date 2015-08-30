package jp.locky.android.elevator.calculate;

import java.util.ArrayList;

public class MinusGravityVar {
		
	ArrayList<Double> gravityDataList = new ArrayList<Double>();
	ArrayList<Long> gravityTimeList = new ArrayList<Long>();
	private int varianceSize_ = CalculatorActivity.varianceSize;
	private double varTheshold_= CalculatorActivity.varTheshold;
	public double gravityMain(double accNorm[],int gravityWindowSize,int fileLength){
		System.out.println("--gravity main starts--");
		//double accurateGravity = getGravityThreshold(accNorm, gravityWindowSize, fileLength); 
		
		//if(accurateGravity!=-100){
			//return accurateGravity;
		//}
		//else{//if 
			System.out.println("--gravity variance starts--");
			//run variance
			//time to mean window 100
			//window lines 			
			double[] arrayVarianced =new double[fileLength-varianceSize_] ;//Gravity		
			
			for(int k=0;k<fileLength-varianceSize_;k++){
				arrayVarianced[k] = variance(varianceSize_, accNorm, k);//run variance
			}
			//window filter
			ArrayList<Double> gravityList =gravityListFinder(arrayVarianced,accNorm, gravityWindowSize, fileLength-varianceSize_,varTheshold_);
			double bestGravity =0;
			if(gravityList.size()!=0){					
				bestGravity = getBestGravityFromLIst(gravityList);
			}else{//重力を未検出の場合、エラーが出るから、解決方法：未検出のとき、重力を0に変わる						
			}
			System.out.println("best gravity:"+bestGravity);
			return bestGravity;						
		//}		
	}
	
	/**
	 * input data array, output gravity array index list, 
	 * 重力成分の可能性が高い成分を探す
	 * 可能性があるセグメントの平均を取る
	 * 各重力セグメントの平均値を計算
	 * 平均値の分散を計算し、分散値の小さいものを重力成分として扱う
	 * これで、エレベータ一定のときの成分を削除できる
	 * */
	private ArrayList<Double> gravityListFinder(double varianced[],double accNormDouble[],int gravityWindowSize,int fileLength,double varThreshold){
		int countWindowSize = 0;
		int countNumberOfWindow =0;
		//ArrayList<Integer> indexList = new ArrayList<Integer>();
		ArrayList<Double> gravityList = new ArrayList<Double>();
		for(int i=0;i<fileLength-gravityWindowSize;i++){//run all data
			for(int k=0;k<gravityWindowSize;k++){
				if(varianced[i+k]<varThreshold){//threshold ,filter varianced array 
					countWindowSize++;
				}
			}
			if(countWindowSize == gravityWindowSize){//till get 100 window size
				double sumOfSegment = 0;
				for(int l=0;l<gravityWindowSize;l++){					
					//indexList.add(i+l);//all gravity index within this window will be returned
					sumOfSegment = sumOfSegment+accNormDouble[i+l];
				}
				double averageOfSegment = sumOfSegment/gravityWindowSize;
				gravityList.add(averageOfSegment);
				i=i+gravityWindowSize-1;
				countNumberOfWindow++;					
			}
			countWindowSize = 0;
		}
		
		System.out.println("Recalc GravityWindow,"+countNumberOfWindow+",");
		countNumberOfWindow =0;
		return gravityList;	
	}
	
	//重力セグメントの中に、一番平均な重力を探す
	private double getBestGravityFromLIst(ArrayList<Double> gravityList){
		int size = gravityList.size();
		double sum = 0;
		for(int i= 0;i<size;i++){
			sum = sum + gravityList.get(i);
		}
		double average = sum/size;
		ArrayList<Double> varianceList = new ArrayList<Double>();
		for(int j = 0;j<size;j++){
			double varianceTmp = (gravityList.get(j)-average)*(gravityList.get(j)-average)/size;
			varianceList.add(varianceTmp);
		}
		//find the smallest variance
		double smallest =varianceList.get(0);
		double current =0;
		int rememberTheOrder = 0;
		for(int k = 1;k<size;k++){
			current = varianceList.get(k);
			if(current<smallest){
				smallest = current;
				rememberTheOrder = k;
			}
		}
		return gravityList.get(rememberTheOrder);
	}
	
	/**gravity calculator*/
/*	private double getGravityThreshold(double accNorm[],int gravityWindowSize,int fileLength){
		System.out.println("--get gravity threshold starts--");
		double totalGravity = 0;
		double accurateGravity = 0;
		int countWindowSize = 0;
		int countNumberOfWindow =0;
		ArrayList<Double> getGravityList = new ArrayList<Double>();
		
		for(int i=0;i<fileLength-gravityWindowSize;i++){//run all data
			for(int k=0;k<gravityWindowSize;k++){
				if(accNorm[i+k]>0.98&&accNorm[i+k]<1.01){//another threshold
					countWindowSize++;
				}
			}
			if(countWindowSize == gravityWindowSize){
				for(int k=0;k<gravityWindowSize;k++){
					getGravityList.add(accNorm[i+k]);
				}
				i=i+gravityWindowSize-1;
				countNumberOfWindow++;					
			}
			countWindowSize = 0;
		}
		if(countNumberOfWindow<1){
			return -100;
		}
		
		int listSize = getGravityList.size();
		for(int j=0;j<listSize;j++){
			totalGravity = totalGravity +getGravityList.get(j);
		}
		accurateGravity = totalGravity/listSize;
		System.out.println("GravityWindow:,"+countNumberOfWindow+",");
		countNumberOfWindow =0;
		return accurateGravity;		
	}
	*/
	/**variance calculator*/
	private double variance(int varianceSize,double[] data,int position){
		
		double sum = 0;
		for(int j = 0; j<varianceSize; j++){
			sum += data[position+j];
		}
		double mean = sum/(double)varianceSize;
		sum = 0;
		for(int j = 0; j<varianceSize; j++){
			sum += Math.pow(data[position+j], 2.0);
		}
		double sqmean = sum/(double)varianceSize;
		double newValue=sqmean-Math.pow(mean, 2.0);
		return newValue;
	}
	/**input data array, output gravity array index list* */
}
