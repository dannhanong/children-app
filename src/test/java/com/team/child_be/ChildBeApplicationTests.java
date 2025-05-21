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
			fcmService.sendNotificationToUser(3L, "Thông báo", "Xin chào", "c-Wr5tJ9Rm63KNdrDRwDij:APA91bEywC7_kS1C1xQaSpjxlyYd_WtktkN1YK9p4t788wHHRY3GtM7n9Oh50d8MQ5KBy3E-AMD5MaYVZ5G3fUBlANzPTK3QBsXbF5z3ImOzTcLc_o54b5Q");
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}
	}

}
