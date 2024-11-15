package org.example.expert.domain.todo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSimpleResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

	private final TodoRepository todoRepository;
	private final WeatherClient weatherClient;

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public TodoSaveResponse saveTodo(User user, TodoSaveRequest todoSaveRequest) {
		String weather = weatherClient.getTodayWeather();

		Todo newTodo = new Todo(
			todoSaveRequest.getTitle(),
			todoSaveRequest.getContents(),
			weather,
			user
		);
		Todo savedTodo = todoRepository.save(newTodo);

		return new TodoSaveResponse(
			savedTodo.getId(),
			savedTodo.getTitle(),
			savedTodo.getContents(),
			weather,
			new UserResponse(user.getId(), user.getEmail())
		);
	}

	public List<TodoResponse> getTodos(int page, int size, String weather, LocalDateTime startDate,
		LocalDateTime endDate) {
		Pageable pageable = PageRequest.of(page - 1, size);

		StringBuilder jpql = new StringBuilder("SELECT t FROM Todo t WHERE 1=1");

		if (startDate != null) {
			jpql.append(" AND t.modifiedAt >= :startDate");
		}

		if (endDate != null) {
			jpql.append(" AND t.modifiedAt <= :endDate");
		}

		if (weather != null && !weather.isBlank()) {
			jpql.append(" AND t.weather = :weather");
		}

		TypedQuery<Todo> query = entityManager.createQuery(jpql.toString(), Todo.class);

		if (startDate != null) {
			query.setParameter("startDate", startDate);
		}
		if (endDate != null) {
			query.setParameter("endDate", endDate);
		}
		if (weather != null && !weather.isBlank()) {
			query.setParameter("weather", weather);
		}

		query.setFirstResult((int)pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		List<Todo> todos = query.getResultList();

		return todos.stream()
			.map(todo -> new TodoResponse(
				todo.getId(),
				todo.getTitle(),
				todo.getContents(),
				todo.getWeather(),
				new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
				todo.getCreatedAt(),
				todo.getModifiedAt()
			))
			.collect(Collectors.toList());
	}

	public TodoResponse getTodo(long todoId) {
		Todo todo = todoRepository.searchByIdAndUser(todoId).orElseThrow(
			() -> new InvalidRequestException("Todo not found")
		);
		User user = todo.getUser();

		return new TodoResponse(
			todo.getId(),
			todo.getTitle(),
			todo.getContents(),
			todo.getWeather(),
			new UserResponse(user.getId(), user.getEmail()),
			todo.getCreatedAt(),
			todo.getModifiedAt()
		);
	}

	public List<TodoSimpleResponse> searchDetails(int page, int size, @RequestBody TodoSearchCondition condition) {
		Pageable pageable = PageRequest.of(page - 1, size);
		return todoRepository.searchDetails(condition, pageable);
	}
}
