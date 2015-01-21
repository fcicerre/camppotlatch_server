package org.coursera.camppotlatch.client.serviceproxy;

import java.util.Collection;
import java.util.Date;

import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.GiftImageType;
import org.coursera.camppotlatch.service.model.OperationResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.amazonaws.services.identitymanagement.model.User;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

public interface GiftServiceApi {
	// RESTFul operation paths
	// Authentication path
	public static final String TOKEN_PATH = "/oauth/token";
	
	// The path where we expect the Gift service to live
	public static final String GIFT_SVC_PATH = "/gift";
	
	// Gift operations
	public static final String GIFT_BY_ID_PATH = GIFT_SVC_PATH + "/{id}";
	public static final String FIND_ALL_GIFTS_PATH = GIFT_SVC_PATH + "/findAll";
	public static final String FIND_TOP_GIFTS_PATH = GIFT_SVC_PATH + "/findTopGifts";
	public static final String FIND_GIFT_BY_CREATOR_LOGIN_PATH = GIFT_SVC_PATH + "/findByCreatorLogin";
	public static final String FIND_GIFT_BY_CAPTION_GIFT_ID_PATH = GIFT_SVC_PATH + "/findByCaptionGiftId";
	
	public static final String GIFT_IMAGE_PATH = GIFT_SVC_PATH + "/{id}/image";
	
	public static final String GIFT_LIKE_PATH = GIFT_SVC_PATH + "/{id}/like";
	public static final String GIFT_UNLIKE_PATH = GIFT_SVC_PATH + "/{id}/unlike";
	public static final String GIFT_IS_LIKED_PATH = GIFT_SVC_PATH + "/{id}/isLiked";
	public static final String GIFT_LIKED_BY_PATH = GIFT_SVC_PATH + "/{id}/likedBy";
	
	public static final String GIFT_MARK_INAPPROPRIATE_PATH = GIFT_SVC_PATH + "/{id}/markInappropriate";
	public static final String GIFT_UNMARK_INAPPROPRIATE_PATH = GIFT_SVC_PATH + "/{id}/unmarkInappropriate";
	public static final String GIFT_IS_MARKED_INAPPROPRIATE_PATH = GIFT_SVC_PATH + "/{id}/isMarkedInappropriate";
	public static final String GIFT_IS_CONSIDERED_INAPPROPRIATE_PATH = GIFT_SVC_PATH + "/{id}/isConsideredInappropriate";
	public static final String GIFT_MARKED_INAPPROPRIATE_BY_PATH = GIFT_SVC_PATH + "/{id}/markedInappropriateBy";
	
	// Parameters
	public static final String ID_PARAMETER = "id";
	
	public static final String MIN_CREATION_TIME_PARAMETER = "minCreationTime";
	public static final String MAX_CREATION_TIME_PARAMETER = "maxCreationTime";
	public static final String TITLE_PART_PARAMETER = "titlePart";
	public static final String DISABLE_INAPPROPRIATE_PARAMETER = "disableInappropriate";
	public static final String OFFSET_PARAMETER = "offset";
	public static final String LIMIT_PARAMETER = "limit";
	
	//public static final String TITLE_PARAMETER = "title";
	public static final String CREATOR_LOGIN_PARAMETER = "creatorLogin";
	public static final String CAPTION_GIFT_ID_PARAMETER = "captionGiftId";
	
	public static final String IMAGE_PARAMETER = "image";
	public static final String IMAGE_TYPE_PARAMETER = "type";

	// Methods
	@POST(GIFT_SVC_PATH)
	public Gift addGift(@Body Gift gift);
	
