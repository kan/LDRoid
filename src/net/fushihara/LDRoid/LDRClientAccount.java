package net.fushihara.LDRoid;

import java.io.Serializable;

public class LDRClientAccount implements Serializable {
	private String login_id;
	private String password;

	public LDRClientAccount(String login_id, String password)
	{
		this.login_id = login_id;
		this.password = password;
	}
	
	public String getLoginId() {
		return login_id;
	}
	public String getPassword() {
		return password;
	}
	
	public boolean isEmpty() {
		return ( login_id == null || password == null );
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof LDRClientAccount) {
			LDRClientAccount b = (LDRClientAccount)o; 
			return login_id == b.login_id &&
				password == b.password;
		}
		return false;
	}
}