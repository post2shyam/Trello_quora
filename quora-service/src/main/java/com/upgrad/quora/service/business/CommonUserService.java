package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import java.time.ZonedDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommonUserService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserAuthDao userAuthDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity getUserProfile(final String userUuid, final String authorizationToken)
            throws AuthorizationFailedException,
            UserNotFoundException {

        UserAuthEntity userAuthEntity = userAuthDao.getUserAuthByToken(authorizationToken);
        UserEntity userEntity = userDao.getUserById(userUuid);
        if(userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in.");
        }else if (userAuthEntity.getLogoutAt() != null || userAuthEntity.getExpiresAt()
                .isAfter(ZonedDateTime.now())) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out. Sign in first to get user details.");
        } else if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        } else {
            return userEntity;
        }
    }
}