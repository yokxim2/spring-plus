package org.example.expert.domain.todo.repository;

import java.util.List;
import java.util.Optional;

import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoSimpleResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Pageable;

public interface TodoQuerydslRepository {
	Optional<Todo> searchByIdAndUser(Long todoId);

	List<TodoSimpleResponse> searchDetails(TodoSearchCondition condition, Pageable pageable);
}
