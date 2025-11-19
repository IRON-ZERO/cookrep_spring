package com.cookrep_spring.app.utils;

import com.cookrep_spring.app.config.AwsS3Config;
import com.cookrep_spring.app.exceptions.S3ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner presigner;
    private final String bucket;
    private final S3Client s3Client;
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);


    //=========== 업로드용 서명된 url 발급 =============
    // 여러 파일에 대한 Presigned URL 발급
    public List<Map<String, String>> generatePresignedUrls(List<String> fileNames) {
        return fileNames.stream().map(fileName -> {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
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


    //=========== 읽기용 서명된 url 발급 =============
    public List<Map<String, String>> generateDownloadPresignedUrls(List<String> fileNames) {
        return fileNames.stream().map(fileName -> {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(10)) // URL 유효기간
                    .build();

            String url = presigner.presignGetObject(presignRequest).url().toString();

            return Map.of(
                    "fileName", fileName,
                    "downloadUrl", url
            );
        }).collect(Collectors.toList());
    }

    //=========== 삭제용 서명된 url 발급 =============
    public void deleteObject(String keyOrUrl) {
        try {
            // URL → Key 안전하게 변환
            String key = extractKeyFromUrl(keyOrUrl);

            // S3 삭제 요청 생성
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.error("S3 object not found: {}", keyOrUrl);

        } catch (NoSuchKeyException e) {
            System.out.println("❌ S3 object not found: " + keyOrUrl);
            throw new S3ObjectNotFoundException("삭제할 수 없습니다. S3 객체가 존재하지 않습니다: " + keyOrUrl);
        } catch (S3Exception e) {
            log.error("S3 삭제 실패: {}", keyOrUrl, e);
        } catch (Exception e) {
            throw new RuntimeException("S3 삭제 중 예외 발생: " + keyOrUrl, e);
        }
    }

    /**
     * URL이면 Key 추출, Key면 그대로 반환
     */
    private String extractKeyFromUrl(String keyOrUrl) {
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) {
            try {
                URI uri = new URI(keyOrUrl);
                String path = uri.getPath(); // "/bucket-name/folder/file.png"
                int index = path.indexOf(bucket + "/");
                if (index >= 0) {
                    return path.substring(index + bucket.length() + 1);
                }
                return path.startsWith("/") ? path.substring(1) : path;
            } catch (URISyntaxException e) {
                throw new RuntimeException("URL 형식이 잘못되었습니다: " + keyOrUrl, e);
            }
        }
        // 이미 Key면 그대로 반환
        return keyOrUrl;
    }


}
