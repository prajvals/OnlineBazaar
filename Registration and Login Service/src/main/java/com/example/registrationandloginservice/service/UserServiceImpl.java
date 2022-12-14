package com.example.registrationandloginservice.service;

import com.example.registrationandloginservice.Entity.Users;
import com.example.registrationandloginservice.Entity.VerificationToken;
import com.example.registrationandloginservice.Enums.VerificationEnums;
import com.example.registrationandloginservice.Repository.UserRepository;
import com.example.registrationandloginservice.Repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    EmailSenderService emailSenderService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VerificationTokenRepository verificationTokenRepository;
    @Override
    public List<Users> registerUser(Users users) {
        System.out.println("entered tye service");
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users.setConfirmPassword(passwordEncoder.encode(users.getConfirmPassword()));
        System.out.println("users = " + users);
        userRepository.save(users);
        return userRepository.findAll();
    }
    @Override
    public List<Users> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public void saveVerificationToken(String token, Users users) {
        VerificationToken verificationToken = new VerificationToken(users,token);
        verificationTokenRepository.save(verificationToken);
    }
    @Override
    public void configureAndSendMail(String token, String url) {

        String bodyMessage = "Hello, please verify the email to use the service";
        emailSenderService.sendSimpleEmail("singhprajval91@gmail.com",bodyMessage + "the token is " + token + "and the url used is " + url,"Token for password Verification");
    }

    @Override
    public String verifyTokenForRegistration(String token) {

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if(verificationToken == null)
        {
            return "User doesnt exsist, Token Cannot be verified";
        }
        Users users = verificationToken.getUsers();
        Calendar calender = Calendar.getInstance();
        if(verificationToken.getExpirationDate().getTime() - calender.getTime().getTime() <=0 )
        {
            verificationTokenRepository.delete(verificationToken);
            userRepository.delete(users);
            return "Token expired, User deleted";
        }
        users.setEnabled(true);
        return "Token Valid, User enabled";
    }

    @Override
    public String getVerificationToken(Users users) {
        VerificationToken verificationToken = verificationTokenRepository.findByUsers(users);
        return verificationToken.getToken();
    }

    @Override
    public Users getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public String generateURL(String applicationUrl, String token, VerificationEnums action) {
        String url = applicationUrl +"/" + action.getAction() + "?token=" + token;
        return url;

    }

    @Override
    public Boolean verifyTokenForForgetPassword(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return false;
        }
        return true;

    }

    @Override
    public String updatePassword(String newPassword, String token) {
        Users users = verificationTokenRepository.findByToken(token).getUsers();
        users.setPassword(passwordEncoder.encode(newPassword));
        users.setConfirmPassword(passwordEncoder.encode(newPassword));
        return "Password changed Successfully";

    }
}
