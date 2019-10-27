
package com.sjsu.project.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.sjsu.project.services.FileInfo;
import com.sjsu.project.services.S3Services;
import com.sjsu.project.services.UserInfo;

@Controller
public class HomeController {

	Logger LOG = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	S3Services s3Services;

	/**
	 * Get method for users and admin 
	 * @param model
	 * @param authentication
	 * @return
	 */
	@GetMapping("/")
	public String home(Model model, Authentication authentication) {
		LOG.info("Loading the root URL page first ");
		try {
			if (authentication != null && authentication.getName() != null) {

				LOG.info("User is authenicated. userName={} credentials={}", authentication.getName(),
						authentication.getCredentials());
				DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
				Map<String, Object> attributes = defaultOidcUser.getAttributes();
				String cognitoGroup = null;
				
				
				
				
				
				// When the group is not null and the group is already assigned
				 	if (attributes.get("cognito:groups") != null) {
					List<String> cognitoGroupList = (List<String>) attributes.get("cognito:groups");
					if (!CollectionUtils.isEmpty(cognitoGroupList)) {
						cognitoGroup = (String) cognitoGroupList.get(0);
					}
				}
				
				

				LOG.info("User has role and group. cognitoGroup={}", cognitoGroup);

				// When the group is coming null and the group is not assigned
				if (cognitoGroup == null) {
					LOG.info("Assigning Group to the User ");
					s3Services.addUserToGroup(authentication.getName(), "immiboxusersgrp");
					cognitoGroup = "immiboxusersgrp";
				}

				// When the group is User group
				if (cognitoGroup.equals("immiboxusersgrp")) {
					LOG.info("User with immiboxusersgrp group signed in ");
					List<S3ObjectSummary> objects;
					List<FileInfo> files = new ArrayList<>();
					String key = null;
					String date = null;
					String fileURL = null;
					String fileDescription = null;
					String firstName = null;
					String lastName = null;
					objects = s3Services.getObjectslistFromFolder(authentication.getName());

					for (S3ObjectSummary objectSummary : objects) {
						key = objectSummary.getKey();
						date = objectSummary.getLastModified().toString();
						fileURL = s3Services.getFileURL(key);
						ObjectMetadata objMetadata =  s3Services.getFileMetaData(key);
						
						fileDescription = objMetadata.getUserMetaDataOf("file-description");
						FileInfo file = new FileInfo(key, date, fileURL, fileDescription, firstName, lastName);
						files.add(file);
					}
					model.addAttribute("files", files);
				}

				// When the group is Admin group
				if (cognitoGroup.equals("immiboxadmingrp")) {
					LOG.info("User with immiboxadmingrp group signed in ");
					adminPage(model, authentication);
					return "adminPage";
				}

			}

		} catch (Exception e) {
			LOG.error("Some error happened on the server. Please check the system logs. ", e);
			model.addAttribute("errorMessage", "Some error happened on the server. Please check the system logs.");
			return "immiboxerror";
		}

		return "index";
	}
	
	
/**
 * UserProfile functionality
 * @param model
 * @param authentication
 * @return
 */
	@GetMapping("/userProfile")
	public String userProfile(Model model, Authentication authentication) {
		DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
		boolean emailVerified = defaultOidcUser.getEmailVerified();
		String emailId = defaultOidcUser.getEmail();
		String userName = (String) defaultOidcUser.getAttributes().get("username");
		String region = defaultOidcUser.getAddress().getRegion();
		String firstName = defaultOidcUser.getGivenName();
		String lastName = defaultOidcUser.getFamilyName();
		UserInfo userInfo = new UserInfo(firstName, lastName, userName, emailId, region, emailVerified);
		LOG.info("Loading the userProfile page userName={}", userInfo.toString());
		model.addAttribute("userInfo", userInfo);
		return "userProfile";

	}
	
	
	/**
	 * post method for delete files for admin
	 * @param fileName
	 * @param authentication
	 * @param model
	 * @return
	 */
	@PostMapping("/deleteFile")
	public String deleteFile(@RequestParam String fileName, Authentication authentication, Model model) {
		LOG.info("Deleting userfile fileName={}", fileName);
		 s3Services.deleteUserFiles(fileName);
		 adminPage(model, authentication);
		 return "adminPage";
	}
	
	/**
	 * admin page functionality for getting files
	 * @param model
	 * @param authentication
	 */
	public void adminPage(Model model, Authentication authentication) {

		LOG.info("Loading the adminPage page userName={}", authentication.getName());

		List<S3ObjectSummary> objects;
		List<FileInfo> files = new ArrayList<>();
		
		String key = null;
		String date = null;
		String fileURL = null;
		String fileDescription = null;
		String firstName = null;
		String lastName = null;
		
		objects = s3Services.getAllUserFiles();

		for (S3ObjectSummary objectSummary : objects) {
			key = objectSummary.getKey();
			date = objectSummary.getLastModified().toString();
			fileURL = s3Services.getFileURL(key);
			ObjectMetadata objMetadata =  s3Services.getFileMetaData(key);
			fileDescription = objMetadata.getUserMetaDataOf("file-description");
			firstName = objMetadata.getUserMetaDataOf("user-firstname");
			lastName = objMetadata.getUserMetaDataOf("user-lastname");
			FileInfo file = new FileInfo(key, date, fileURL, fileDescription, firstName, lastName);
			files.add(file);
		}
		model.addAttribute("userfiles", files);

	}

}
