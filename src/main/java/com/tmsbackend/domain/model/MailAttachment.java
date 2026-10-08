package com.tmsbackend.domain.model;

public record MailAttachment(String fileName, String contentType, byte[] data) {
}
