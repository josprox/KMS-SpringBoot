package com.josprox.kms.controller

import com.josprox.kms.repository.ActivationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class DashboardController(private val activationRepository: ActivationRepository) {

    @GetMapping("/")
    fun index() = "redirect:/dashboard"

    @GetMapping("/login")
    fun login() = "login"

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        val activations = activationRepository.findAll()
        model.addAttribute("activations", activations)
        model.addAttribute("totalActivations", activations.size)
        return "dashboard"
    }
}
