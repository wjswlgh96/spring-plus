package org.example.expert.domain.todo.entity

import jakarta.persistence.*
import org.example.expert.domain.comment.entity.Comment
import org.example.expert.domain.common.entity.Timestamped
import org.example.expert.domain.manager.entity.Manager
import org.example.expert.domain.user.entity.User

@Entity
@Table(name = "todos")
data class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val title: String,
    val contents: String,
    val weather: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @OneToMany(mappedBy = "todo", cascade = [CascadeType.REMOVE])
    val comments: MutableList<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "todo", cascade = [CascadeType.PERSIST])
    val managers: MutableList<Manager> = mutableListOf()
) : Timestamped() {

    companion object {
        fun create(title: String, contents: String, weather: String, user: User): Todo {
            val todo = Todo(
                title = title,
                contents = contents,
                weather = weather,
                user = user
            )
            todo.managers.add(Manager(user, todo))
            return todo
        }
    }

}