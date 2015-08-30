package jp.locky.android.elevator.uti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyFile {
	/**
	 * 移动文件
	 * 
	 * @param srcFileName
	 *            源文件完整路径
	 * @param destDirName
	 *            目的目录完整路径
	 * @return 文件移动成功返回true，否则返回false
	 * @throws IOException 
	 */
	public  void copyFile(String srcFilePath, String dstFilePath) throws IOException {
		  File srcFile = new File(srcFilePath);
		  File dstFile = new File(dstFilePath);
		
		  // ファイルコピーのフェーズ
		  InputStream input = null;
		  OutputStream output = null;
		  input = new FileInputStream(srcFile);
		  output = new FileOutputStream(dstFile);
		 
		  int DEFAULT_BUFFER_SIZE = 1024 * 4;
		  byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		  int n = 0;
		  while (-1 != (n = input.read(buffer))) {
		    output.write(buffer, 0, n);
		  }
		  input.close();
		  output.close();
		}
}
