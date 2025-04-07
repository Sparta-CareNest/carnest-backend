package com.carenest.business.caregiverservice.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public class PageableUtils {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 10;
	private static final String DEFAULT_SORT_DIRECTION = "desc";

	public static Pageable customPageable(Integer page, Integer size, String sortDirection, String sortProperty) {
		int currentPage = (page != null && page > 0) ? page - 1 : DEFAULT_PAGE;
		int currentSize = (size != null) ? size : DEFAULT_SIZE;
		String currentSortDirection = (sortDirection != null) ? sortDirection.toLowerCase() : DEFAULT_SORT_DIRECTION;

		Sort sort = getSort(currentSortDirection, sortProperty);
		return PageRequest.of(currentPage, currentSize, sort);
	}

	private static Sort getSort(String sortDirection, String sortProperty) {
		List<Sort.Order> orders = new ArrayList<>();

		if (!StringUtils.hasText(sortProperty)) {
			orders.add(getOrder(sortDirection, SortConstants.CREATED_AT));
			orders.add(getOrder(sortDirection, SortConstants.UPDATED_AT));
		} else {
			orders.add(getOrder(sortDirection, sortProperty));
		}
		return Sort.by(orders);
	}

	private static Sort.Order getOrder(String sortDirection, String property) {
		return DEFAULT_SORT_DIRECTION.equalsIgnoreCase(sortDirection)
			? Sort.Order.desc(property) : Sort.Order.asc(property);
	}


	public class SortConstants {
		public static final String CREATED_AT = "createdAt";
		public static final String UPDATED_AT = "updatedAt";
	}
}

