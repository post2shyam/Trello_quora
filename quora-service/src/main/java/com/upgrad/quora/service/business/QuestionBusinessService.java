package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
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
     * @param questionUuid - id of the question which has to be fetched from db
     * @return - asked question
     */
    public QuestionEntity getQuestionEntity(final String questionUuid,
                                            final String authorization,
                                            final String additionalErrorMsg)
            throws AuthorizationFailedException, InvalidQuestionException, AuthenticationFailedException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        doesQuestionExist(questionUuid);
        return questionDao.getQuestionByUUId(questionUuid);
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
                                                      final String additionalErrorMsg) throws AuthorizationFailedException, AuthenticationFailedException, UserNotFoundException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        final List<QuestionEntity> allQuestionsByUser = questionDao.getAllQuestionsByUser(userId);
        if (0 == allQuestionsByUser.size()) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return allQuestionsByUser;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final String questionId,
                                         final String authorization,
                                         final String additionalErrorMsg) throws AuthorizationFailedException, AuthenticationFailedException, InvalidQuestionException {
        isUserAuthenticated(authorization);
        isUserLoggedOut(authorization, additionalErrorMsg);
        doesQuestionExist(questionId);
        final QuestionEntity questionEntity = questionDao.getQuestionByUUId(questionId);
        isUserOwnerOrAdmin(authorization, questionEntity, additionalErrorMsg);
        return questionDao.deleteQuestion(questionId);
    }

    /**
     * Persist the question with new content
     *
     * @param authorization
     * @param questionEntity - question entity carrying the new content
     * @return question entity after successfully persisting
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestionContent(final String authorization, final QuestionEntity questionEntity) throws AuthorizationFailedException {
        final UserAuthEntity userAuthEntity = questionDao.getUserAuthToken(authorization);
        final UserEntity userEntity = userAuthEntity.getUserEntity();
        final boolean isLoggedInUserSameToQuestionUser = userEntity.getUuid().equals(questionEntity.getUser().getUuid());

        //Is the logged in user owner of question
        if (!isLoggedInUserSameToQuestionUser) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
        }

        return questionDao.editQuestionContent(questionEntity);
    }

    /**
     * Method checks if user is authenticated
     *
     * @param authorization
     * @throws AuthorizationFailedException
     */
    private void isUserAuthenticated(final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }

    /**
     * Checks if user has been logged out
     *
     * @param authorization
     * @param additionalErrorMsg
     * @throws AuthenticationFailedException
     */
    private void isUserLoggedOut(final String authorization, final String additionalErrorMsg) throws AuthenticationFailedException {
        UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        if (userAuthToken.getLogoutAt() != null) {
            throw new AuthenticationFailedException("ATHR-002", String.format("User is signed out.%s", additionalErrorMsg));
        }
    }

    /**
     * Check if user is owner or admin
     * @param authorization
     * @param questionEntity
     * @param additionalErrorMsg
     * @throws AuthenticationFailedException
     */
    private void isUserOwnerOrAdmin(final String authorization,
                                    final QuestionEntity questionEntity,
                                    final String additionalErrorMsg) throws AuthenticationFailedException {
        final UserAuthEntity userAuthToken = questionDao.getUserAuthToken(authorization);
        final UserEntity signedInUser = userAuthToken.getUserEntity();
        final UserEntity questionUser = questionEntity.getUser();

        //Is the signed in user and question user are the same
        final boolean isSignedInUserOwnerOfQuestion = signedInUser.getUuid().equals(questionUser.getUuid());

        //Is the loggedin user admin
        final boolean isSignedInUserAdmin = signedInUser.getRole().equals("admin");

        //Neither the owner of question, nor admin then throw exception
        if (!isSignedInUserOwnerOfQuestion && !isSignedInUserAdmin) {
            throw new AuthenticationFailedException("ATHR-003", String.format("%s", additionalErrorMsg));
        }
    }

    /**
     * Check if question exists
     * @param questionId
     * @throws InvalidQuestionException
     */
    private void doesQuestionExist(final String questionId) throws InvalidQuestionException {
        final QuestionEntity questionEntity = questionDao.getQuestionByUUId(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
    }
}
