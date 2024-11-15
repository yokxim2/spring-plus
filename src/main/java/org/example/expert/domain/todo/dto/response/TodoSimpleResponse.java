package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSimpleResponse {
	private String title;
	private Long managerCount;
	private Long commentCount;
}
