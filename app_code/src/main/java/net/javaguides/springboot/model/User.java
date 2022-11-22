package net.javaguides.springboot.model;

import org.jboss.aerogear.security.otp.api.Base32;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name="users", uniqueConstraints={@UniqueConstraint(columnNames={"email"})})
public class User
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "first_name")
	private String firstName;

	@NotNull
	@Column(name = "last_name")
	private String lastName;

//	@Column(unique = true)
	private String email;

	@Column(name = "usingfa")
	private boolean Usingfa;

	@Column(name = "phone_code")
	private String phoneCode;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "authy_id")
	private String authyId;

	@NotNull
	private String password;

	@Column(name = "enabled")
	private boolean enabled;

	@Column(name = "face_recognition")
	private boolean faceRecognition;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE})
	@JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
	private Set<Role> roles = new HashSet<>();

	public User() {}

	public User(String firstName, String lastName, String email, String password, boolean Usingfa, Set<Role> roles, String phoneCode, String phoneNumber, String authyId, boolean faceRecognition) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.roles = roles;
		this.Usingfa=Usingfa;
		this.enabled = true;
		this.faceRecognition = false;
		this.phoneCode = phoneCode;
		this.phoneNumber = phoneNumber;
		this.authyId = authyId;
		this.faceRecognition = faceRecognition;
	}


	public User(String email, Set<Role> roles) {
		super();
		this.email = email;
		this.roles = roles;
	}

	public User(String email){
		super();
		this.email=email;
	}
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public boolean getUsingfa() {
		return Usingfa;
	}

	public void setUsingfa(boolean isusingfa) {
		this.Usingfa = isusingfa;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getAuthyId() {
		return authyId;
	}

	public void setAuthyId(String authyId) {
		this.authyId = authyId;
	}

	public String getPhoneCode() {
		return phoneCode;
	}

	public void setPhoneCode(String phoneCode) {
		this.phoneCode = phoneCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isUsingfa() {
		return Usingfa;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", email='" + email + '\'' +
				", Usingfa=" + Usingfa +
				", phoneCode='" + phoneCode + '\'' +
				", phoneNumber='" + phoneNumber + '\'' +
				", authyId='" + authyId + '\'' +
				", password='" + password + '\'' +
				", enabled=" + enabled +
				", roles=" + roles +
				'}';
	}
}
