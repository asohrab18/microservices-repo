package com.microservices.moviescatalogservice.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.microservices.moviescatalogservice.bean.CatalogItems;
import com.microservices.moviescatalogservice.bean.Movie;
import com.microservices.moviescatalogservice.bean.MovieCatalogOfUser;
import com.microservices.moviescatalogservice.bean.Rating;
import com.microservices.moviescatalogservice.bean.UserRatings;

@RestController
@RequestMapping("/catalogs")
public class MovieCatalogResource {

	@Autowired
	private RestTemplate restTemp;

	@GetMapping("/{userId}")
	public MovieCatalogOfUser getCatalogItems(@PathVariable("userId") String userId) {
		MovieCatalogOfUser movieCatalogOfUser = new MovieCatalogOfUser();
		movieCatalogOfUser.setUserId(userId);
		
		List<CatalogItems> catalogItemsList = null;
		UserRatings userRatings = restTemp.getForObject("http://ratings-data-service/ratingsdata/" + userId, UserRatings.class);
		List<Rating> ratings = userRatings.getRatings();
		if (ratings != null && !ratings.isEmpty()) {
			catalogItemsList = ratings.stream().map(rating -> {
				Movie movie = restTemp.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
				return new CatalogItems(movie.getMovieName(), movie.getDescription(), rating.getRating());
			}).collect(Collectors.toList());
		}
		movieCatalogOfUser.setCatalogItemsList(catalogItemsList);
		return movieCatalogOfUser;
	}
}
