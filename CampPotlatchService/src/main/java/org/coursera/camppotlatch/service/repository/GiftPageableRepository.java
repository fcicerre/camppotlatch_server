package org.coursera.camppotlatch.service.repository;

import org.coursera.camppotlatch.service.model.Gift;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@EnableScan
public interface GiftPageableRepository extends PagingAndSortingRepository<Gift, String> {
	
	// Find all gifts with a matching title
	// public Page<Gift> findByTitle(String title, Pageable page);
	
	// Find all gifts with the title matching an expression (not supported by DynamoDB)
	// public Page<Gift> findByTitleLike(String titleLikeExpression, Pageable page);
}
