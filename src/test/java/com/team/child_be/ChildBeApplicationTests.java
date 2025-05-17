package com.team.child_be;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.team.child_be.services.FCMService;

@SpringBootTest
class ChildBeApplicationTests {
	@Autowired
	private FCMService fcmService;

	@Test
	void contextLoads() {
		try {
			fcmService.sendNotification("dJk5Ehf7ROi62xd_2Rdsm8:APA91bELxAoLx0gGQY9BeU1pCrtLXYWKYJ5qL2Xoyi4Ni0C-a8nP3nHRNvOyAQ1mGgoyDdW1vBeDRI-Ltls2aw84ReDOZbX09NMn7piVSIbCJsvM6pEGhOI", "Test", "HOHOOHO");
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}
	}

}
