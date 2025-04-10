package com.java.be_ticket_booking.service;

import java.util.List;

import com.java.be_ticket_booking.request.AddCommentRequest;
import com.java.be_ticket_booking.request.EditCommentRequest;
import com.java.be_ticket_booking.response.CommentResponse;
import com.java.be_ticket_booking.response.MyApiResponse;

public interface CommentService {
	public CommentResponse addComment(String username, AddCommentRequest req);
	public CommentResponse addListComments(String username, List<AddCommentRequest> req);
	public CommentResponse editComment(String username, String comment_id, EditCommentRequest req);
	
	public MyApiResponse addLike(String username, long movie_id);
	public MyApiResponse addDisLike(String username, long movie_id);
	
	public MyApiResponse deleteCommentByUsername(String username, String comment_id);
	public MyApiResponse deleteCommentById(String comment_id);
	
	public CommentResponse getOne(String username, String comment_id);
	public List<CommentResponse> getAllComments();
	public List<CommentResponse> getAllCommentsFromMovieId(long movie_id);
	public List<CommentResponse> getAllCommentsFromUserId(String user_id);
	public List<CommentResponse> getAllCommentsFromusername(String username);
}
