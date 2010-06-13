package net.fushihara.LDRoid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LDRClient extends DefaultHttpClient {
	
	public class Subscribe extends Object {
		public List<String> tags = new ArrayList<String>();
		public String folder;
		public String subscribe_id;
		public String icon;
		public String title;
		public int rate;
		public String link;
		public int subscribers_count;
		public int unread_count;
		
		public Subscribe(JSONObject obj) throws JSONException {
			folder = obj.getString("folder");
			subscribe_id = obj.getString("subscribe_id");
			icon = obj.getString("icon");
			title = obj.getString("title");
			link = obj.getString("link");
			rate = obj.getInt("rate");
			subscribers_count = obj.getInt("subscribers_count");
			unread_count = obj.getInt("unread_count");
			JSONArray json = obj.getJSONArray("tags");
			for (int i = 0; i < json.length(); i++) {
			    JSONObject jo = json.getJSONObject(i);
			    tags.add(jo.toString());
			}
		}
	}
	
	public class Feed extends Object {
		public String title;
		public String author;
		public String link;
		public long date;
		public String body;

		public Feed(JSONObject obj) throws JSONException {
			title = obj.getString("title");
			author = obj.getString("author");
			link = obj.getString("link");
			body = obj.getString("body");
			date = obj.optLong("modified_on");
		}
	}
	
	private static String auth_url = "https://member.livedoor.com/login/";
	private static String domain = "http://reader.livedoor.com/";
	
	private String login_id;
	private String password;
	private String session_id = null;

	public LDRClient(Context context) {
		super();
		
		getAccount(context);
	}

	private void getAccount(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		setLoginId(pref.getString("login_id", null));
		password = pref.getString("password", null);
	}
	
	public List<Subscribe> subs(Context context, int unread) {
		try {
			login(context);

			HttpPost req = new HttpPost(domain + "/api/subs");
	        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
	        
	        params.add(new BasicNameValuePair("unread", String.valueOf(unread)));
	        params.add(new BasicNameValuePair("ApiKey", session_id));

			req.setEntity(new UrlEncodedFormEntity(params));
	        HttpResponse response = execute(req);
			
			JSONArray json = new JSONArray(getContent(response));
			List<Subscribe> items = new ArrayList<Subscribe>();
			for (int i = 0; i < json.length(); i++) {
			    JSONObject obj = json.getJSONObject(i);
			    items.add(new Subscribe(obj));
			}
			
			return items;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public List<Feed> unRead(Context context, String subscribe_id) {
		try {
			login(context);

			HttpPost req = new HttpPost(domain + "/api/unread");
	        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
	        
	        params.add(new BasicNameValuePair("subscribe_id", subscribe_id));
	        params.add(new BasicNameValuePair("ApiKey", session_id));

			req.setEntity(new UrlEncodedFormEntity(params));
	        HttpResponse response = execute(req);

	        JSONObject jsonroot = new JSONObject(getContent(response));
	        JSONArray json = jsonroot.getJSONArray("items");
			List<Feed> items = new ArrayList<Feed>();
			for (int i = 0; i < json.length(); i++) {
			    JSONObject obj = json.getJSONObject(i);
			    items.add(new Feed(obj));
			}
			
			return items;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void touchAll(Context context, String subscribe_id) {
		try {
			login(context);

			HttpPost req = new HttpPost(domain + "/api/touch_all");
	        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
	        
	        params.add(new BasicNameValuePair("subscribe_id", subscribe_id));
	        params.add(new BasicNameValuePair("ApiKey", session_id));

			req.setEntity(new UrlEncodedFormEntity(params));
	        execute(req);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void pin_add(Context context, Feed feed) {
		try {
			login(context);

			HttpPost req = new HttpPost(domain + "/api/pin/add");
	        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
	        
	        params.add(new BasicNameValuePair("title", feed.title));
	        params.add(new BasicNameValuePair("link", feed.link));
	        params.add(new BasicNameValuePair("ApiKey", session_id));

			req.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
	        execute(req);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// HttpResponseのbodyを取得
	private String getContent(HttpResponse response) {
		try {
			InputStream in = response.getEntity().getContent();
			
			InputStreamReader isr = new InputStreamReader(in);
	    	BufferedReader buf = new BufferedReader(isr);
	        StringBuilder strb = new StringBuilder();
	        
	        String sline;
			while ( (sline = buf.readLine()) != null ) {
				strb.append(sline + "\n");
			}
	        
	        return strb.toString();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private void login(Context context) throws ClientProtocolException, IOException {
		if (session_id != null) {
			return;
		}
		if (getLoginId() == null || password == null) {
			getAccount(context);
		}
		
		HttpPost request = new HttpPost(auth_url);
		
		// ログインのために必要なパラメータをセット
		// .next, .svが無いと上手くいかない模様
        List<NameValuePair> params = new ArrayList<NameValuePair>(1);
        
        params.add(new BasicNameValuePair("livedoor_id", getLoginId()));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair(".next", "http://reader.livedoor.com/reader/"));
        params.add(new BasicNameValuePair(".sv", "reader"));
        
		request.setEntity(new UrlEncodedFormEntity(params));
		
		// ログイン
		HttpResponse response = execute(request);

		// LDRセッションIDの取得
		Header[] headers = response.getHeaders("Set-Cookie");
		for(int i=0; i<headers.length; i++) {
			String val = headers[i].getValue();
			Pattern p = Pattern.compile("reader_sid=(.+?);");
			Matcher m = p.matcher(val);
			
			if ( m.find() ) {
				session_id = m.group(1);
			}
		}

	}

	public void setLoginId(String login_id) {
		this.login_id = login_id;
	}

	public String getLoginId() {
		return login_id;
	}
}
