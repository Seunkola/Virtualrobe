package com.amazonaws.models.nosql;

import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = AWSConfiguration.AMAZON_DYNAMODB_TABLENAME_CHATROOM)

public class ChatRoomDO {
    private String _userId;
    private String _chatRoomId;
    private String _createdAt;
    private String _name;
    private String _recipients;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "chatRoomId")
    @DynamoDBIndexHashKey(attributeName = "chatRoomId", globalSecondaryIndexName = "ByCreationDate")
    public String getChatRoomId() {
        return _chatRoomId;
    }

    public void setChatRoomId(final String _chatRoomId) {
        this._chatRoomId = _chatRoomId;
    }
    @DynamoDBIndexRangeKey(attributeName = "createdAt", globalSecondaryIndexName = "ByCreationDate")
    public String getCreatedAt() {
        return _createdAt;
    }

    public void setCreatedAt(final String _createdAt) {
        this._createdAt = _createdAt;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
    @DynamoDBAttribute(attributeName = "recipients")
    public String getRecipients() {
        return _recipients;
    }

    public void setRecipients(final String _recipients) {
        this._recipients = _recipients;
    }

    @DynamoDBAttribute(attributeName = "username")
    public String getuser() {
        return _userId;
    }

    public void setuser(final String _user) {
        this._userId = _user;
    }

}
