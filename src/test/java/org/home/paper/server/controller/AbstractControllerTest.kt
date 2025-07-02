package org.home.paper.server.controller

import org.home.paper.server.Dummies.unsavedAdmin
import org.home.paper.server.Dummies.unsavedUser
import org.home.paper.server.model.auth.User
import org.home.paper.server.repository.UserRepository
import org.home.paper.server.service.JwtService
import org.springframework.http.HttpHeaders

open class AbstractControllerTest(
    protected val userRepository: UserRepository,
    private val jwtService: JwtService
) {

    protected val context = mutableMapOf<String, Any>()

    fun createUser() {
        val user = userRepository.save(unsavedUser)
        context[USER] = user
    }

    fun createAdmin() {
        val user = userRepository.save(unsavedAdmin)
        context[USER] = user
    }

    fun generateToken(): String {
        val user = (context[USER] as User?) ?: throw IllegalStateException("No user found")
        return jwtService.generateToken(user.toSecure())
    }

    fun getUser(): User {
        return context.getValue(USER) as User
    }

    fun authHeaders(): HttpHeaders {
        val jwtToken = generateToken()
        return HttpHeaders().also {
            it.setBearerAuth(jwtToken)
        }
    }

    companion object {
        const val USER = "user"
    }

}
