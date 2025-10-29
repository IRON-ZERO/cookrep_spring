package com.cookrep_spring.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;
    private final String BUCKET_NAME = "cookrepbucket";
    private final S3Client s3Client;

    // 여러 파일에 대한 Presigned URL 발급
    public List<Map<String, String>> generatePresignedUrls(List<String> fileNames) {
        return fileNames.stream().map(fileName -> {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(fileName)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .putObjectRequest(putObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10))
                    .build();

            String url = presigner.presignPutObject(presignRequest).url().toString();
            return Map.of(
                    "fileName", fileName,
                    "uploadUrl", url
            );
        }).collect(Collectors.toList());
    }

    public void deleteObject(String keyOrUrl) {
        try {
            // presigned URL에서 query 파라미터 제거
            String key = keyOrUrl.contains("?") ? keyOrUrl.split("\\?")[0] : keyOrUrl;

            // Bucket 이름 이후 key 추출
            if (key.contains(BUCKET_NAME)) {
                key = key.substring(key.indexOf(BUCKET_NAME) + BUCKET_NAME.length() + 1);
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            System.out.println("S3 object deleted: " + key);

        } catch (S3Exception e) {
            e.printStackTrace();
            throw new RuntimeException("S3 삭제 실패: " + keyOrUrl);
        }
    }

    //
}
