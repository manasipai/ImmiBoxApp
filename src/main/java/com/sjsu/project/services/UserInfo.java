
package com.sjsu.project.services;

public class UserInfo {
	private final String firstName;
	private final String lastName;
    private final String userName;
    private final String emailAddr;
    private final String location;
    private final boolean emailVerified;
    
    public UserInfo(final String firstName, final String lastName, final String userName, final String emailAddr, final String location, boolean emailVerified) {
        this.firstName = firstName;
        this.lastName = lastName;
    	this.userName = userName;
        this.emailAddr = emailAddr;
        this.location = location;
        this.emailVerified = emailVerified;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getUserName() {
        return userName;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public String getLocation() {
        return location;
    }

	public boolean isEmailVerified() {
		return emailVerified;
	}

	@Override
	public String toString() {
		return "UserInfo [firstName=" + firstName + ",lastName=" + lastName +", userName=" + userName + ", emailAddr=" + emailAddr + ", location=" + location
				+ ", emailVerified=" + emailVerified + "]";
	}
	

}
