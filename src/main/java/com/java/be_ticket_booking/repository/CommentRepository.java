package com.java.be_ticket_booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.be_ticket_booking.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, String>{
	List<Comment> findAllByUserId(String user_id);
	
	@Query("SELECT c FROM Comment c WHERE c.user.username=:username ORDER BY DATE(c.update_at) ASC")
	List<Comment> findAllByUsername(@Param("username") String username);
	
	List<Comment> findAllByMovieId(long movie_id);
	
	boolean existsByUserIdAndMovieId(String user_id, long movie_id);
}
