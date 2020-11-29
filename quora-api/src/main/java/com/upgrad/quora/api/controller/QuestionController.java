package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
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

    @RequestMapping(method = RequestMethod.POST, path = "/question/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,
                                                           @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        final QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setContent(questionRequest.getContent());

        final QuestionEntity createdQuestionEntity = questionBusinessService.createQuestion(questionEntity, authorization);
        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("Question created successfully");
        return new ResponseEntity<>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        final List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestions(authorization);
        return prepareQuestionDetailResponse(allQuestions);
    }

    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@PathVariable("userId") final String userId,
                                                                               @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        final List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestionsByUser(userId, authorization);
        return prepareQuestionDetailResponse(allQuestions);
    }

    private ResponseEntity<List<QuestionDetailsResponse>> prepareQuestionDetailResponse(final List<QuestionEntity> allQuestions) {
        List<QuestionDetailsResponse> allQuestionsRsp = new ArrayList<>(allQuestions.size());
        for (QuestionEntity quesEntity : allQuestions) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.setId(quesEntity.getUuid());
            questionDetailsResponse.setContent(quesEntity.getContent());

            allQuestionsRsp.add(questionDetailsResponse);
        }
        return new ResponseEntity<>(allQuestionsRsp, HttpStatus.OK);
    }
}
