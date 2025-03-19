package org.home.paper.server.controller

import org.home.paper.server.repository.UserRepository
import org.home.paper.server.Dummies.unsavedUser
import org.home.paper.server.model.User
import org.home.paper.server.service.JwtService

open class AbstractControllerTest(
    protected val userRepository: UserRepository,
    private val jwtService: JwtService
) {

    protected val context = mutableMapOf<String, Any>()

    fun createUser() {
        val user = userRepository.save(unsavedUser)
        context[USER] = user
    }

    fun generateToken(): String {
        val user = (context[USER] as User?) ?: throw IllegalStateException("No user found")
        return jwtService.generateToken(user)
    }

    companion object {
        const val USER = "user"
    }

}
