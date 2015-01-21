package org.coursera.camppotlatch.service.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.http.HttpResponse;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.service.commons.DateUtils;
import org.coursera.camppotlatch.service.commons.ImageUtils;
import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.GiftImageType;
import org.coursera.camppotlatch.service.model.InappropriateMark;
import org.coursera.camppotlatch.service.model.LikeMark;
import org.coursera.camppotlatch.service.model.OperationResult;
import org.coursera.camppotlatch.service.model.User;
import org.coursera.camppotlatch.service.model.OperationResult.OperationResultState;
import org.coursera.camppotlatch.service.repository.GiftRepository;
import org.coursera.camppotlatch.service.repository.InappropriateMarkRepository;
import org.coursera.camppotlatch.service.repository.LikeMarkRepository;
import org.coursera.camppotlatch.service.repository.UserRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

import com.google.common.collect.Lists;

@Controller
public class GiftController {
	
	@Autowired
	private GiftRepository giftRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private LikeMarkRepository likeMarkRepository;
	
	@Autowired
	private InappropriateMarkRepository inappropMarkRepository;
	
	private Log log = new Log4JLogger(GiftController.class.getName());
			
	/**
	 * Add a new gift.
	 */
	@RequestMapping(value=GiftServiceApi.GIFT_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Gift addGift(@RequestBody Gift gift, HttpServletResponse response) {
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		if (gift.getCaptionGiftId() != null) {
			Gift captionGift = giftRepository.findById(gift.getCaptionGiftId());
			if (captionGift != null) {
				// Increments the related gifts count
				captionGift.setRelatedCount(captionGift.getRelatedCount() + 1);
				giftRepository.save(captionGift);
			}
		}
		
		if (gift.getCreatorLogin() != null) {
			User creatorUser = userRepository.findByLogin(gift.getCreatorLogin());
			if (creatorUser != null) {
				// Increments the posted gifts count
				creatorUser.setPostedGiftsCount(creatorUser.getPostedGiftsCount() + 1);
				userRepository.save(creatorUser);
			}
		}
		
		return giftRepository.save(gift);
	}
	
	/**
	 * Receives GET requests to /gift and returns the current list of gifts.
	 */
	@RequestMapping(value=GiftServiceApi.GIFT_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Gift> getAll(
			@RequestParam(defaultValue="0") Integer offset, 
			@RequestParam(defaultValue="20") Integer limit,
			Principal user) {
		Collection<Gift> gifts = null;
		gifts = giftRepository.getAll(offset, limit);
		
		String userLogin = user.getName();		
		for(Gift gift : gifts) {
			// Verify if this gift is marked with like by the user
			LikeMark likeMark = likeMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
			gift.setLikeFlag((likeMark != null) ? 1 : 0);
			
			// Verify if this gift is marked with inappropriate by the user
			InappropriateMark inappropriateMark = inappropMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
			gift.setInappropriateFlag((inappropriateMark != null) ? 1 : 0);			
		}
		
		return gifts;
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_BY_ID_PATH, method=RequestMethod.GET)
	public @ResponseBody Gift findById(@PathVariable String id, Principal user) {
		Gift gift = giftRepository.findById(id);

		String userLogin = user.getName();

		// Verify if this gift is marked with like by the user
		LikeMark likeMark = likeMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
		gift.setLikeFlag((likeMark != null) ? 1 : 0);
		
		// Verify if this gift is marked with inappropriate by the user
		InappropriateMark inappropriateMark = inappropMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
		gift.setInappropriateFlag((inappropriateMark != null) ? 1 : 0);			
		
		return gift;
	}
	
	@RequestMapping(value=GiftServiceApi.FIND_ALL_GIFTS_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Gift> findAll(
			@RequestParam String minCreationTime,
			@RequestParam String maxCreationTime,
			@RequestParam(defaultValue="") String titlePart,
			@RequestParam(defaultValue="false") Boolean disableInappropriate,
			@RequestParam(defaultValue="0") Integer offset,
			@RequestParam(defaultValue="20") Integer limit,
			Principal user,
			HttpServletResponse response) {
		
		Collection<Gift> gifts = null;
		
		if (minCreationTime == null || minCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (maxCreationTime == null || maxCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		try {
			Date minCreationTimeDate = new DateUtils().parseISO8601DateFormat(minCreationTime);
			Date maxCreationTimeDate = new DateUtils().parseISO8601DateFormat(maxCreationTime);
			
			String userLogin = user.getName();
			gifts = giftRepository.findAll(minCreationTimeDate, maxCreationTimeDate, titlePart, 
					disableInappropriate, userLogin, offset, limit);	
			
			for(Gift gift : gifts) {
				// Verify if this gift is marked with like by the user
				LikeMark likeMark = likeMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setLikeFlag((likeMark != null) ? 1 : 0);
				
				// Verify if this gift is marked with inappropriate by the user
				InappropriateMark inappropriateMark = inappropMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setInappropriateFlag((inappropriateMark != null) ? 1 : 0);			
			}
		} catch (ParseException ex) {
			log.error("Error parsing date in findAll operation: " + ex.getMessage(), ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		return gifts;
	}
	
	@RequestMapping(value=GiftServiceApi.FIND_TOP_GIFTS_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Gift> findTopGifts(
			@RequestParam String minCreationTime,
			@RequestParam String maxCreationTime,
			@RequestParam(defaultValue="") String titlePart,
			@RequestParam(defaultValue="false") Boolean disableInappropriate,
			@RequestParam(defaultValue="0") Integer offset,
			@RequestParam(defaultValue="20") Integer limit,
			Principal user,
			HttpServletResponse response) {
		Collection<Gift> gifts = null;
		
		if (minCreationTime == null || minCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (maxCreationTime == null || maxCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		try {
			Date minCreationTimeDate = new DateUtils().parseISO8601DateFormat(minCreationTime);
			Date maxCreationTimeDate = new DateUtils().parseISO8601DateFormat(maxCreationTime);
			
			String userLogin = user.getName();
			gifts = giftRepository.findTopGifts(minCreationTimeDate, maxCreationTimeDate, titlePart,
					disableInappropriate, userLogin, offset, limit);
			
			for(Gift gift : gifts) {
				// Verify if this gift is marked with like by the user
				LikeMark likeMark = likeMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setLikeFlag((likeMark != null) ? 1 : 0);
				
				// Verify if this gift is marked with inappropriate by the user
				InappropriateMark inappropriateMark = inappropMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setInappropriateFlag((inappropriateMark != null) ? 1 : 0);			
			}
		} catch (ParseException ex) {
			log.error("Error parsing date in findTopGifts operation: " + ex.getMessage(), ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		
		return gifts;
	}
	
	@RequestMapping(value=GiftServiceApi.FIND_GIFT_BY_CREATOR_LOGIN_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Gift> findByCreatorLogin(
			@RequestParam String creatorLogin,
			@RequestParam String minCreationTime,
			@RequestParam String maxCreationTime,
			@RequestParam(defaultValue="") String titlePart,
			@RequestParam(defaultValue="false") Boolean disableInappropriate,
			@RequestParam(defaultValue="0") Integer offset,
			@RequestParam(defaultValue="20") Integer limit,
			Principal user,
			HttpServletResponse response) {
		Collection<Gift> gifts = null;
		
		if (creatorLogin == null || creatorLogin == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (minCreationTime == null || minCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (maxCreationTime == null || maxCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		Date minCreationTimeDate = null;
		Date maxCreationTimeDate = null;
		try {
			minCreationTimeDate = new DateUtils().parseISO8601DateFormat(minCreationTime);
			maxCreationTimeDate = new DateUtils().parseISO8601DateFormat(maxCreationTime);
			
		} catch (ParseException ex) {
			log.error("Error parsing date in findByCreatorLogin operation: " + ex.getMessage(), ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		try {
			String userLogin = user.getName();
			gifts = giftRepository.findByCreatorLogin(creatorLogin, minCreationTimeDate, maxCreationTimeDate, 
					titlePart, false, disableInappropriate, userLogin, offset, limit);
			
			for(Gift gift : gifts) {
				// Verify if this gift is marked with like by the user
				LikeMark likeMark = likeMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setLikeFlag((likeMark != null) ? 1 : 0);
				
				// Verify if this gift is marked with inappropriate by the user
				InappropriateMark inappropriateMark = inappropMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setInappropriateFlag((inappropriateMark != null) ? 1 : 0);			
			}
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;			
		}
		
		return gifts;
	}
	
	@RequestMapping(value=GiftServiceApi.FIND_GIFT_BY_CAPTION_GIFT_ID_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Gift> findByCaptionGiftId(
			@RequestParam String captionGiftId,
			@RequestParam String minCreationTime,
			@RequestParam String maxCreationTime,
			@RequestParam(defaultValue="") String titlePart,
			@RequestParam(defaultValue="false") Boolean disableInappropriate,
			@RequestParam(defaultValue="0") Integer offset,
			@RequestParam(defaultValue="20") Integer limit,
			Principal user,
			HttpServletResponse response) {
		Collection<Gift> gifts = null;
		
		if (captionGiftId == null || captionGiftId == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (minCreationTime == null || minCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		if (maxCreationTime == null || maxCreationTime == "") {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
		
		try {
			Date minCreationTimeDate = new DateUtils().parseISO8601DateFormat(minCreationTime);
			Date maxCreationTimeDate = new DateUtils().parseISO8601DateFormat(maxCreationTime);
			
			String userLogin = user.getName();
			gifts = giftRepository.findByCaptionGiftId(captionGiftId, minCreationTimeDate, maxCreationTimeDate, 
					titlePart, disableInappropriate, userLogin, offset, limit);	
			
			for(Gift gift : gifts) {
				// Verify if this gift is marked with like by the user
				LikeMark likeMark = likeMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setLikeFlag((likeMark != null) ? 1 : 0);
				
				// Verify if this gift is marked with inappropriate by the user
				InappropriateMark inappropriateMark = inappropMarkRepository.findByGiftIdAndUserLogin(gift.getId(), userLogin);
				gift.setInappropriateFlag((inappropriateMark != null) ? 1 : 0);			
			}
		} catch (ParseException ex) {
			log.error("Error parsing date in findByCaptionGiftId operation: " + ex.getMessage(), ex);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;			
		}
		
		return gifts;
	}
	
	/**
	 * Update an existing gift.
	 */
	@RequestMapping(value=GiftServiceApi.GIFT_BY_ID_PATH, method=RequestMethod.PUT)
	public @ResponseBody OperationResult updateGift(
			@PathVariable String id,
			@RequestBody Gift gift, HttpServletResponse response) {
		
		Gift retrievedGift = giftRepository.findById(id);
		if (retrievedGift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		if (!gift.getId().equals(id)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
		
		retrievedGift.setTitle(gift.getTitle());
		retrievedGift.setImageId(gift.getImageId());
		retrievedGift.setComments(gift.getComments());
		retrievedGift.setCreatorLogin(gift.getCreatorLogin());
		retrievedGift.setCreateTime(gift.getCreateTime());
		retrievedGift.setLikesCount(gift.getLikesCount());
		retrievedGift.setInappropriateCount(gift.getInappropriateCount());
		retrievedGift.setCaptionGiftId(gift.getCaptionGiftId());

		try {
			giftRepository.save(retrievedGift);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	/**
	 * Remove an existing gift.
	 */
	@RequestMapping(value=GiftServiceApi.GIFT_BY_ID_PATH, method=RequestMethod.DELETE)
	public @ResponseBody OperationResult removeGift(@PathVariable String id, HttpServletResponse response) {
		Gift gift = giftRepository.findById(id);
		if (gift == null)
			return new OperationResult(OperationResultState.SUCCEEDED);
		
		try {
			removeGift(gift);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;			
		}

		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	// Remove gift and related gifts (recursively)
	private void removeGift(Gift gift) throws Exception {
		if (gift.getCaptionGiftId() == null) {
			// Caption gift -- it could contain related gifts
			if (gift.getRelatedCount() > 0) {
				// Delete all related gifts
				Collection<Gift> relatedGifts = 
						giftRepository.findByCaptionGiftId(gift.getId(), null, null, null, false, null, -1, -1);
				for (Gift relatedGift : relatedGifts) {
					removeGift(relatedGift);
				}
			}
		} else {
			// Related gift -- update the caption gift related gifts count
			Gift captionGift = giftRepository.findById(gift.getCaptionGiftId());
			if (captionGift != null) {
				// Decrements the related gifts count
				captionGift.setRelatedCount(captionGift.getRelatedCount() - 1);
				giftRepository.save(captionGift);
			}				
		}

		if (gift.getCreatorLogin() != null) {
			User creatorUser = userRepository.findByLogin(gift.getCreatorLogin());
			if (creatorUser != null) {
				// Decrements the posted gifts count
				creatorUser.setPostedGiftsCount(creatorUser.getPostedGiftsCount() - 1);
				userRepository.save(creatorUser);
			}
		}
		
		giftRepository.delete(gift.getId());		
	}
	
	// Gift image management
	/**
	 * Save an image file to the S3 file system
	 */
	@RequestMapping(value=GiftServiceApi.GIFT_IMAGE_PATH, method=RequestMethod.POST)
	public @ResponseBody OperationResult postGiftImage(
			@PathVariable String id, 
			@RequestParam(GiftServiceApi.IMAGE_PARAMETER) MultipartFile imageFile, HttpServletResponse response) {

		if (id == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
		
		if (imageFile == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);			
		}
			
		Gift gift = giftRepository.findById(id);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}

		gift.setImageContentType(imageFile.getContentType());
		
		InputStream inputStream = null;
		try {
			inputStream = imageFile.getInputStream();
			giftRepository.saveImageFile(gift, inputStream);
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
	@RequestMapping(value=GiftServiceApi.GIFT_IMAGE_PATH, method=RequestMethod.GET)
	public void getGiftImage(@PathVariable String id,
			@RequestParam(defaultValue="NORMAL") GiftImageType type,
			HttpServletResponse response) {
		if (id == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;			
		}
		
		/*
		if (!type.equals("") && !thumbnail.equals("Y") && !thumbnail.equals("N")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		*/
		
		Gift gift = giftRepository.findById(id);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		try {
			response.setContentType(gift.getImageContentType());
			if (type == GiftImageType.NORMAL) {
				// Load the image directly to the response
				giftRepository.loadImageFile(gift, response.getOutputStream());
			} else if (type == GiftImageType.THUMBNAIL) {
				ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream();
				// Load the image in the memory
				giftRepository.loadImageFile(gift, memoryOutputStream);
				memoryOutputStream.flush();
				ByteArrayInputStream memoryInputStream = new ByteArrayInputStream(memoryOutputStream.toByteArray());
				BufferedImage image = ImageIO.read(memoryInputStream);
				BufferedImage resizedImage =
						  Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC,
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
	
	// Gift like mark management
	@RequestMapping(value=GiftServiceApi.GIFT_LIKE_PATH, method=RequestMethod.POST)
	public @ResponseBody OperationResult likeGift(
			@PathVariable String id, Principal principal, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		String userLogin = principal.getName();		
		LikeMark retrievedMark = likeMarkRepository.findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedMark != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);
			//return new OperationResult(OperationResultState.SUCCEEDED);
		}
		
		String creatorLogin = gift.getCreatorLogin();
		if (creatorLogin == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);	
		}
		User creatorUser = userRepository.findByLogin(creatorLogin);
		
		LikeMark likeMark = new LikeMark(giftId, userLogin, new Date());
		
		try {
			// Create the like mark
			likeMarkRepository.save(likeMark);
			
			// Increments the likes count for the gift
			gift.setLikesCount(gift.getLikesCount() + 1);
			giftRepository.save(gift);
			
			// Increments the likes count for the user's gifts
			if (creatorUser != null) {
				creatorUser.setLikesCount(creatorUser.getLikesCount() + 1);
				userRepository.save(creatorUser);
			}
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_UNLIKE_PATH, method=RequestMethod.POST)
	public @ResponseBody OperationResult unlikeGift(
			@PathVariable String id, Principal principal, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		String userLogin = principal.getName();
		LikeMark retrievedMark = likeMarkRepository.findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedMark == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);
			//return new OperationResult(OperationResultState.SUCCEEDED);
		}
		
		String creatorLogin = gift.getCreatorLogin();
		if (creatorLogin == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);	
		}
		User creatorUser = userRepository.findByLogin(creatorLogin);
		
		try {
			// Remove the like mark
			likeMarkRepository.delete(giftId, userLogin);
			
			// Decrements the likes count for the gift
			gift.setLikesCount(gift.getLikesCount() - 1);
			giftRepository.save(gift);
			
			// Decrements the likes count for the user's gifts
			if (creatorUser != null) {
				creatorUser.setLikesCount(creatorUser.getLikesCount() - 1);
				userRepository.save(creatorUser);
			}			
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
		
	@RequestMapping(value=GiftServiceApi.GIFT_IS_LIKED_PATH, method=RequestMethod.GET)
	public @ResponseBody Integer isGiftLiked(
			@PathVariable String id, Principal principal, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return 0;
		}
		
		String userLogin = principal.getName();
		LikeMark retrievedMark = likeMarkRepository.findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedMark == null) {
			return 0;
		}
		
		return 1;		
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_LIKED_BY_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<User> getUsersWhoLikedGift(
			@PathVariable String id,
			@RequestParam Integer offset,
			@RequestParam Integer limit,
			HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		Collection<User> users = new ArrayList<User>();
		
		Collection<LikeMark> likes = likeMarkRepository.findAllByGiftId(giftId, offset, limit);
		for(LikeMark likeMark : likes) {
			String userLogin = likeMark.getUserLogin();
			
			User user = userRepository.findByLogin(userLogin);
			if (user != null)
				users.add(user);
		}
		
		return users;
	}
	
	
	// Gift inappropriate mark management
	@RequestMapping(value=GiftServiceApi.GIFT_MARK_INAPPROPRIATE_PATH, method=RequestMethod.POST)
	public @ResponseBody OperationResult markInappropriateGift(
			@PathVariable String id, Principal principal, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		String userLogin = principal.getName();
		InappropriateMark retrievedMark = inappropMarkRepository.findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedMark != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);
			//return new OperationResult(OperationResultState.SUCCEEDED);
		}
		
		String creatorLogin = gift.getCreatorLogin();
		if (creatorLogin == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);	
		}
		User creatorUser = userRepository.findByLogin(creatorLogin);
		
		InappropriateMark inappropriateMark = new InappropriateMark(giftId, userLogin, new Date());
		
		try {
			// Create the inappropriate mark
			inappropMarkRepository.save(inappropriateMark);
			
			// Increments the inappropriate count for the gift
			gift.setInappropriateCount(gift.getInappropriateCount() + 1);
			giftRepository.save(gift);
			
			// Increments the inappropriate marks count for the user's gifts
			if (creatorUser != null) {
				creatorUser.setInappropriatesCount(creatorUser.getInappropriatesCount() + 1);
				userRepository.save(creatorUser);
			}
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_UNMARK_INAPPROPRIATE_PATH, method=RequestMethod.POST)
	public @ResponseBody OperationResult unmarkInappropriateGift(
			@PathVariable String id, Principal principal, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		String userLogin = principal.getName();
		InappropriateMark retrievedMark = inappropMarkRepository.findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedMark == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return new OperationResult(OperationResultState.FAILED);
			//return new OperationResult(OperationResultState.SUCCEEDED);
		}
		
		String creatorLogin = gift.getCreatorLogin();
		if (creatorLogin == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);	
		}
		User creatorUser = userRepository.findByLogin(creatorLogin);
		
		try {
			// Remove the inappropriate mark
			inappropMarkRepository.delete(giftId, userLogin);
			
			// Decrements the inappropriate count for the gift
			gift.setInappropriateCount(gift.getInappropriateCount() - 1);
			giftRepository.save(gift);
			
			// Decrements the inappropriate marks count for the user's gifts
			if (creatorUser != null) {
				creatorUser.setInappropriatesCount(creatorUser.getInappropriatesCount() - 1);
				userRepository.save(creatorUser);
			}
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new OperationResult(OperationResultState.FAILED);
		}
		
		return new OperationResult(OperationResultState.SUCCEEDED);
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_IS_MARKED_INAPPROPRIATE_PATH, method=RequestMethod.GET)
	public @ResponseBody Integer isGiftMarkedInappropriate(
			@PathVariable String id, Principal principal, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return 0;
		}
		
		String userLogin = principal.getName();
		InappropriateMark retrievedMark = inappropMarkRepository.findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedMark == null) {
			return 0;
		}
		
		return 1;		
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_IS_CONSIDERED_INAPPROPRIATE_PATH, method=RequestMethod.GET)
	public @ResponseBody Integer isGiftConsideredInappropriate(
			@PathVariable String id, HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return 0;
		}
		
		Collection<InappropriateMark> inappropMarks = inappropMarkRepository.findAllByGiftId(giftId, 0, 10);
		if (inappropMarks.size() == 0)
			return 0;
		
		return 1;
	}
	
	@RequestMapping(value=GiftServiceApi.GIFT_MARKED_INAPPROPRIATE_BY_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<User> getUsersWhoMarkedInappropriateGift(
			@PathVariable String id,
			@RequestParam Integer offset,
			@RequestParam Integer limit,
			HttpServletResponse response) {
		
		String giftId = id;
		Gift gift = giftRepository.findById(giftId);
		if (gift == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		Collection<User> users = new ArrayList<User>();
		
		Collection<InappropriateMark> inappropMarks = inappropMarkRepository.findAllByGiftId(giftId, offset, limit);
		for(InappropriateMark inappropriateMark : inappropMarks) {
			String userLogin = inappropriateMark.getUserLogin();
			
			User user = userRepository.findByLogin(userLogin);
			if (user != null)
				users.add(user);
		}
		
		return users;
	}
}
