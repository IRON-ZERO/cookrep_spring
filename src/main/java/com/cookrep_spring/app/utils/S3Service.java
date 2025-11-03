package com.cookrep_spring.app.utils;

import com.cookrep_spring.app.exceptions.S3ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final String BUCKET_NAME = "cookrepbucket";
    private final S3Client s3Client;

    //=========== 업로드용 서명된 url 발급 =============
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


    //=========== 읽기용 서명된 url 발급 =============
    public List<Map<String, String>> generateDownloadPresignedUrls(List<String> fileNames) {
        return fileNames.stream().map(fileName -> {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
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
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            System.out.println("S3 object deleted: " + key);

        } catch (NoSuchKeyException e) {
            System.out.println("❌ S3 object not found: " + keyOrUrl);
            throw new S3ObjectNotFoundException("삭제할 수 없습니다. S3 객체가 존재하지 않습니다: " + keyOrUrl);
        } catch (S3Exception e) {
            e.printStackTrace();
            throw new RuntimeException("S3 삭제 실패: " + keyOrUrl, e);
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
                int index = path.indexOf(BUCKET_NAME + "/");
                if (index >= 0) {
                    return path.substring(index + BUCKET_NAME.length() + 1);
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
