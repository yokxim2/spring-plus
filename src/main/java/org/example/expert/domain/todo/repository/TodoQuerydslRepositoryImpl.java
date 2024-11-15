package org.example.expert.domain.todo.repository;

import static org.example.expert.domain.comment.entity.QComment.*;
import static org.example.expert.domain.manager.entity.QManager.*;
import static org.example.expert.domain.todo.entity.QTodo.*;
import static org.example.expert.domain.user.entity.QUser.*;
import static org.springframework.util.StringUtils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.TodoSearchCondition;
import org.example.expert.domain.todo.dto.response.TodoSimpleResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TodoQuerydslRepositoryImpl implements TodoQuerydslRepository {

	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<Todo> searchByIdAndUser(Long todoId) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(todo)
				.join(todo.user, user).fetchJoin()
				.where(todo.id.eq(todoId))
				.fetchOne()
		);
	}

	@Override
	public List<TodoSimpleResponse> searchDetails(TodoSearchCondition condition, Pageable pageable) {
		return queryFactory
			.select(Projections.fields(TodoSimpleResponse.class,
				todo.title,
				manager.countDistinct().as("managerCount"),
				comment.count().as("commentCount")))
			.from(todo)
			.leftJoin(todo.comments, comment)
			.leftJoin(todo.managers, manager)
			.where(
				// 동적 쿼리들
				titleEq(condition.getTitle()),
				firstCreatedDateEq(condition.getFirstCreatedDate()),
				lastCreatedDateEq(condition.getLastCreatedDate()),
				managerNameEq(condition.getManagerName())
			)
			.groupBy(todo.title, todo.createdAt)
			.orderBy(todo.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}

	private BooleanExpression titleEq(String title) {
		return hasText(title) ? todo.title.containsIgnoreCase(title) : null;
	}

	private BooleanExpression firstCreatedDateEq(LocalDateTime firstCreatedDate) {
		return firstCreatedDate != null ? todo.createdAt.goe(firstCreatedDate) : null;
	}

	private BooleanExpression lastCreatedDateEq(LocalDateTime lastCreatedDate) {
		return lastCreatedDate != null ? todo.createdAt.loe(lastCreatedDate) : null;
	}

	private BooleanExpression managerNameEq(String managerName) {
		return hasText(managerName) ? todo.managers.any().user.nickname.containsIgnoreCase(managerName) : null;
	}
}
