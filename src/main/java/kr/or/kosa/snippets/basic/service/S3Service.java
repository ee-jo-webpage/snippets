package kr.or.kosa.snippets.basic.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;
    private final String bucket;

    public S3Service(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket}") String bucket) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
    }

    /**
     * 파일을 S3에 업로드하고, public URL을 반환한다.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String key = "images/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(bucket, key, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        amazonS3.putObject(request);

        return amazonS3.getUrl(bucket, key).toString();
    }

    /**
     * 주어진 URL에서 S3 객체 키를 파싱하여 삭제한다.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        String key = fileUrl.substring(fileUrl.indexOf(bucket) + bucket.length() + 1);
        DeleteObjectRequest deleteRequest = new DeleteObjectRequest(bucket, key);
        amazonS3.deleteObject(deleteRequest);
    }
}
