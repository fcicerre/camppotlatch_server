package org.coursera.camppotlach.service.controller.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.coursera.camppotlach.service.model.test.GiftTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.coursera.camppotlatch.client.auth.UnsafeHttpsClient;
import org.coursera.camppotlatch.client.serviceproxy.GiftServiceApi;
import org.coursera.camppotlatch.service.commons.DateUtils;
import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.GiftImageType;
import org.coursera.camppotlatch.service.model.OperationResult;
import org.coursera.camppotlatch.service.model.OperationResult.OperationResultState;

import retrofit.RestAdapter.LogLevel;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class GiftServiceTest {
	//private static final String TEST_URL = "https://camppotlach-env.elasticbeanstalk.com";
	//private static final String TEST_URL = "http://camppotlach-env.elasticbeanstalk.com";
	//private static final String TEST_URL = "https://localhost:443";
	private static final String TEST_URL = "http://localhost:8080";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "cicerre70";
	private static final String CLIENT_ID = "mobile";
	//private static final String READ_ONLY_CLIENT_ID = "mobileReader";

	/*
	private static GiftServiceApi giftService = new org.coursera.camppotlach.client.auth.SecuredRestBuilder()
			.setLoginEndpoint(TEST_URL + GiftServiceApi.TOKEN_PATH)
			.setUsername(USERNAME)
			.setPassword(PASSWORD)
			.setClientId(CLIENT_ID)
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(GiftServiceApi.class);
	*/
	
	private static GiftServiceApi giftService = new org.coursera.camppotlatch.client.auth.SecuredRestBuilder()
	.setLoginEndpoint(TEST_URL + GiftServiceApi.TOKEN_PATH)
	.setUsername(USERNAME)
	.setPassword(PASSWORD)
	.setClientId(CLIENT_ID)
	.setClient(new ApacheClient())
	.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
	.create(GiftServiceApi.class);
	
	/*
	private static GiftServiceApi giftService = new RestAdapter.Builder()
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(GiftServiceApi.class);
	*/
	
	
	@BeforeClass
	public static void prepareClassTests() {
		// Remove all gifts from test database
		Collection<Gift> gifts = null;
		do {
			gifts = giftService.getAll(0, 20);
			
			for(Gift gift : gifts) {
				giftService.removeGift(gift.getId());
			}
		} while (gifts.size() > 0);
	}
	
	private Collection<Gift> createTestGifts() throws Exception {
		Collection<Gift> newGifts = new ArrayList<Gift>();
		
		// Add gifts
		Collection<Gift> testGifts = GiftTest.getTestGifts();
		for(Gift gift : testGifts) {
			Gift newGift = giftService.addGift(gift);
			newGift.setImageContentType(gift.getImageContentType());
			newGift.setImagePath(gift.getImagePath());
			newGifts.add(newGift);
		}
		
		// Add related gifts
		Collection<Gift> testRelatedGifts = GiftTest.getRelatedTestGifts(newGifts);
		for(Gift gift : testRelatedGifts) {
			Gift newGift = giftService.addGift(gift);
			newGift.setImageContentType(gift.getImageContentType());
			newGift.setImagePath(gift.getImagePath());
			newGifts.add(giftService.addGift(newGift));
		}
		
		return newGifts;
	}
	
	private void removeTestGifts(Collection<Gift> testGifts) {
		for(Gift gift : testGifts) {
			giftService.removeGift(gift.getId());
		}		
	}
	
	/**
	 * This test creates a Gift, adds the Gift to the GiftService, and then
	 * checks that the Gift is included in the list when getGiftList() is
	 * called.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGiftAddAndGet() throws Exception {
		Gift gift = GiftTest.randomGift();
		
		// Add the gift
		Gift newGift = giftService.addGift(gift);
		assertNotNull(newGift);
		assertNotNull(newGift.getId());

		// We should get back the gift that we added above
		Gift giftRecovered = giftService.findById(newGift.getId());
		assertTrue(newGift.equals(giftRecovered));
		
		// Remove the gift
		giftService.removeGift(newGift.getId());
	}
	
	@Test
	public void testGiftAddAndList() throws Exception {
		Gift gift = GiftTest.randomGift();
		
		// Add the gift
		Gift newGift = giftService.addGift(gift);
		assertNotNull(newGift);
		assertNotNull(newGift.getId());

		// We should get back the gift that we added above
		Collection<Gift> gifts = giftService.getAll(0, 20);
		assertTrue(gifts.contains(gift));
		
		// Remove the gift
		giftService.removeGift(newGift.getId());
	}
	
	@Test
	public void testGiftAddAndRemove() throws Exception {
		Gift gift = GiftTest.randomGift();
		
		// Add the gift
		Gift newGift = giftService.addGift(gift);
		
		// Remove the gift
		giftService.removeGift(newGift.getId());
		
		// We should not get back the gift that we added and removed above
		Collection<Gift> gifts = giftService.getAll(0, 20);
		assertFalse(gifts.contains(gift));
	}
	
	/*
	@Test
	public void testGiftsSearchByTitle() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();
		
		// Get beach gift
		Gift beachGift = (Gift)GiftTest.filterByTitle(testGifts, "Beach").toArray()[0];
		
		// Test for the beach gift search
		Collection<Gift> gifts = giftService.findByTitle("Beach", 0, 20);
		assertTrue(gifts.contains(beachGift));
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
	*/
	
	@Test
	public void testGiftsSearchByTitlePart() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();
		
		String[] titleParts = {"each", "", "Montain", "Lacoon"};
		
		for (int i = 0; i < titleParts.length; i++) {
			String titlePart = titleParts[i];
			
			// Recover all caption gifts with the title part
			DateUtils dateUtils = new DateUtils();
			Date minCreationTime = dateUtils.parseISO8601DateFormat("2014-10-01T00:00:00");
			Date maxCreationTime = dateUtils.parseISO8601DateFormat("2014-11-04T00:00:00");
			String minCreationTimeStr = dateUtils.convertToISO8601DateFormat(minCreationTime);
			String maxCreationTimeStr = dateUtils.convertToISO8601DateFormat(maxCreationTime);
			Collection<Gift> gifts = giftService.findAll(minCreationTimeStr, maxCreationTimeStr, titlePart, false, 0, 20);
			
			// Filter the gifts with the title part
			Collection<Gift> compareGifts = GiftTest.findBetweenCreationDates(testGifts, minCreationTime, maxCreationTime);
			compareGifts = GiftTest.filterByTitlePart(testGifts, titlePart);
			compareGifts = GiftTest.filterByCaptionGifts(compareGifts);
			compareGifts = GiftTest.sortByCreationDateDesc(compareGifts);
	
			assertTrue(GiftTest.sameList(gifts, compareGifts));
		}
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
	
	@Test
	public void testTopGiftsSearchByCreationDateRange() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();
		
		String[] minCreationTimes = {"2014-10-01T00:00:00", "2014-09-01T00:00:00", "2014-11-01T00:00:00", "2014-09-01T00:00:00"};
		String[] maxCreationTimes = {"2014-11-04T00:00:00", "2014-10-01T00:00:00", "2014-11-04T00:00:00", "2014-11-04T00:00:00"};
		
		for (int i = 0; i < minCreationTimes.length; i++) {
			String minCreationTimeStrAux = minCreationTimes[i];
			String maxCreationTimeStrAux = maxCreationTimes[i];
			
			DateUtils dateUtils = new DateUtils();
			Date minCreationTime = dateUtils.parseISO8601DateFormat(minCreationTimeStrAux);
			Date maxCreationTime = dateUtils.parseISO8601DateFormat(maxCreationTimeStrAux);
			String minCreationTimeStr = dateUtils.convertToISO8601DateFormat(minCreationTime);
			String maxCreationTimeStr = dateUtils.convertToISO8601DateFormat(maxCreationTime);
			Collection<Gift> gifts = giftService.findTopGifts(minCreationTimeStr, maxCreationTimeStr, null, false, 0, 20);
			
			Collection<Gift> compareGifts = GiftTest.findBetweenCreationDates(testGifts, minCreationTime, maxCreationTime);
			compareGifts = GiftTest.filterByCaptionGifts(compareGifts);
			compareGifts = GiftTest.sortByLikesDescAndCreationDateDesc(compareGifts);
			
			assertTrue(GiftTest.sameList(gifts, compareGifts));
		}
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
	
	@Test
	public void testGiftsSearchByCreatorLoginAndCreationDateRange() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();
		
		String[] creatorLogins = {"fcicerre", "eiodice", "mcicerre"};
		
		for (int i = 0; i < creatorLogins.length; i++) {
			String creatorLogin = creatorLogins[i];
			
			DateUtils dateUtils = new DateUtils();
			Date minCreationTime = dateUtils.parseISO8601DateFormat("2014-10-01T00:00:00");
			Date maxCreationTime = dateUtils.parseISO8601DateFormat("2014-11-04T00:00:00");
			String minCreationTimeStr = dateUtils.convertToISO8601DateFormat(minCreationTime);
			String maxCreationTimeStr = dateUtils.convertToISO8601DateFormat(maxCreationTime);
			Collection<Gift> gifts = giftService.findByCreatorLogin(creatorLogin, minCreationTimeStr, maxCreationTimeStr, null, false, 0, 20);
			
			Collection<Gift> compareGifts = GiftTest.findBetweenCreationDates(testGifts, minCreationTime, maxCreationTime);
			compareGifts = GiftTest.filterByCreatorLogin(compareGifts, creatorLogin);
			compareGifts = GiftTest.filterByCaptionGifts(compareGifts);
			compareGifts = GiftTest.sortByCreationDateDesc(compareGifts);
			
			assertTrue(GiftTest.sameList(gifts, compareGifts));
		}
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
	
	@Test
	public void testGiftsSearchByCaptionGiftIdAndCreationDateRange() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();
		
		// Obtain caption gifts ids
		ArrayList<String> testCaptionGiftIds = new ArrayList<String>();
		for (Gift gift : testGifts) {
			if (gift.getCaptionGiftId() == null)
				testCaptionGiftIds.add(gift.getId());
		}
		
		for (int i = 0; i < testCaptionGiftIds.size(); i++) {
			String testCaptionGiftId = testCaptionGiftIds.get(i);
			
			DateUtils dateUtils = new DateUtils();
			Date minCreationTime = dateUtils.parseISO8601DateFormat("2014-10-01T00:00:00");
			Date maxCreationTime = dateUtils.parseISO8601DateFormat("2014-11-04T00:00:00");
			String minCreationTimeStr = dateUtils.convertToISO8601DateFormat(minCreationTime);
			String maxCreationTimeStr = dateUtils.convertToISO8601DateFormat(maxCreationTime);
			Collection<Gift> gifts = giftService.findByCaptionGiftId(testCaptionGiftId, minCreationTimeStr, maxCreationTimeStr, null, false, 0, 20);
			
			Collection<Gift> compareGifts = GiftTest.findBetweenCreationDates(testGifts, minCreationTime, maxCreationTime);
			compareGifts = GiftTest.filterByCaptionGiftId(compareGifts, testCaptionGiftId);
			compareGifts = GiftTest.sortByCreationDateDesc(compareGifts);
			
			assertTrue(GiftTest.sameList(gifts, compareGifts));
		}
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
	
	@Test
	public void testGiftsImagePostAndRetrieve() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();

		Collection<Gift> subsetTestGifts = GiftTest.filterByTitlePart(testGifts, GiftTest.BEACH_TITLE_PART);
		
		for(Gift testGift : subsetTestGifts) {
			String imageUrl = testGift.getImagePath();
			
			File originalImageFile = new File(imageUrl);
			if (!originalImageFile.exists())
				throw new Exception("There is no " + imageUrl + " file for test");
			
			// Post the file
			TypedFile typedImageFile = new TypedFile(testGift.getImageContentType(), originalImageFile);
			OperationResult result = giftService.postGiftImage(testGift.getId(), typedImageFile);
			assertEquals(result.getResult(), OperationResultState.SUCCEEDED);
			
			InputStream retrievedInputStream = null;
			try {
				Response response = giftService.getGiftImage(testGift.getId(), GiftImageType.NORMAL);
				assertEquals(response.getStatus(), 200);
				retrievedInputStream = response.getBody().in();
				
				byte[] originalFile = IOUtils.toByteArray(new FileInputStream(originalImageFile));
				byte[] retrievedFile = IOUtils.toByteArray(retrievedInputStream);
				assertTrue(Arrays.equals(originalFile, retrievedFile));
			} finally {
				if (retrievedInputStream != null)
					retrievedInputStream.close();
			}
		}
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
	
	@Test
	public void testGiftUpdate() throws Exception {
		// Add gifts
		Collection<Gift> testGifts = createTestGifts();

		Collection<Gift> subsetTestGifts = GiftTest.filterByTitlePart(testGifts, GiftTest.TAQUARAL_TITLE_PART);
		
		for(Gift testGift : subsetTestGifts) {
			testGift.setImageContentType(GiftTest.JPEG_MIME_TYPE);
			testGift.setImagePath(GiftTest.getImageFilePath(GiftTest.TAQUARAL_FILE_NAME));
			
			// Post the file
			String imagePath = testGift.getImagePath();		
			File originalImageFile = new File(imagePath);
			if (!originalImageFile.exists())
				throw new Exception("There is no " + imagePath + " file for test");
			
			TypedFile typedImageFile = new TypedFile(testGift.getImageContentType(), originalImageFile);
			OperationResult result = giftService.postGiftImage(testGift.getId(), typedImageFile);
			assertEquals(result.getResult(), OperationResultState.SUCCEEDED);
			
			InputStream retrievedInputStream = null;
			try {
				Response response = giftService.getGiftImage(testGift.getId(), GiftImageType.NORMAL);
				assertEquals(response.getStatus(), 200);
				
				retrievedInputStream = response.getBody().in();				
				byte[] originalFile = IOUtils.toByteArray(new FileInputStream(originalImageFile));
				byte[] retrievedFile = IOUtils.toByteArray(retrievedInputStream);
				assertTrue(Arrays.equals(originalFile, retrievedFile));
			} finally {
				if (retrievedInputStream != null)
					retrievedInputStream.close();
			}
			
			// Update
			testGift.setTitle(GiftTest.TAQUARAL_NEW_TITLE_PART);
			testGift.setImageContentType(GiftTest.JPEG_MIME_TYPE);
			testGift.setImagePath(GiftTest.getImageFilePath(GiftTest.TAQUARAL_NEW_FILE_NAME));
			
			String id = testGift.getId();
			result = giftService.updateGift(id, testGift);
			assertEquals(result.getResult(), OperationResultState.SUCCEEDED);			
			assertEquals(testGift.getId(), id);
			
			Gift recoveredGift = giftService.findById(id);
			assertEquals(testGift.getTitle(), recoveredGift.getTitle());
			assertEquals(testGift.getId(), id);
			
			// Post the new file
			imagePath = testGift.getImagePath();		
			originalImageFile = new File(imagePath);
			if (!originalImageFile.exists())
				throw new Exception("There is no " + imagePath + " file for test");
			
			typedImageFile = new TypedFile(testGift.getImageContentType(), originalImageFile);
			result = giftService.postGiftImage(testGift.getId(), typedImageFile);
			assertEquals(result.getResult(), OperationResultState.SUCCEEDED);
			
			retrievedInputStream = null;
			try {
				Response response = giftService.getGiftImage(testGift.getId(), GiftImageType.NORMAL);
				assertEquals(response.getStatus(), 200);
				
				retrievedInputStream = response.getBody().in();				
				byte[] originalFile = IOUtils.toByteArray(new FileInputStream(originalImageFile));
				byte[] retrievedFile = IOUtils.toByteArray(retrievedInputStream);
				assertTrue(Arrays.equals(originalFile, retrievedFile));
			} finally {
				if (retrievedInputStream != null)
					retrievedInputStream.close();
			}
		}
		
		// Remove gifts
		removeTestGifts(testGifts);
	}
}