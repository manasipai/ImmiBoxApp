
package com.sjsu.project.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sjsu.project.services.S3Services;

@Controller
@RequestMapping("/immibox")
public class FileUploadController {

	Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	S3Services s3Services;

	
	/**
	 * Upload Multipart File post methoid 
	 * @param file
	 * @param fileDescription
	 * @param model
	 * @param redirectAttributes
	 * @param authentication
	 * @return
	 */
	@PostMapping("/uploadFile")
	public String uploadMultipartFile(@RequestParam("uploadedFile") MultipartFile file, @RequestParam("fileDesc") String fileDescription,  Model model,
			RedirectAttributes redirectAttributes, Authentication authentication) {
		LOG.info("Inside the FileUploadController file={}  fileName={} userName={}", file, file.getName(), authentication.getName());
		try {
			
			String key = authentication.getName() + "/" + file.getOriginalFilename();
			DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
			String firstName = defaultOidcUser.getGivenName();
			String lastName = defaultOidcUser.getFamilyName();
			String fileurl = s3Services.uploadFile(key, file, fileDescription, firstName, lastName);
			model.addAttribute("fileURL", fileurl);
			model.addAttribute("filename", file.getOriginalFilename());
			
		} catch (Exception e) {
			LOG.error("Error occurred in Uploading the file", e);
			model.addAttribute("errorMessage", "Oops ! Something wrong happend on the server");
			return "immiboxerror";
		}
		return "uploadFilesScreen";
	}
	
	
	@GetMapping("/uploadFilesScreen")
	public String showUploadFileScreen() {
		LOG.info("Inside the Upload File Screen method");
		return "uploadFilesScreen";
	}

}