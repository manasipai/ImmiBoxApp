
package com.sjsu.project.services;


public class FileInfo {
	private final String firstName;
	private final String lastName;
    private final String fileName;
    private final String lastUpdated;
    private final String fileDownload;
    private final String fileDescription;
    
    public FileInfo(final String fileName, final String lastUpdated, final String fileDownload, final String fileDescription, final String firstName, final String lastName) {
        this.fileName = fileName;
        this.lastUpdated = lastUpdated;
        this.fileDownload = fileDownload;
        this.fileDescription = fileDescription;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
    
    public String getFileDownload() {
        return fileDownload;
    }
    
    public String getFileDescription()
    {
    	return fileDescription;
    }
    
    public String getFirstName()
    {
    	return firstName;
    }
    
    public String getLastName()
    {
    	return lastName;
    }

}
