package net.javaguides.springboot.web.dto;

public class UserRegistrationDto {

	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private boolean Usingfa;
	private String phoneNumber_phoneCode;
	private String phoneNumber;
	private String token;

	public UserRegistrationDto(){}

	public UserRegistrationDto(String firstName, String lastName, String email, String password,boolean Usingfa, String token, String phoneNumber, String phoneNumber_phoneCode) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.Usingfa = Usingfa;
		this.token = token;
		this.phoneNumber_phoneCode = phoneNumber_phoneCode;
		this.phoneNumber = phoneNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isUsingfa() {
		return Usingfa;
	}

	public void setUsingfa(boolean usingfa) {
		Usingfa = usingfa;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPhoneNumber_phoneCode() {
		return phoneNumber_phoneCode;
	}

	public void setPhoneNumber_phoneCode(String phoneNumber_phoneCode) {
		this.phoneNumber_phoneCode = phoneNumber_phoneCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}

