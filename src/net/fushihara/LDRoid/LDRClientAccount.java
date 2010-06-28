package net.fushihara.LDRoid;

public class LDRClientAccount {
	public String login_id;
	public String password;

	public LDRClientAccount() {
	}
	
	public boolean isEmpty() {
		return ( login_id == null || password == null );
	}
}