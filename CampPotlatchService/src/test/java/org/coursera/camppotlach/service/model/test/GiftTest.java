package org.coursera.camppotlach.service.model.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.coursera.camppotlatch.service.commons.DateUtils;
import org.coursera.camppotlatch.service.model.Gift;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

public class GiftTest {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final String RESOURCES_PATH = "C:\\projetos\\eclipse\\CampPotlachService\\src\\test\\resources\\";
	
	public static final String JPEG_MIME_TYPE = "image/jpeg";
	
	public static final String BEACH_TITLE_PART = "Beach";
	
	public static final String TAQUARAL_TITLE_PART = "Taquaral";
	public static final String TAQUARAL_NEW_TITLE_PART = "Takuaral";
	
	public static final String TAQUARAL_FILE_NAME = "Taquaral Lagoon.jpg";
	public static final String TAQUARAL_NEW_FILE_NAME = "Accoustic Shell in Taquaral Lagoon.jpg";
	
	/**
	 * Construct and return a Gift object with a
	 * random title, url and comments.
	 * 
	 * @return
	 */
	public static Gift randomGift() throws Exception {
		DateUtils dateUtils = new DateUtils();
		
		// Information about the gift
		// Construct a random identifier using Java's UUID class
		String id = UUID.randomUUID().toString();
		String title = "Gift-"+id;
		//String imageContentType = null;
		//String imagePath = null;
		String imageId = UUID.randomUUID().toString();
		String comments = "Gift-comments-"+id;
		String creatorLogin = "user-" + id;
		String creatorName = "name-" + id;
		//String createTime = dateUtils.convertToISO8601DateFormat(new Date());
		Date createTime = new Date();
		String createTimeStr = dateUtils.convertToISO8601DateFormat(createTime);
		// Date with seconds precision
		Date createTimeNorm = dateUtils.parseISO8601DateFormat(createTimeStr);
		Integer likesCount = Math.round((float)Math.random() * 50.0F);
		Integer likeFlag = (((int)Math.round((float)Math.random() * 50.0F)) % 5 > 3) ? 1 : 0;
		Integer inappropriateCount = Math.round((float)Math.random() * 10.0F);
		Integer inappropriateFlag = (((int)Math.round((float)Math.random() * 50.0F)) % 10 > 8) ? 1 : 0;
		Integer relatedCount = 0;
		String captionGiftId = null;
		return new Gift(title, imageId, comments, creatorLogin, creatorName, createTimeNorm,
				likesCount, likeFlag, inappropriateCount, inappropriateFlag, relatedCount, captionGiftId);
	}
	
	public static Collection<Gift> getTestGifts() throws Exception {
		DateUtils dateUtils = new DateUtils();
		Collection<Gift> gifts = new ArrayList<Gift>(); 

		String[] titles = {"Beach", "Montain", "Fields", "Taquaral Lagoon", "Park of the Waters"};
		String[] imageFileNames = {"beach.jpg", "mountain.jpg", "fields.jpg", 
				"Taquaral Lagoon.jpg", "Park of the Waters.jpg"};
		String[] creatorLogins = {"fcicerre", "eiodice", "nlima", "fcicerre", "mrcicerre"};
		String[] creatorNames = {"Fibrio Cicerre", "Elita Iodice", "Nilon Lima", "Fibrio Cicerre", "Marto Cicerre"};
		Date[] createTimes = {dateUtils.parseISO8601DateFormat("2014-11-01T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-11-02T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-09-20T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-11-04T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-10-15T00:00:00")};
		for (int i = 0; i < titles.length; i++) {
			Gift gift = randomGift();
			gift.setTitle(titles[i]);
			gift.setImageContentType(JPEG_MIME_TYPE);
			gift.setImagePath(getImageFilePath(imageFileNames[i]));
			gift.setCreatorLogin(creatorLogins[i]);
			gift.setCreatorName(creatorNames[i]);
			gift.setCreateTime(createTimes[i]);
			gifts.add(gift);
		}
		
		return gifts;
	}
	
