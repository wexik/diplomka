package mailinglistonline.server.export.database.entities;

import java.util.Set;
/**
 * Entity which is used to authenticate client app to REST interface
 * @author Július Staššík
 *
 */
public class Client {
	private String name;
	private String pwd;
	private String role;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
			
}
