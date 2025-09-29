package com.example.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.example.Greeting;
import com.example.HelloMessage;

@Controller
public class GreetingController {

  /**
   * 如果消息发送到 /hello 目标地址，就会调用 greeting() 方法，
   * 消息的有效载荷绑定到一个 HelloMessage 对象，该对象被传入 greeting()，
   * 方法的返回值会广播给订阅了 /topic/greetings 目标地址的所有订阅者。
   * 
   * @param message
   * @return
   * @throws Exception
   */
  @MessageMapping("/hello")
  @SendTo("/topic/greetings")
  public Greeting greeting(HelloMessage message) throws Exception {
    Thread.sleep(1000); // simulated delay
    return new Greeting("Hello, " + message.getName() + "!");
  }
}
