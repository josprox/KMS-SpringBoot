package com.josprox.kms.controller

import com.josprox.kms.repository.ActivationRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.time.format.DateTimeFormatter

@Controller
class DashboardController(private val activationRepository: ActivationRepository) {

    @GetMapping("/")
    fun index() = "redirect:/dashboard"

    @GetMapping("/login")
    fun login() = "login"

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        val activations = activationRepository.findAll()
        
        // Stats for Charts
        val softwareStats = activations.groupBy { it.softwareName }
            .mapValues { it.value.size }

        val dailyStats = activations.groupBy { it.activationDate.toLocalDate() }
            .mapValues { it.value.size }
            .toSortedMap()

        model.addAttribute("activations", activations.sortedByDescending { it.activationDate })
        model.addAttribute("totalActivations", activations.size)
        model.addAttribute("softwareLabels", softwareStats.keys.toList())
        model.addAttribute("softwareData", softwareStats.values.toList())
        model.addAttribute("dailyLabels", dailyStats.keys.map { it.format(DateTimeFormatter.ofPattern("MMM dd")) })
        model.addAttribute("dailyData", dailyStats.values.toList())

        return "dashboard"
    }
}
