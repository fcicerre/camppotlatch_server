package org.coursera.camppotlatch.service.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;
import org.coursera.camppotlatch.service.commons.ImageUtils;
import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.OperationResult;
import org.coursera.camppotlatch.service.model.User;
import org.coursera.camppotlatch.service.model.UserImageType;
import org.coursera.camppotlatch.service.model.OperationResult.OperationResultState;
import org.coursera.camppotlatch.service.repository.GiftRepository;
import org.coursera.camppotlatch.service.repository.UserRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import retrofit.http.Body;
import retrofit.http.PUT;
import retrofit.http.Path;

@Controller
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GiftRepository giftRepository;
	
	private Log log = new Log4JLogger(UserController.class.getName());
			
	/**
	 * Add a new user.
	 */
	@RequestMapping(value=UserServiceApi.USER_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody User addUser(@RequestBody User user, HttpServletResponse response) {
		User retrievedUser = userRepository.findByLogin(user.getLogin());
		if (retrievedUser != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		User newUser = userRepository.save(user);
		
		return newUser;
	}
	
	/**
	 * Receives GET requests to /user and returns the current list of users.
	 */
	@RequestMapping(value=UserServiceApi.USER_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<User> getAll(
			@RequestParam(defaultValue="0") Integer offset, 
			@RequestParam(defaultValue="20") Integer limit) {
		
		Collection<User> users = userRepository.getAll(offset, limit);
			
		return users;
	}
	
	@RequestMapping(value=UserServiceApi.USER_BY_LOGIN_PATH, method=RequestMethod.GET)
	public @ResponseBody User findByLogin(@PathVariable String login) {
		User user = userRepository.findByLogin(login);
		
		return user;
	}
	
	@RequestMapping(value=UserServiceApi.FIND_ALL_USERS_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<User> findAll(
			@RequestParam(defaultValue="") String namePart,
			@RequestParam(defaultValue="0") Integer offset,
			@RequestParam(defaultValue="20") Integer limit, 
			HttpServletResponse response) {
		
		Collection<User> users = 
				userRepository.findAll(namePart, offset, limit);	
		
		return users;
	}
	
	@RequestMapping(value=UserServiceApi.FIND_TOP_GIFT_GIVERS, method=RequestMethod.GET)
	public @ResponseBody Collection<User> findTopGiftGivers(
			@RequestParam(defaultValue="") String namePart,
			@RequestParam(defaultValue="0") Integer offset,
			@RequestParam(defaultValue="20") Integer limit,
			HttpServletResponse response) {
		
		Collection<User> users = 
				userRepository.findTopGiftGivers(namePart, offset, limit);	
		
		return users;
	}
	
	/**
	 * Update an existing user.
	 */
	@RequestMapping(value=UserServiceApi.USER_BY_LOGIN_PATH, method=RequestMethod.PUT)
	public @ResponseBody OperationResult updateUser(@PathVariable String login, 
			@RequestBody User user, HttpServletResponse response) {
		
		User retrievedUser = userRepository.findByLogin(login);
		if (retrievedUser == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		if (!user.getLogin().equals(login)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
		
		//retrievedUser.setPassword(user.getPassword());
		retrievedUser.setRoles(user.getRoles());
		retrievedUser.setName(user.getName());
		retrievedUser.setEmail(user.getEmail());		
		retrievedUser.setImageId(user.getImageId());
		retrievedUser.setCity(user.getCity());
		retrievedUser.setCountry(user.getCountry());
		retrievedUser.setCreateTime(user.getCreateTime());
		retrievedUser.setDisableInappropriate(user.getDisableInappropriate());
		retrievedUser.setGiftsReloadPeriod(user.getGiftsReloadPeriod());
		retrievedUser.setLikesCount(user.getLikesCount());
		retrievedUser.setInappropriatesCount(user.getInappropriatesCount());
		retrievedUser.setPostedGiftsCount(user.getPostedGiftsCount());

		try {
			userRepository.save(retrievedUser);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	/**
	 * Remove an existing user.
	 */
	@RequestMapping(value=UserServiceApi.USER_BY_LOGIN_PATH, method=RequestMethod.DELETE)
	public @ResponseBody OperationResult removeUser(@PathVariable String login, HttpServletResponse response) {
		// Remove all the gifts posted by this user
		try {
			Collection<Gift> gifts = giftRepository.findByCreatorLogin(login, null, null, null, true, false, null, -1, -1);
			for (Gift gift : gifts) {
				giftRepository.delete(gift.getId());
			}
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		// Remove the user
		userRepository.delete(login);
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	// User image management
	/**
	 * Save an image file to the S3 file system
	 */
	@RequestMapping(value=UserServiceApi.USER_IMAGE_PATH, method=RequestMethod.POST)
	public @ResponseBody OperationResult postUserImage(
			@PathVariable String login,
			@RequestParam(UserServiceApi.IMAGE_PARAMETER) MultipartFile imageFile, HttpServletResponse response) {

		if (login == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
		
		if (imageFile == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
			
		User user = userRepository.findByLogin(login);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}

		user.setImageContentType(imageFile.getContentType());
		
		InputStream inputStream = null;
		try {
			inputStream = imageFile.getInputStream();
			userRepository.saveImageFile(user, inputStream);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		} finally {				
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {}
		}
	
		response.setStatus(HttpServletResponse.SC_OK);
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	/**
	 * Load an image file from the S3 file system
	 */
	@RequestMapping(value=UserServiceApi.USER_IMAGE_PATH, method=RequestMethod.GET)
	public void getUserImage(@PathVariable String login,
			@RequestParam(defaultValue="NORMAL") UserImageType type,
			HttpServletResponse response) {
		
		if (login == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
		
		User user = userRepository.findByLogin(login);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		try {
			response.setContentType(user.getImageContentType());
			if (type == UserImageType.NORMAL) {
				// Load the image directly to the response
				userRepository.loadImageFile(user, response.getOutputStream());
			} else if (type == UserImageType.THUMBNAIL) {
				ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();
				// Load the image in the memory
				userRepository.loadImageFile(user, memoryOutputStream);
				memoryOutputStream.flush();
				ByteArrayInputStream memoryInputStream = new ByteArrayInputStream(memoryOutputStream.toByteArray());
				BufferedImage image = ImageIO.read(memoryInputStream);
				BufferedImage resizedImage =
						  Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC,
						               Gift.THUMBNAIL_WIDTH, Gift.THUMBNAIL_HEIGHT, Scalr.OP_ANTIALIAS);
				ImageIO.write(resizedImage, ImageUtils.JPEG_TYPE, response.getOutputStream());
			}
			response.flushBuffer();
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	// Password management
	@RequestMapping(value=UserServiceApi.USER_PASSWORD_PATH, method=RequestMethod.PUT)
	public @ResponseBody OperationResult changePassword(
			@PathVariable String login, @RequestBody Collection<String> passwords,
			HttpServletResponse response) {

		if (passwords.size() != 2) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);
		}

		String[] passArray = passwords.toArray(new String[]{});
		String oldPassword = passArray[0];
		String newPassword = passArray[1];
		
		if (oldPassword == null || newPassword == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
		
		User retrievedUser = userRepository.findByLogin(login);
		if (retrievedUser == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		if (!retrievedUser.getPassword().equals(oldPassword)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		retrievedUser.setPassword(newPassword);

		try {
			userRepository.save(retrievedUser);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
}
