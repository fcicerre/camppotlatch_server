package org.coursera.camppotlatch.client.serviceproxy;

import java.util.Collection;

import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.GiftImageType;
import org.coursera.camppotlatch.service.model.OperationResult;
import org.coursera.camppotlatch.service.model.User;
import org.coursera.camppotlatch.service.model.UserImageType;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

public interface UserServiceApi {
	// RESTFul operation paths
	
	// The path where we expect the User service to live
	public static final String USER_SVC_PATH = "/user";
	
	public static final String USER_BY_LOGIN_PATH = USER_SVC_PATH + "/{login}";
	public static final String FIND_ALL_USERS_PATH = USER_SVC_PATH + "/findAll";
	public static final String FIND_TOP_GIFT_GIVERS = USER_SVC_PATH + "/findTopGiftGivers";
	
	public static final String USER_IMAGE_PATH = USER_SVC_PATH + "/{login}/image";
	public static final String USER_PASSWORD_PATH = USER_SVC_PATH + "/{login}/password";
	
	// Parameters
	public static final String LOGIN_PARAMETER = "login";
	
	//public static final String MIN_CREATION_TIME_PARAMETER = "minCreationTime";
	//public static final String MAX_CREATION_TIME_PARAMETER = "maxCreationTime";
	public static final String NAME_PART_PARAMETER = "namePart";
	public static final String OFFSET_PARAMETER = "offset";
	public static final String LIMIT_PARAMETER = "limit";
	
	public static final String IMAGE_PARAMETER = "image";
	public static final String IMAGE_TYPE_PARAMETER = "type";

	// Methods
	@POST(USER_SVC_PATH)
	public User addUser(@Body User user);
	
	@GET(USER_SVC_PATH)
	public Collection<User> getAll(
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
	
	@GET(USER_BY_LOGIN_PATH)
	public User findByLogin(@Path(LOGIN_PARAMETER) String login);
	
	@GET(FIND_ALL_USERS_PATH)
	public Collection<User> findAll(
			@Query(NAME_PART_PARAMETER) String namePart,
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);
	
	@GET(FIND_TOP_GIFT_GIVERS)
	public Collection<User> findTopGiftGivers(
			@Query(NAME_PART_PARAMETER) String namePart,
			@Query(OFFSET_PARAMETER) Integer offset,
			@Query(LIMIT_PARAMETER) Integer limit);

	@PUT(USER_BY_LOGIN_PATH)
	public OperationResult updateUser(@Path(LOGIN_PARAMETER) String login, @Body User user);
	
	@DELETE(USER_BY_LOGIN_PATH)
	public OperationResult removeUser(@Path(LOGIN_PARAMETER) String login);
	
	// User image management
	@Multipart
	@POST(USER_IMAGE_PATH)
	public OperationResult postUserImage(@Path(LOGIN_PARAMETER) String login, @Part(IMAGE_PARAMETER) TypedFile imageData);
	
	/**
	 * Get the user image from the server.
	 * 
	 * @param login - user login
	 * @param type - image type ( normal | thumbnail )
	 * @return jpeg image as a stream
	 */
	@Streaming
	@GET(USER_IMAGE_PATH)
	public Response getUserImage(@Path(LOGIN_PARAMETER) String login, @Query(IMAGE_TYPE_PARAMETER) UserImageType type);
	
	// Password management
	@PUT(USER_PASSWORD_PATH)
	public OperationResult changePassword(@Path(LOGIN_PARAMETER) String login, @Body Collection<String> passwords);
}
