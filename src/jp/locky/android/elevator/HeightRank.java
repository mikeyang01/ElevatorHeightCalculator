package jp.locky.android.elevator;

import java.util.Locale;

public class HeightRank {
	public String heightRank(int distance) {
		int restDistance = 0;
		String language = Locale.getDefault().getLanguage();
		System.out.println("language:" + language);
		if (language.equals("ja")) {
			if (distance < 147) {
				restDistance = 147 - distance;
				System.out.println("rest distance:" + restDistance);
				// return restDistance+"メートル"+"\n"+"ピラミッドより高い";
				return "ピラミッドの高さまで\n残り： " + restDistance + "メートル";
			} else if (distance < 333) {
				restDistance = 333 - distance;
				System.out.println("rest distance:" + restDistance);
				// return restDistance+"メートル"+"\n"+"東京タワーより高い";
				return "東京タワーまで\n残り: " + restDistance + "メートル";
			} else if (distance < 634) {
				restDistance = 634 - distance;
				// return restDistance+"メートル"+"\n"+"東京スカイツリーより高い";
				return "東京スカイツリーまで\n残り: " + restDistance + "メートル";
			} else if (distance < 828) {
				restDistance = 828 - distance;
				// return restDistance+"メートル"+"\n"+"ハリファ塔より高い";
				return "ハリファ塔まで\n残り: " + restDistance + "メートル";
			} else if (distance < 3776) {
				restDistance = 3776 - distance;
				// return restDistance+"メートル"+"\n"+"富士山より高い";
				return "富士山まで\n残り: " + restDistance + "メートル";
			} else {
				return "高すぎる";
			}
		}else if(language.equals("zh")){
			if (distance < 147) {
				restDistance = 147 - distance;
				System.out.println("剩余距离：" + restDistance);
				return "还有 "+restDistance+" 米,\n就比金字塔高了.";
			} else if (distance < 324) {
				restDistance = 324 - distance;
				System.out.println("剩余距离：" + restDistance);
				//eiffel 324
				return "还有 "+restDistance+" 米,\n就比艾福尔铁塔高了.";
			} else if (distance < 634) {
				restDistance = 634 - distance;
				return "还有 "+restDistance+" 米,\n就比东京塔高了.";
			} else if (distance < 829) {
				restDistance = 829 - distance;
				//Burj Khalifa 829
				return "还有 "+restDistance+" 米,\n就比哈利法塔高了.";
			} else if (distance < 3776) {
				restDistance = 3776 - distance;
				//Mount Fuji 3,776
				return "还有 "+restDistance+" 米,\n就比富士山高了.";
				//Mount Everest 8,848
			} else if (distance < 8848) {
				restDistance = 8848 - distance;
				return "还有 "+restDistance+" 米,\n就比喜马拉雅山高了.";
				//Mount Everest 8,848
			}else {
				return "Too high";
			}
		}
		else{
			if (distance < 147) {
				restDistance = 147 - distance;
				System.out.println("rest distance:" + restDistance);
				return "Another "+restDistance+" meters,\nit'll be higher than the Pyramid.";
			} else if (distance < 324) {
				restDistance = 324 - distance;
				System.out.println("rest distance:" + restDistance);
				//Eiffel 324
				return "Another "+restDistance+" meters,\nit'll be higher than the Eiffel tower.";
			} else if (distance < 634) {
				restDistance = 634 - distance;
				return "Another "+restDistance+" meters,\nit'll be higher than the Tokyo Skytree.";
			} else if (distance < 829) {
				restDistance = 829 - distance;
				//Burj Khalifa 829
				return "Another "+restDistance+" meters,\nit'll be higher than the Burj Khalifa.";
			} else if (distance < 3776) {
				restDistance = 3776 - distance;
				//Mount Fuji 3,776
				return "Another "+restDistance+" meters,\nit'll be higher than the Mount Fuji.";
				//Mount Everest 8,848
			} else if (distance < 8848) {
				restDistance = 8848 - distance;
				return "Another "+restDistance+" meters,\nit'll be higher than the Mount Everest.";
				//Mount Everest 8,848
			}else {
				return "Too high";
			}
		}
	}
}
