package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.common.Const;
import org.example.expert.common.exception.InvalidRequestException;
import org.example.expert.common.exception.ProfileUploadException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileUrlResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.utils.S3Util;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String BASE_S3_URL = "https://" + Const.BUCKET_NAME + ".s3.amazonaws.com/";

    private final UserRepository userRepository;
    private final S3Client s3Client;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    @Transactional
    public UserProfileUrlResponse updateProfileImage(Long userId, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidRequestException("User not found"));

        S3Util.validateFileExtension(profileImage);         // 확장자 및 올바른 파일 검사

        String originalFilename = profileImage.getOriginalFilename();
        StringBuilder extension = new StringBuilder();

        if (originalFilename != null && originalFilename.contains(".")) {
            extension.append(originalFilename.substring(originalFilename.lastIndexOf(".")));
        }

        String fileName = "profile/" + userId + "/profileImage" + extension;

        try(InputStream inputStream = profileImage.getInputStream()) {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(Const.BUCKET_NAME)
                .key(fileName)
                .contentType(profileImage.getContentType())
                .build();

            s3Client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, profileImage.getSize()));
        } catch (IOException ex) {
            throw new ProfileUploadException("S3 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String profileUrl = BASE_S3_URL + fileName;
        user.updateProfileUrl(profileUrl);
        return new UserProfileUrlResponse(profileUrl);
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InvalidRequestException("User not found"));

        String profileUrl = user.getProfileUrl();
        if (profileUrl == null) {
            throw new InvalidRequestException("삭제할 프로필 이미지가 없습니다.");
        }

        try {
            URI uri = new URI(profileUrl);
            String path = uri.getPath();
            String key = path.startsWith("/") ? path.substring(1) : path;

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(Const.BUCKET_NAME)
                .key(key)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (URISyntaxException ex) {
            throw new ProfileUploadException("잘못된 URL 형식입니다.", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            throw new ProfileUploadException("S3 프로필 이미지 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        user.deleteProfileUrl();
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }
}
