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
		// try {
		// 	fcmService.sendNotification("dmCEeAIiQlmR6GFf8w8q7i:APA91bE8hhokF7rRMUMTuk0LndvRw9MDiq-wEhmZFtHnAQOYX-IRMfklbobfGcO_aPv8iPsWviU5XO-SGghV7F7L8puhV3tVguGQPJpr5RZwpxO46Zx3bjw", "Test", "HOHOOHO");
		// } catch (FirebaseMessagingException e) {
		// 	e.printStackTrace();
		// }
	}

}
