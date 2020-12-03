package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    /**
     * @param answerEntity
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(final AnswerEntity answerEntity, final String authorization) throws InvalidQuestionException {
        if (answerEntity.getQuestion() == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        final UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        answerEntity.setUser(userAuthToken.getUserEntity());
        return answerDao.createAnswer(answerEntity);
    }

    /**
     * @param uuid
     * @return
     */
    public AnswerEntity getAnswerbyUuid(final String uuid, final String authorization)
            throws AnswerNotFoundException, AuthorizationFailedException, AuthenticationFailedException {

        //check if user has not signed in
        isUserAuthenticated(authorization);

        //check if user is logged out
        isUserLoggedOut(authorization);

        //Fetch answer entity from answer id
        AnswerEntity answerEntity = answerDao.getAnswerByUuId(uuid);

        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        return answerDao.getAnswerByUuId(uuid);
    }

    /**
     * @param answerEntity
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final AnswerEntity answerEntity) {
        AnswerEntity deletedAnswer = answerDao.deleteAnswer(answerEntity);
        return deletedAnswer;
    }

    /**
     * @param answerEntity
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswer(final AnswerEntity answerEntity) {
        return answerDao.editAnswer(answerEntity);
    }

    /**
     * @param questionEntity
     *
     * @return
     * @throws AuthorizationFailedException
     */
    public List<AnswerEntity> getAllAnswersToQuestion(final QuestionEntity questionEntity) {
        return answerDao.getAllAnswersToQuestion(questionEntity.getUuid());
    }

    /**
     * @param answerId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    public AnswerEntity validateAnswerToEdit(final String answerId, final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException, AuthenticationFailedException {

        //Get answer by uuid. It throws exception if answer does not exists
        AnswerEntity answerEntity = getAnswerbyUuid(answerId, authorization);

        //Check if user is owner of the answer
        final Boolean isUserOwnerOfAnswer = isUserAnswerOwner(authorization, answerEntity);

        //If user is not the owner of the answer, will throw an exception
        if (!isUserOwnerOfAnswer) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }
        return answerEntity;
    }

    /**
     * @param answerId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    public AnswerEntity validateAnswerToDelete(final String answerId, final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException, AuthenticationFailedException {

        // Fetch answer entity
        AnswerEntity answerEntity = getAnswerbyUuid(answerId, authorization);

        //Check if user is owner of the answer
        final Boolean isUserOwnerOfAnswer = isUserAnswerOwner(authorization, answerEntity);

        //Check if user is admin
        final Boolean isUserAdmin = isUserAdmin(authorization);

        if (!isUserAdmin && !isUserOwnerOfAnswer) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
        }
        return answerEntity;
    }

    private void checkBasicAnswerValidation() {

    }

    private void isUserAuthenticated(final String authorization) throws AuthorizationFailedException {
        UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }

    private void isUserLoggedOut(final String authorization) throws AuthenticationFailedException {
        UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken.getLogoutAt() != null) {
            throw new AuthenticationFailedException("ATHR-002", "User is signed out.Sign in first to delete an answer");
        }
    }

    private boolean isUserAdmin(final String authorization) {
        UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken.getUserEntity().getRole() == "admin") {
          return true;
        }
        return false;
    }

    private boolean isUserAnswerOwner(final String authorization, final AnswerEntity answerEntity) throws AuthenticationFailedException {
        UserAuthEntity userAuthToken = answerDao.getUserAuthToken(authorization);
        if (userAuthToken.getUserEntity().getUuid() == answerEntity.getUser().getUuid()) {
            return true;
        }
        return false;
    }
}
