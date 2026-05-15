package com.gdoc.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度6-64位")
    private String password;

    @Size(max = 32, message = "昵称最长32位")
    private String nickname;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}