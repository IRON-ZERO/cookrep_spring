package com.cookrep_spring.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
}
