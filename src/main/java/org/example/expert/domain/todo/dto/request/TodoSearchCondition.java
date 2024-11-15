package org.example.expert.domain.todo.dto.request;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TodoSearchCondition {
	// 일정 제목, 생성일 시작, 생성일 끝, 담당자 닉네임
	private String title;
	private LocalDateTime firstCreatedDate;
	private LocalDateTime lastCreatedDate;
	private String managerName;
}
