package com.ledger.business.util;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileUtils {
    public static MultipartFile wrap(String newFileName,MultipartFile file){
        MultipartFile uuidNamedFile = new MultipartFile() {
            @Override
            public String getName() {
                return file.getName();
            }

            @Override
            public String getOriginalFilename() {
                return newFileName;
            }

            @Override
            public String getContentType() {
                return file.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return file.isEmpty();
            }

            @Override
            public long getSize() {
                return file.getSize();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return file.getBytes();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return file.getInputStream();
            }

            @Override
            public Resource getResource() {
                return MultipartFile.super.getResource();
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {

            }

            @Override
            public void transferTo(Path dest) throws IOException, IllegalStateException {
                file.transferTo(dest);
            }
        };
        return uuidNamedFile;
    }
}
