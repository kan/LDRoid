package net.fushihara.LDRoid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.fushihara.LDRoid.LDRClient.Feeds;
import net.fushihara.LDRoid.LDRClient.Subscribe;
import net.fushihara.LDRoid.PrefetchUnReadFeedsTask.OnPrefetchUnReadFeedsListener;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

public class Main extends ListActivity implements OnPrefetchUnReadFeedsListener {
	private static final String TAG = "Main";
    public static final String KEY_LOGIN_ID = "login_id";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SUBS_ID  = "subs_id";
    public static final String KEY_SUBS_TITLE = "subs_title";
    public static final String KEY_SUBS_UNREAD_COUNT = "subs_unread_count";
    private static final String SUBS_FILE = "subs";
    private static final int PREFETCH_COUNT = 5;
    
    private static final int REQUEST_SETTING = 1;
    private static final int REQUEST_FEEDVIEW = 2;

	private SubscribeLocalList subs;
	private PrefetchUnReadFeedsTask prefetch_task;
	private int prefetch_start_position;
	private int prefetch_limit;
	private ProgressDialog prefetch_dialog;

	private UnReadFeedsCache feeds_cache;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        feeds_cache = UnReadFeedsCache.getInstance(getApplicationContext());
        
        // 保存されている subs を読み込む
        loadFromFile();
    }
    
    private void loadFromFile() {
    	SubscribeLocalList merged = null;
        SubscribeLocalList sll = loadSubsLocalFromFile();
        if (sll != null) {
        	List<Subscribe> subs = loadSubsFromFile();
        	if (subs != null) {
        		merged = new SubscribeLocalList(subs, sll);
        	}
        }

        if (merged == null) {
        	setSubs(new SubscribeLocalList());
        }
        else {
        	setSubs(merged);
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	//loadSubs();
    }
    
	private void loadSubs() {
		Log.d(TAG, "loadSubs");

		LDRClient client = getClient();
		if ( client == null ) {
			// ID/PW未設定
	    	showSetting();
        	return;
		}

		GetSubsTask task = new GetSubsTask(this);
		task.execute(client);
	}
	
	private LDRClient getClient() {
		LDRoidApplication app = (LDRoidApplication)getApplication(); 
		return app.getClient();
	}
	
	public void onGetSubsTaskCompleted(List<Subscribe> result, Exception error) {
		if (error != null) {
			Toast.makeText(this, 
					"ERROR: " + error.getMessage(), 
					Toast.LENGTH_LONG).show();
			return;
		}
		
		
		
		if (result == null) {
			Toast.makeText(this, "no feed", Toast.LENGTH_LONG).show();
		}
		
		feeds_cache.clear();

		// subs をレート順に
		Collections.sort(result, new Comparator<Subscribe>() {
			@Override
			public int compare(Subscribe object1, Subscribe object2) {
				return object2.rate - object1.rate;
			}
		});
		
		// 読み込んだ subs をファイルに書き出す
		saveSubsToFile((ArrayList<Subscribe>)result);
		
		SubscribeLocalList subsLocal = new SubscribeLocalList(result);
		//removeSubsLocalFile(subsLocal);
		setSubs(subsLocal);
	}
	
	private void setSubs(SubscribeLocalList newSubs) {
		subs = newSubs;
		
		// キャッシュ済みかどうかのフラグを設定
		ArrayList<String> cachedList = feeds_cache.getList();
		int cachedList_size = cachedList.size();
		for (int j=0; j<cachedList_size; j++) {
			SubscribeLocal sl = subs.getItemById(cachedList.get(j));
			sl.setPrefetched(sl != null);
		}
		
		SubsAdapter adapter = new SubsAdapter(this, subs);
		setListAdapter(adapter);
        prefetchStart(0, PREFETCH_COUNT);
	}
	
	private void showSetting() {
     	Intent intent = new Intent(this, Setting.class);
     	startActivityForResult(intent, REQUEST_SETTING);
	}
	
	private void prefetchStart(int start_position, int limit) {
		prefetch_start_position = start_position;
		prefetch_limit = limit;
		
		if (prefetch_dialog != null) {
			prefetch_dialog.setMax(limit);
		}
		
		if (!prefetchNext()) {
			prefetchFinish();
		}
	}
	
	// フィードの先読み
	private boolean prefetchNext() {
		if (prefetch_task != null) {
			return false;
		}
		
		int position = prefetch_start_position;
		SubsAdapter adapter = (SubsAdapter)getListAdapter();
		// フィードが空か、リストの範囲外が指定されたときは先読みしない
		if (position < 0 || position >= adapter.getCount()) {
			return false;
		}

		LDRClient client = getClient();
		if (client == null) {
			return false;
		}
		
		// 先読み開始位置からキャッシュの存在を確認して、キャッシュがなければタスクを起動
		int last = Math.min(adapter.getCount(), position + prefetch_limit);

		for (; position<last; position++) {
			SubscribeLocal sub = (SubscribeLocal)adapter.getItem(position);
			if (!sub.isPrefetched() && !feeds_cache.isExists(sub.getSubscribeId())) {
				// キャッシュが作成されていないものを見つけたらタスクを起動
				Log.d(TAG, "prefetch " + position);
				prefetch_task = new PrefetchUnReadFeedsTask(client, this);
				setProgressBarIndeterminateVisibility(true);
				prefetch_task.execute(sub.getSubscribeId());

				// 「すべて取得」でダイアログが表示されているときは進捗を更新
				if (prefetch_dialog != null && prefetch_dialog.isShowing() && last > 0) {
					prefetch_dialog.setProgress(position);
				}
				
				return true;
			}
		}
		return false;
	}
	
	private void prefetchCancel() {
		prefetch_limit = 0;
	}

	// prefetchStart で始まった動作が完了したときの処理
	private void prefetchFinish() {
		// 「すべて取得」のダイアログが表示されていたら消す
		if (prefetch_dialog != null) {
			prefetch_dialog.dismiss();
			prefetch_dialog = null;
		}
	}
	
	// フィードの先読み完了
	@Override
	public void onPrefetchUnReadFeedsTaskComplete(Object sender, 
			String subscribe_id, Feeds feeds, Exception e) {

		setProgressBarIndeterminateVisibility(false);
		// エラーが無ければ保存する
		prefetch_task = null;
		
		if (e == null) {
			// TODO: ファイルの書き出しまでAsyncTaskでやったほうが
			// パフォーマンスが良いと思うが、FeedView で書き込みが
			// 同時に発生する可能性があるのでメインスレッドで実行
			feeds_cache.put(subscribe_id, feeds);
			
			SubscribeLocal sl = subs.getItemById(subscribe_id);
			if (sl != null) {
				sl.setPrefetched(true);
				getListView().invalidateViews();
			}
			
			// 次の先読みを開始
			if (!prefetchNext()) {
				prefetchFinish();
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private List<Subscribe> loadSubsFromFile() {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ArrayList<Subscribe> result = null;
		
		try {
			fis = openFileInput(SUBS_FILE);
			ois = new ObjectInputStream(fis);
			result = (ArrayList<Subscribe>)ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "loadSubsFromFile " + (result != null));
		
		return result;
	}
	
	private void saveSubsToFile(List<Subscribe> subs) {
		Log.d(TAG, "saveSubsToFile");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(SUBS_FILE, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(subs);
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
	
	private void saveSubsLocalToFile() {
		ObjToFile of = new ObjToFile(this, "subs_local");
		of.put("0", subs);
	}
	
	private SubscribeLocalList loadSubsLocalFromFile() {
		SubscribeLocalList  sl = null;
		try {
			ObjToFile of = new ObjToFile(this, "subs_local");
			sl = new SubscribeLocalList(
					loadSubsFromFile(),
					(SubscribeLocalList)of.get("0"));
		}
		catch (Exception e) {
		}
		return sl;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		saveSubsLocalToFile();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
	    getMenuInflater().inflate(R.menu.main_menu, menu);
	    return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        default:
            ret = super.onOptionsItemSelected(item);
            break;
        case R.id.menu_reload:
            loadSubs();
            break;
        case R.id.menu_setting:
        	showSetting();
        	break;
        case R.id.menu_fetch_all:
        	fetchAll();
        }
        return ret;
    }
    
    // すべて取得
    private void fetchAll() {
    	prefetch_dialog = new ProgressDialog(this);
    	prefetch_dialog.setMessage(getString(R.string.dlg_fetch_all_title));
    	prefetch_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	prefetch_dialog.setButton(getString(R.string.dlg_cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				prefetchCancel();
			}
		});
    	prefetch_dialog.show();
    	prefetchStart(0, getListAdapter().getCount());
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	Log.d(TAG, "onActivityResult");
    	switch (requestCode) {
    	case REQUEST_SETTING:
    		// 設定画面から帰ってきたらアカウントが変更されていないか確認する
    		LDRoidApplication app = (LDRoidApplication)getApplication();
    		LDRClient client = app.getClient();
    		if (!client.getAccount().equals(app.getAccount())) {
        		// アカウントが変更されていたら、新しいアカウントで再取得する
    			// TODO:
    			app.clearClient();
				loadSubs();
    		}
    		break;
    	case REQUEST_FEEDVIEW:
    		// FeedView から戻ったとき、既読化されていたら(RESULT_OK)
    		// subs の unread_count を 0 にする
    		if (data != null) {
    			String subs_id = data.getStringExtra(KEY_SUBS_ID);
    			if (subs_id != null) {
        			if (data.hasExtra(FeedView.KEY_TOUCHED)) {
        				SubscribeLocal sl = subs.getItemById(subs_id);
        				if (sl != null) {
        					sl.setTouchState(SubscribeLocal.TOUCH_FINISHED);
        					getListView().invalidateViews();
        				}
        			}

        			// FeedViewでキャッシュが作成されたか確認
    				if (feeds_cache.isExists(subs_id)) {
    					SubscribeLocal sl = subs.getItemById(subs_id);
    					if (sl != null) {
    						sl.setPrefetched(true);
    	    		        getListView().invalidateViews();
    					}
    				}
    			}
    		}
    		break;
    	}
    }
    
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        // TODO: もしも prefetch_task で取得中のフィードが
        // クリックされた場合は、prefetch_task の終了を待たないと二重に読み込みが
        // 発生してしまうので少し無駄に待たされる
        
        Intent i = new Intent(this, FeedView.class);
        SubscribeLocal sub = subs.get(position);
        i.putExtra(KEY_SUBS_ID, sub.getSubscribeId());
        i.putExtra(KEY_SUBS_TITLE, sub.getTitle());
        i.putExtra(KEY_SUBS_UNREAD_COUNT, sub.getUnreadCount());
        startActivityForResult(i, REQUEST_FEEDVIEW);

        prefetchStart(position + 1, PREFETCH_COUNT);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_S:
    		showNextUnread();
    		break;
    	}
    	return super.onKeyDown(keyCode, event);
    }

    private void showNextUnread() {
    	ListView list = getListView();
    	if (list.getCount() == 0) return;

    	int pos = list.getSelectedItemPosition();
    	
		Log.d(TAG, Integer.toString(pos));
		int top_margin = list.getHeight() / 3;
		if (pos == -1)
			list.setSelectionFromTop(list.getFirstVisiblePosition()+1, 0);
		else 
			list.setSelectionFromTop(pos + 1, top_margin);
    }
}
