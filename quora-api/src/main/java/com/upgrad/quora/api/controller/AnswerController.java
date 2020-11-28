package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AnswerController {

        @Autowired
        private AnswerBusinessService answerBusinessService;

        @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
        public ResponseEntity<AnswerResponse> createAnswer(@RequestHeader("authorization") final String authorization, @PathVariable("questionId") final String questionId, final AnswerRequest answerRequest) {

            AnswerEntity answerEntity = new AnswerEntity();

            AnswerEntity createdAnswer = answerBusinessService.createAnswer(answerEntity, userAuthEntity);
            AnswerResponse answerResponse = new AnswerResponse().id('database_uuid1').
                    status("ANSWER CREATED");

            return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
        }
}
