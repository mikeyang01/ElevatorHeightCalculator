package jp.locky.android.elevator.uti;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SaveToFile {
	public void saveToFile(String data, String filePath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath,true));
			bw.write(data);
			bw.close();
		} catch (IOException e) {
			System.out.println("save to file:"+e);
		}
	}

}