	public static Collection<Gift> getRelatedTestGifts(Collection<Gift> gifts) throws Exception {
		DateUtils dateUtils = new DateUtils();
		Collection<Gift> relatedGifts = new ArrayList<Gift>(); 

		String[] captionGiftTitles = {"Beach", "Fields", "Taquaral Lagoon"};
		String[] captionGiftCreatorLogins = {"fcicerre", "nlima", "fcicerre"};
		Date[] captionGiftCreateTimes = {dateUtils.parseISO8601DateFormat("2014-11-01T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-09-20T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-11-04T00:00:00")};
		
		String[] titles = {"Beach Related", "Fields Related", "Taquaral Lagoon Related"};
		String[] imageFileNames = {"Beach of Carneiros.jpg", "Alone-in-the-fields.jpg", 
				"Accoustic Shell in Taquaral Lagoon.jpg"};
		String[] creatorLogins = {"eiodice", "fcicerre", "mcicerre"};
		String[] creatorNames = {"Elita Iodice", "Fibrio Cicerre", "Marto Cicerre"};
		Date[] createTimes = {dateUtils.parseISO8601DateFormat("2014-11-04T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-10-02T00:00:00"),
				dateUtils.parseISO8601DateFormat("2014-11-05T00:00:00")};
		
		for (int i = 0; i < captionGiftTitles.length; i++) {
			Gift captionGift = findFirstByTitleCreatorLoginCreationTime(gifts, captionGiftTitles[i], captionGiftCreatorLogins[i], captionGiftCreateTimes[i]);
			if (captionGift == null)
				throw new Exception("Test exception: missing caption gift to create related gift");
			if (captionGift.getId() == null)
				throw new Exception("Test exception: missing caption gift id to create related gift");
			Gift relatedGift = randomGift();
			relatedGift.setTitle(titles[i]);
			relatedGift.setImageContentType(JPEG_MIME_TYPE);
			relatedGift.setImagePath(getImageFilePath(imageFileNames[i]));
			relatedGift.setCreatorLogin(creatorLogins[i]);
			relatedGift.setCreatorName(creatorNames[i]);
			relatedGift.setCreateTime(createTimes[i]);
			relatedGift.setCaptionGiftId(captionGift.getId());
			
			captionGift.setRelatedCount(captionGift.getRelatedCount() + 1);
		}
		
		return relatedGifts;
	}
	
	public static String getImageFilePath(String fileName) {
		return RESOURCES_PATH + fileName;
	}
	
