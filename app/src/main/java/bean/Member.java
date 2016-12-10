package bean;

public class Member {
	
	private String email;
	
	private String tel;
	
	private static Member instance;
	
	public static Member instance() {
		
		if (instance==null) {
			instance = new Member();
		}
		
		return instance;
	}

	public String getEmail() {
		
		if (email==null) {
			email = "test@eoss-th.com";
		}
		
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}
	
}
