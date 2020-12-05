package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    private QuestionDao questionDao;

    /**
     * This method persists the new question to the db
     *
     * @param questionEntity - new question which has to be persisted
     * @param s
     * @param authorization  - logged-in user
     * @return persisted new question
     * @throws AuthorizationFailedException - if the user fails to authenticate
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity,
                                         final String authorization,
                                         final String additionalErrorMsg) throws AuthorizationFailedException, AuthenticationFailedException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        questionEntity.setUser(userAuthToken.getUserEntity());
        return questionDao.createQuestion(questionEntity);
    }

    /**
     * This method fetches the question from dbm corresponding to a given id.
     *
     * @param uuid - id of the question which has to be fetched from db
     * @return - asked question
     */
    public QuestionEntity getQuestionEntity(final String uuid,
                                            final String authorization,
                                            final String additionalErrorMsg)
            throws AuthorizationFailedException, InvalidQuestionException, AuthenticationFailedException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        doesQuestionExist(uuid);
        return questionDao.getQuestionByUUId(uuid);
    }


    /**
     * Returns all questions from the database
     *
     * @param authorization - logged-in user
     * @return list of all questions
     * @throws AuthorizationFailedException - if the user is not authenticated.
     */
    public List<QuestionEntity> getAllQuestions(final String authorization,
                                                final String additionalErrorMsg) throws AuthorizationFailedException, AuthenticationFailedException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        return questionDao.getAllQuestions();
    }

    /**
     * Returns all questions from the database belonging to a particular user
     *
     * @param userId        - user for which the questions are to be listed.
     * @param authorization - logged-in user
     * @param s
     * @return list of all questions
     * @throws AuthorizationFailedException - if the user is not authenticated.
     */
    public List<QuestionEntity> getAllQuestionsByUser(final String userId,
                                                      final String authorization,
                                                      final String additionalErrorMsg) throws AuthorizationFailedException, AuthenticationFailedException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        return questionDao.getAllQuestionsByUser(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final String questionId,
                                         final String authorization,
                                         final String additionalErrorMsg) throws AuthorizationFailedException, AuthenticationFailedException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        return questionDao.deleteQuestion(questionId);
    }

    /**
     * Persist the question with new content
     *
     * @param questionEntity - question entity carrying the new content
     * @return question entity after successfully persisting
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestionContent(final QuestionEntity questionEntity) {
        return questionDao.editQuestionContent(questionEntity);
    }

    private void isUserAuthenticated(final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }

    private void isUserLoggedOut(final String authorization, final String additionalErrorMsg) throws AuthenticationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken.getLogoutAt() != null) {
            throw new AuthenticationFailedException("ATHR-002", String.format("User is signed out.%s", additionalErrorMsg));
        }
    }

    private void doesQuestionExist(final String questionId) throws InvalidQuestionException {
        QuestionEntity questionEntity = questionDao.getQuestionByUUId(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
    }

}
