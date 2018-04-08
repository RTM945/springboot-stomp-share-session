package me.rtmsoft.springboot_stomp_share_session;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.WebSocketSession;

@RestController
public class TestController {
	
	@RequestMapping(path = "/login", method=RequestMethod.POST)
	@ResponseBody
	public String login(HttpSession session) {
		System.out.println("login");
		session.setAttribute("user", "user");
		session.setAttribute("shareAttr", new HashMap<String, Object>());
		return "{\"error\":0}";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(path = "/wssession", method=RequestMethod.POST)
	@ResponseBody
	public String getWebsocketSession(HttpSession session) {
		Map<String, Object> map = (Map<String, Object>) session.getAttribute("shareAttr");
		WebSocketSession wssession = (WebSocketSession) map.get("WEBSOCKET.SESSION");
		return "{\"websocket sessionId\":" + wssession.getId() + "}";
	}

}
