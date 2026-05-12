package com.gdoc.common.result;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    TOKEN_INVALID(1004, "Token无效或已过期"),

    DOC_NOT_FOUND(2001, "文档不存在"),
    DOC_PERMISSION_DENIED(2002, "无权操作该文档"),
    DOC_SHARE_EXPIRED(2003, "分享链接已过期"),
    DOC_SHARE_NOT_FOUND(2004, "分享链接不存在"),
    DOC_COLLABORATOR_NOT_FOUND(2005, "协作者不存在"),
    DOC_COLLABORATOR_EXISTS(2006, "该用户已是协作者"),
    DOC_CANNOT_REMOVE_OWNER(2007, "不能移除文档拥有者"),

    COLLAB_ROOM_FULL(3001, "协同房间已满"),
    COLLAB_VERSION_CONFLICT(3002, "操作版本冲突，请重试"),

    FRIEND_ALREADY_EXISTS(4001, "已经是好友或已发送过请求"),
    FRIEND_REQUEST_NOT_FOUND(4002, "好友请求不存在"),
    FRIEND_CANNOT_ADD_SELF(4003, "不能添加自己为好友"),
    FRIEND_NOT_FOUND(4004, "好友关系不存在"),
    FRIEND_REQUEST_ALREADY_HANDLED(4005, "好友请求已处理"),

    MESSAGE_NOT_FOUND(5001, "消息不存在"),

    INVITATION_NOT_FOUND(6001, "协作邀请不存在"),
    INVITATION_ALREADY_HANDLED(6002, "协作邀请已处理"),
    INVITATION_NOT_FRIEND(6003, "只能邀请好友协作");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
