package org.egov.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.domain.exception.*;
import org.egov.domain.model.OtpRequest;
import org.egov.domain.model.User;
import org.egov.persistence.repository.OtpEmailRepository;
import org.egov.persistence.repository.OtpRepository;
import org.egov.persistence.repository.OtpSMSRepository;
import org.egov.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class OtpService {

    private OtpRepository otpRepository;
    private OtpSMSRepository otpSMSSender;
    private OtpEmailRepository otpEmailRepository;
    private UserRepository userRepository;

    @Autowired
    public OtpService(OtpRepository otpRepository, OtpSMSRepository otpSMSSender, OtpEmailRepository otpEmailRepository,
                      UserRepository userRepository) {
        this.otpRepository = otpRepository;
        this.otpSMSSender = otpSMSSender;
        this.otpEmailRepository = otpEmailRepository;
        this.userRepository = userRepository;
    }

    public void sendOtp(OtpRequest otpRequest) {
        otpRequest.validate();
        if (otpRequest.isRegistrationRequestType() || otpRequest.isLoginRequestType()) {
            sendOtpForUserRegistration(otpRequest);
        } else {
            sendOtpForPasswordReset(otpRequest);
        }
    }

    private void sendOtpForUserRegistration(OtpRequest otpRequest) {
        final User matchingUser = userRepository.fetchUser(otpRequest.getMobileNumber(), otpRequest.getTenantId(),
                otpRequest.getUserType());

        if (otpRequest.isRegistrationRequestType() && null != matchingUser)
            throw new UserAlreadyExistInSystemException();
        else if (otpRequest.isLoginRequestType() && null == matchingUser){
            if(Boolean.TRUE.equals(otpRequest.getRtpLogin())) {
                log.info("RTP Login Failed as user is not registered in the system for mobile number: " + otpRequest.getMobileNumber());
                throw new RTPNotFoundException("INVALID_RTP_LOGIN", "RTP Login Failed as user is not registered in the system");
            }
            throw new UserNotExistingInSystemException();
        }

        if(Boolean.TRUE.equals(otpRequest.getRtpLogin()) && otpRequest.isLoginRequestType() && matchingUser != null){
            List<String> roles = Arrays.asList("BPA_ARCHITECT", "BPA_RTP");

            if(CollectionUtils.isEmpty(matchingUser.getRoles()) || (matchingUser.getRoles().stream().noneMatch(role -> roles.contains(role.getCode())))){
                log.info("RTP Login Failed as user does not have required role for mobile number: " + otpRequest.getMobileNumber());
                throw new RTPNotFoundException("INVALID_RTP_LOGIN", "RTP Login Failed as user does not have required role");
            }
        }

        final String otpNumber = otpRepository.fetchOtp(otpRequest);
        otpSMSSender.send(otpRequest, otpNumber);
    }

    private void sendOtpForPasswordReset(OtpRequest otpRequest) {
        final User matchingUser = userRepository.fetchUser(otpRequest.getMobileNumber(), otpRequest.getTenantId(),
                otpRequest.getUserType());
        if (null == matchingUser) {
            throw new UserNotFoundException();
        }
        if (null == matchingUser.getMobileNumber() || matchingUser.getMobileNumber().isEmpty())
            throw new UserMobileNumberNotFoundException();
        try {
            final String otpNumber = otpRepository.fetchOtp(otpRequest);
            otpRequest.setMobileNumber(matchingUser.getMobileNumber());
            otpSMSSender.send(otpRequest, otpNumber);
            otpEmailRepository.send(matchingUser.getEmail(), otpNumber);
        } catch (Exception e) {
            log.error("Exception while fetching otp: ", e);
        }
    }

}
