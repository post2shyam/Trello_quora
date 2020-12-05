package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * This method is to create an question for user. Login is needed in order to access this endpoint.
     *
     * @param questionRequest - question for which answer is seeked
     * @param authorization   - logged in user
     * @return Answer to the question
     * @throws AuthorizationFailedException - if the user fails to authenticate
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,
                                                           @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AuthenticationFailedException {
        final QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setContent(questionRequest.getContent());

        final QuestionEntity createdQuestionEntity = questionBusinessService.createQuestion(questionEntity, authorization, "Sign in first to post a question");
        final QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("Question created successfully");
        return new ResponseEntity<>(questionResponse, HttpStatus.CREATED);
    }

    /**
     * Returns all questions of the database
     *
     * @param authorization - logged in user
     * @return - all the questions in the database
     * @throws AuthorizationFailedException - if the user is not authenticated
     */
    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AuthenticationFailedException {
        final List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestions(authorization, "Sign in first to get all questions");
        return prepareQuestionDetailResponse(allQuestions);
    }

    /**
     * Return all questions belonging to a particular user
     *
     * @param userId        - userId of the user whose question list is to be fetched
     * @param authorization - logged in user
     * @return - list of questions belonging to asked user
     * @throws AuthorizationFailedException - if the user is not authenticated
     */
    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@PathVariable("userId") final String userId,
                                                                               @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AuthenticationFailedException {
        final List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestionsByUser(userId, authorization, "Sign in first to get all questions posted by a specific user");
        return prepareQuestionDetailResponse(allQuestions);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@PathVariable("questionId") final String questionId,
                                                                 @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AuthenticationFailedException {
        final QuestionEntity questionEntity = questionBusinessService.deleteQuestion(questionId, authorization, "Sign in first to delete a question");
        final QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse().id(questionEntity.getUuid()).status("Question deleted successfully");
        return new ResponseEntity<>(questionDeleteResponse, HttpStatus.NO_CONTENT);
    }


    /**
     * To edit content of an existing question
     *
     * @param questionId          - question id of the question to be edited
     * @param questionEditRequest - carries the new content
     * @param authorization       - logged-in user
     * @return
     * @throws AuthorizationFailedException - if the user is not logged-in
     * @throws InvalidQuestionException     - if the question dont exist
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@PathVariable("questionId") final String questionId,
                                                                    final QuestionEditRequest questionEditRequest,
                                                                    @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException, AuthenticationFailedException {
        //Fetch the existing question
        final QuestionEntity questionEntity = questionBusinessService.getQuestionEntity(questionId, authorization, "Sign in first to edit the question");

        //Update the contents
        questionEntity.setContent(questionEditRequest.getContent());
        questionBusinessService.editQuestionContent(questionEntity);

        //Prepare the HTTP response and return
        final QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(questionEntity.getUuid()).status("Question updated successfully");
        return new ResponseEntity<>(questionEditResponse, HttpStatus.OK);
    }

    private ResponseEntity<List<QuestionDetailsResponse>> prepareQuestionDetailResponse(final List<QuestionEntity> allQuestions) {
        final List<QuestionDetailsResponse> allQuestionsRsp = new ArrayList<>(allQuestions.size());
        for (QuestionEntity quesEntity : allQuestions) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.setId(quesEntity.getUuid());
            questionDetailsResponse.setContent(quesEntity.getContent());

            allQuestionsRsp.add(questionDetailsResponse);
        }
        return new ResponseEntity<>(allQuestionsRsp, HttpStatus.OK);
    }
}
