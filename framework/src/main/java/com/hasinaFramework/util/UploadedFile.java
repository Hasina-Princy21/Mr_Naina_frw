package com.hasinaFramework.util;

import java.io.InputStream;

public class UploadedFile {
    private String fileName;
    private String contentType;
    private long size;
    private InputStream inputStream;
    private byte[] bytes;

    public UploadedFile() {}

    public UploadedFile(String fileName, String contentType, long size, InputStream inputStream) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte[] getBytes() throws Exception {
        if (bytes == null && inputStream != null) {
            bytes = inputStream.readAllBytes();
        }
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
