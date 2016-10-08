
public class User {
	String name;
	String ip;
	public User(String name, String ip) {
		this.name = name;
		this.ip = ip;
	}
	public String getUserName() {
		return name;
	}
	public void setUserName(String name) {
		this.name = name;
	}
	public String getUserIp() {
		return ip;
	}
	public void setUserIp(String ip) {
		this.ip = ip;
	}
}
