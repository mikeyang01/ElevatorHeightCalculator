package jp.locky.android.elevator.uti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileCompress {

	public void zip(String inName, String outName) {
		System.out.println("--begin compress--");
		File file = new File(inName);
		int length = (int) file.length();
		byte[] b = new byte[length];
		try {
			InputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
		} catch (Exception e) {
			System.out.println("zip read error");
			e.printStackTrace();
		}

		try {
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(outName)));
			//zout.setLevel(9);
			ZipEntry zipEntry = new ZipEntry(file.getName());
			zout.putNextEntry(zipEntry);
			zout.write(b);
			zout.finish();
			zout.close();
		} catch (Exception e) {
			System.out.println("zip out error");
			e.printStackTrace();
		}

	}
}
