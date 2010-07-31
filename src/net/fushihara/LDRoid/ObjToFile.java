package net.fushihara.LDRoid;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

public class ObjToFile {
	private static final String TAG = "ObjToFile";
	private Context context;
	private String prefix;
	
	public ObjToFile(Context context, String prefix) {
		this.context = context;
		this.prefix = prefix;
	}
	
	public String getFileName(String subscribe_id) {
		return prefix + subscribe_id;
	}
	
	// ファイルに保存
	public void put(String name, Object o) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			Log.d(TAG, "put " + getFileName(name));
			fos = context.openFileOutput(getFileName(name), 
					Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isExists(String name) {
		FileInputStream fis = null;
		boolean result = false;
		
		try {
			fis = context.openFileInput(getFileName(name));
			result = true;
			
		} catch (FileNotFoundException e) {
			
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public Object get(String name) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Object o = null;
		
		try {
			Log.d(TAG, "get "+ getFileName(name));
			fis = context.openFileInput(getFileName(name));
			ois = new ObjectInputStream(fis);
			o = ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	// キャッシュをすべてクリア
	public void clear() {
		String filenames [] = context.fileList();
		int filenames_length = filenames.length;
		for (int j=0; j<filenames_length; j++) {
			if (filenames[j].startsWith(prefix)) {
				Log.d(TAG, "delete " + filenames[j]);
				context.deleteFile(filenames[j]);
			}
		}
	}
	
	// すべてのファイル名を取得
	public ArrayList<String> getList() {
		ArrayList<String> result = new ArrayList<String>();
		
		String filenames [] = context.fileList();
		int filenames_length = filenames.length;
		int prefix_length = prefix.length();
		for (int j=0; j<filenames_length; j++) {
			if (filenames[j].startsWith(prefix)) {
				result.add(filenames[j].substring(prefix_length));
			}
		}
		return result;
	}
}
