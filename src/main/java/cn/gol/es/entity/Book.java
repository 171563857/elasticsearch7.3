package cn.gol.es.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder(toBuilder = true)
public class Book {
	private Integer bookNo;
	private String bookName;
	private Integer bookType;
	private Integer bookStatus;
	private Integer bookAuditStatus;
	private String bookCover;
	private String bookAuthor;
	private Integer bookFormat;
	private String bookIntro;
	private Integer fileNo;
	private Integer fileSize;
	private String bookFileName;
	private String bookFilePath;
	private String bookFileHash;
	private String remark;

	@Tolerate
	public Book() {
	}
}
