package org.example.expert.utils;

import org.example.expert.common.exception.ProfileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public abstract class S3Util {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");

    public static void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ProfileUploadException("파일 이름이 올바르지 않습니다", BAD_REQUEST);
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ProfileUploadException("허용되지 않은 파일 형식입니다.", BAD_REQUEST);
        }
    }
}
