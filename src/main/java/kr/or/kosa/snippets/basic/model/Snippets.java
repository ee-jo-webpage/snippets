package kr.or.kosa.snippets.basic.model;

import java.util.Date;

import lombok.Data;


@Data
public class Snippets {
	
	private int snippetid;
	private int userid;
	private int folderid;
	private int colorid;
	private String sourceurl;
	private SnippetTypeBasic type;
	private String memo;
	private Date createdat;
	private Date updatedat;
	private int likecount;
	private int visibility;
	
	private String codecontent;
	private String textcontent;    
    private String language;    
    private String imageurl;    
    private String alttext;
}
