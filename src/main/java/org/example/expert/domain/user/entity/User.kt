package org.example.expert.domain.user.entity

import jakarta.persistence.*
import org.example.expert.domain.common.entity.Timestamped
import org.example.expert.domain.user.enums.UserRole

@Entity
@Table(name = "users")
class User private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null,

    val email : String,

    var nickname : String,

    var password : String,

    @Enumerated(EnumType.STRING)
    var userRole : UserRole

): Timestamped() {

    companion object {
        fun of(email : String, nickname : String, password : String, userRole: UserRole): User {
            return User(
                email = email,
                nickname = nickname,
                password = password,
                userRole = userRole
            )
        }
    }

    fun getUsername(): String {
        return email
    }

    fun changePassword(password : String) {
        this.password = password
    }

    fun updateRole(userRole : UserRole) {
        this.userRole = userRole
    }
}