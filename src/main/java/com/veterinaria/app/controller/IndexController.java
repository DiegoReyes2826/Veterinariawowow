package com.veterinaria.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @GetMapping("/")
    public String mostrarIndex(@RequestParam(required = false) String rol, Model model) {
        model.addAttribute("rol", rol);
        return "index";
    }

    @PostMapping("/seleccionar-rol")
    public String seleccionarRol(@RequestParam("rol") String rol, Model model) {
        model.addAttribute("rol", rol);
        if ("recepcionista".equals(rol)) {
            return "recepcionista"; // Redirige al panel de Recepcionista
        } else if ("administrador".equals(rol)) {
            return "administrador"; // Redirige al panel de Administrador
        }
        return "redirect:/"; // Si el rol no es válido, regresa a la página inicial
    }

    @GetMapping("/recepcionista")
    public String panelRecepcionista(Model model) {
        model.addAttribute("rol", "recepcionista"); // Asegúrate de que rol siempre sea "recepcionista"
        return "recepcionista";
    }
}