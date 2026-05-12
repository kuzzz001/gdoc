package com.gdoc.model.dto;

import jakarta.validation.constraints.NotNull;

public class AddFriendRequest {

    @NotNull(message = "好友ID不能为空")
    private Long friendId;

    public Long getFriendId() { return friendId; }
    public void setFriendId(Long friendId) { this.friendId = friendId; }
}