	@GET(GIFT_SVC_PATH)
	public Collection<Gift> getAll(
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
	
	@GET(GIFT_BY_ID_PATH)
	public Gift findById(@Path(ID_PARAMETER) String id);
	
	@GET(FIND_ALL_GIFTS_PATH)
	public Collection<Gift> findAll(
			@Query(MIN_CREATION_TIME_PARAMETER) String minCreationTime,
			@Query(MAX_CREATION_TIME_PARAMETER) String maxCreationTime,
			@Query(TITLE_PART_PARAMETER) String titlePart,
			@Query(DISABLE_INAPPROPRIATE_PARAMETER) Boolean disableInappropriate, 
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
	
	@GET(FIND_TOP_GIFTS_PATH)
	public Collection<Gift> findTopGifts(
			@Query(MIN_CREATION_TIME_PARAMETER) String minCreationTime,
			@Query(MAX_CREATION_TIME_PARAMETER) String maxCreationTime,
			@Query(TITLE_PART_PARAMETER) String titlePart,
			@Query(DISABLE_INAPPROPRIATE_PARAMETER) Boolean disableInappropriate, 
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);

	@GET(FIND_GIFT_BY_CREATOR_LOGIN_PATH)
	public Collection<Gift> findByCreatorLogin(
			@Query(CREATOR_LOGIN_PARAMETER) String creatorLogin,
			@Query(MIN_CREATION_TIME_PARAMETER) String minCreationTime,
			@Query(MAX_CREATION_TIME_PARAMETER) String maxCreationTime,
			@Query(TITLE_PART_PARAMETER) String titlePart,
			@Query(DISABLE_INAPPROPRIATE_PARAMETER) Boolean disableInappropriate, 
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);

	@GET(FIND_GIFT_BY_CAPTION_GIFT_ID_PATH)
	public Collection<Gift> findByCaptionGiftId(
			@Query(CAPTION_GIFT_ID_PARAMETER) String captionGiftId,
			@Query(MIN_CREATION_TIME_PARAMETER) String minCreationTime,
			@Query(MAX_CREATION_TIME_PARAMETER) String maxCreationTime,
			@Query(TITLE_PART_PARAMETER) String titlePart,
			@Query(DISABLE_INAPPROPRIATE_PARAMETER) Boolean disableInappropriate, 
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
	
	@PUT(GIFT_BY_ID_PATH)
	public OperationResult updateGift(@Path(ID_PARAMETER) String id, @Body Gift gift);
	
	@DELETE(GIFT_BY_ID_PATH)
	public OperationResult removeGift(@Path(ID_PARAMETER) String id);
	
	// Gift image management
	@Multipart
	@POST(GIFT_IMAGE_PATH)
	public OperationResult postGiftImage(@Path(ID_PARAMETER) String id, @Part(IMAGE_PARAMETER) TypedFile imageData);
	
	/**
	 * Get the gift image from the server.
	 * 
	 * @param id - gift id
	 * @param type - image type ( normal | thumbnail )
	 * @return jpeg image as a stream
	 */
	@Streaming
	@GET(GIFT_IMAGE_PATH)
	public Response getGiftImage(@Path(ID_PARAMETER) String id, @Query(IMAGE_TYPE_PARAMETER) GiftImageType type);
	
	// Like management
	@POST(GIFT_LIKE_PATH)
	public OperationResult likeGift(@Path(ID_PARAMETER) String id);
	
	@POST(GIFT_UNLIKE_PATH)
	public OperationResult unlikeGift(@Path(ID_PARAMETER) String id);
		
	@GET(GIFT_IS_LIKED_PATH)
	public Integer isGiftLiked(@Path(ID_PARAMETER) String id);
	
	@GET(GIFT_LIKED_BY_PATH)
	public Collection<User> getUsersWhoLikedGift(
			@Path(ID_PARAMETER) String id,
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
	
	// Inappropriate mark management
	@POST(GIFT_MARK_INAPPROPRIATE_PATH)
	public OperationResult markInappropriateGift(@Path(ID_PARAMETER) String id);
	
	@POST(GIFT_UNMARK_INAPPROPRIATE_PATH)
	public OperationResult unmarkInappropriateGift(@Path(ID_PARAMETER) String id);
	
	@GET(GIFT_IS_MARKED_INAPPROPRIATE_PATH)
	public Integer isGiftMarkedInappropriate(@Path(ID_PARAMETER) String id);
	
	@GET(GIFT_IS_CONSIDERED_INAPPROPRIATE_PATH)
	public Integer isGiftConsideredInappropriate(@Path(ID_PARAMETER) String id);
	
	@GET(GIFT_MARKED_INAPPROPRIATE_BY_PATH)
	public Collection<User> getUsersWhoMarkedInappropriateGift(
			@Path(ID_PARAMETER) String id,
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
}
