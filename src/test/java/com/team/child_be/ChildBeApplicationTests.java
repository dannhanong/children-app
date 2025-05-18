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
			fcmService.sendNotificationToUser(13L, "Thông báo", "Xin chào", "https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1EYUPm.img?w=768&h=512&m=6&x=318&y=188&s=210&d=210");
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}
	}

}
