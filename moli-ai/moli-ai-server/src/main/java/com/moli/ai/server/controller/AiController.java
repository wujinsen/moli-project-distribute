package com.moli.ai.server.controller;


import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
@Api(tags = "demo")
public class AiController {

    @GetMapping("/test")
    public String test(){

        return "test success";
    }
}
