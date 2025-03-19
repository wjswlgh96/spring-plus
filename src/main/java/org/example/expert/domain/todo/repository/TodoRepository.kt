package org.example.expert.domain.todo.repository

import org.example.expert.domain.todo.entity.Todo
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository: JpaRepository<Todo, Long>, TodoRepositoryQuery