	/**
	 *  Convert an object to JSON using Jackson's ObjectMapper
	 *  
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static String toJson(Object o) throws Exception{
		return objectMapper.writeValueAsString(o);
	}
	
	public static Gift findFirstByTitleCreatorLoginCreationTime(
			Collection<Gift> gifts,
			String title,
			String creatorLogin,
			Date createTime) {
		for(Gift gift : gifts) {
			if (gift.getTitle().equals(title) && 
					gift.getCreatorLogin().equals(creatorLogin) && 
					gift.getCreateTime().equals(createTime))
				return gift;
		}
		
		return null;
	}
	
	public static Collection<Gift> findBetweenCreationDates(Collection<Gift> gifts, Date minDate, Date maxDate) {
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : gifts) {
			if (gift.getCreateTime().compareTo(minDate) >= 0 && gift.getCreateTime().compareTo(maxDate) <= 0)
				result.add(gift);
		}
		
		return result;
	}
	
	public static Collection<Gift> filterByTitle(Collection<Gift> gifts, String title) {
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : gifts) {
			if (gift.getTitle().equals(title))
				result.add(gift);
		}
		
		return result;		
	}
	
	public static Collection<Gift> filterByTitlePart(Collection<Gift> gifts, String titlePart) {
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : gifts) {
			if (gift.getTitle().contains(titlePart))
				result.add(gift);
		}
		
		return result;		
	}
	
	public static Collection<Gift> filterByCreatorLogin(Collection<Gift> gifts, String creatorLogin) {
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : gifts) {
			if (gift.getCreatorLogin().equals(creatorLogin))
				result.add(gift);
		}
		
		return result;		
	}
	
	public static Collection<Gift> filterByCaptionGifts(Collection<Gift> gifts) {
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : gifts) {
			if (gift.getCaptionGiftId() == null)
				result.add(gift);
		}
		
		return result;		
	}
	
	public static Collection<Gift> filterByCaptionGiftId(Collection<Gift> gifts, String captionGiftId) {
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : gifts) {
			if (gift.getCaptionGiftId() != null && gift.getCaptionGiftId().equals(captionGiftId))
				result.add(gift);
		}
		
		return result;		
	}
	
	public static Collection<Gift> sortByCreationDateDesc(Collection<Gift> gifts) {
        final Comparator<Date> dateComparatorDesc = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return -o1.compareTo(o2);
			}
        };
        SortedMap<Date, Gift> sortedGifts = new TreeMap<Date, Gift>(dateComparatorDesc);
        for(Gift gift : gifts) {
        	sortedGifts.put(gift.getCreateTime(), gift);
        }
        
        return sortedGifts.values();
	}
	
	
	// Likes and creation date class
	private static class LikesCreationDate implements Comparable<LikesCreationDate> {
		public Integer likes;
		public Date creationDate;
		public LikesCreationDate(Integer likes, Date creationDate) {
			this.likes = likes;
			this.creationDate = creationDate;
		}

		@Override
		public int hashCode() {
			// Google Guava provides great utilities for hashing
			return Objects.hashCode(likes, creationDate);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LikesCreationDate) {
				LikesCreationDate other = (LikesCreationDate) obj;
				// Google Guava provides great utilities for equals too!
				return Objects.equal(likes, other.likes)
						&& Objects.equal(creationDate, other.creationDate);
			} else {
				return false;
			}
		}

		@Override
		public int compareTo(LikesCreationDate o) {
			if (likes != o.likes)
				return (likes < o.likes) ? -1 : 1;
			else if (!creationDate.equals(o.creationDate))
				return creationDate.compareTo(o.creationDate);
			else
				return 0;
		}	
	}
	
	public static Collection<Gift> sortByLikesDescAndCreationDateDesc(Collection<Gift> gifts) {
        final Comparator<LikesCreationDate> dateLikesComparatorDesc = new Comparator<LikesCreationDate>() {
			@Override
			public int compare(LikesCreationDate o1, LikesCreationDate o2) {
				return -o1.compareTo(o2);
			}
        };
        SortedMap<LikesCreationDate, Gift> sortedGifts = new TreeMap<LikesCreationDate, Gift>(dateLikesComparatorDesc);
        for(Gift gift : gifts) {
        	sortedGifts.put(new LikesCreationDate(gift.getLikesCount(), gift.getCreateTime()), gift);
        }
        
        return sortedGifts.values();
	}
	
	public static boolean sameCollection(Collection<Gift> gifts1, Collection<Gift> gifts2) {
		if (gifts1 == null && gifts2 == null)
			return true;
		if (gifts1 == null || gifts2 == null)
			return false;
			
		return gifts1.containsAll(gifts2) && gifts2.containsAll(gifts1);
	}
	
	public static boolean sameList(Collection<Gift> gifts1, Collection<Gift> gifts2) {
		if (gifts1 == null && gifts2 == null)
			return true;
		if (gifts1 == null || gifts2 == null)
			return false;
		
		if (gifts1.size() != gifts1.size())
			return false;
		
		Iterator<Gift> iGift1 = gifts1.iterator();
		Iterator<Gift> iGift2 = gifts2.iterator();
		while (iGift1.hasNext()) {
			Gift gift1 = iGift1.next();
			Gift gift2 = iGift2.next();
			if (!gift1.equals(gift2))
				return false;
		}
		
		return true;
	}
}
