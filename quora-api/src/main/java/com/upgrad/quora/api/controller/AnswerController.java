package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
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
public class AnswerController {

    @Autowired
    private AnswerBusinessService answerBusinessService;

    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * This method is to create an answer for the question. Login is needed in order to access this endpoint.
     *
     * @return AnswerResponse - Answer response model type
     * @throws AuthorizationFailedException - if user does not exist in db
     * @throws InvalidQuestionException - if question does not exists in db
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") final String authorization, @PathVariable("questionId") final String questionId, final AnswerRequest answerRequest)
            throws AuthorizationFailedException, InvalidQuestionException {

        final AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setAnswer(answerRequest.getAnswer());

        //Get question entity using id provided by the user
        QuestionEntity questionEntity = questionBusinessService.getQuestionEntity(questionId);
        answerEntity.setQuestion(questionEntity);

        final AnswerEntity createdAnswerEntity = answerBusinessService.createAnswer(answerEntity,authorization);
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswerEntity.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    /**
     * This method is to delete an answer. Only valid users(admin or owner) can delete the answers
     *
     * @return AnswerDeleteResponse - Answer delete model type
     * @throws AuthorizationFailedException - if user does not exist in db
     * @throws AnswerNotFoundException - if answer does not exists in db
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException {

        //Fetch answer entity from answer id
        AnswerEntity answerEntity = answerBusinessService.getAnswerbyUuid(answerId);

        //Check all validations for deleting the answer and return the entity
        AnswerEntity validatedAnswerEntity = answerBusinessService.validateAnswerEntity(answerEntity, authorization);
        //Delete answer
        AnswerEntity deletedAnswer = answerBusinessService.deleteAnswer(validatedAnswerEntity);

        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse().id(deletedAnswer.getUuid())
                .status("ANSWER DELETED");

        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(final AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerId,
                                                                @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException {

        //Fetch answer entity from answer id
        AnswerEntity answerEntity = answerBusinessService.getAnswerbyUuid(answerId);
        //Update answer entity content
        answerEntity.setAnswer(answerEditRequest.getContent());

        //Check all validations for editing an answer and return the entity
        AnswerEntity validatedAnswerEntity = answerBusinessService.validateAnswerEntity(answerEntity, authorization);
        //Persist Edit answer
        AnswerEntity editedAnswer = answerBusinessService.editAnswer(validatedAnswerEntity);

        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(editedAnswer.getUuid())
                .status("ANSWER EDITED");

        return new ResponseEntity<>(answerEditResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {

        //Get question entity using id provided by the user
        QuestionEntity questionEntity = questionBusinessService.getQuestionEntity(questionId);

        // Fetch all answers for the provided question id
        List<AnswerEntity> allAnswers  = answerBusinessService.getAllAnswersToQuestion(questionEntity , authorization);

        List<AnswerDetailsResponse> allAnswersList = new ArrayList<>(allAnswers.size());
        for (AnswerEntity answerEntity : allAnswers) {
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
            // Set answer's uuid
            answerDetailsResponse.setId(answerEntity.getUuid());
            // Set answer content
            answerDetailsResponse.setAnswerContent(answerEntity.getAnswer());
            // Set question content
            answerDetailsResponse.setQuestionContent(questionEntity.getContent());

            allAnswersList.add(answerDetailsResponse);
        }
        return new ResponseEntity<>(allAnswersList, HttpStatus.OK);

    }

}
