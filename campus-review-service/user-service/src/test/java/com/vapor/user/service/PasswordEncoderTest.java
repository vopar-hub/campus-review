package com.vapor.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class PasswordEncoderTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordEncoder() {
        System.out.println(passwordEncoder.encode("admin123"));
//                     $2a$10$Gd.NHmHNCBc.StCpVKxKze1NZYbw3pIzR0ZETQ8Nrmygsr9eVoT6W
    }

}