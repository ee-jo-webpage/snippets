package kr.or.kosa.snippets.basic.model;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;


@Data
public class Snippets {
   
   private Long snippetId;
   private Long userId;
   private Long folderId;
   private Long colorId;
   private String sourceUrl;
   private SnippetTypeBasic type;
   private String memo;
   private Date createdAt;
   private Date updatedAt;
   private int likeCount;
   private int visibility;
   

   private String Content;    
   private String language;    
   private String imageUrl;    
   private String altText;
   
   private MultipartFile imageFile;
   
   public MultipartFile getImageFile() {
       return imageFile;
   }

   public void setImageFile(MultipartFile imageFile) {
       this.imageFile = imageFile;
   }
}
