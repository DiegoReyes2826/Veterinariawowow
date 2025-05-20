package com.veterinaria.app.controller;

import com.veterinaria.app.model.Cita;
import com.veterinaria.app.model.Mascota;
import com.veterinaria.app.model.Usuario;
import com.veterinaria.app.repository.CitaRepository;
import com.veterinaria.app.repository.MascotaRepository;
import com.veterinaria.app.repository.UsuarioRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class VeterinarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping("/recepcionista/veterinarios-disponibles")
    public String verVeterinariosDisponibles(
            @RequestParam(value = "nombre", required = false) String nombre,
            Model model) {

        // Depuración: Verificar todos los usuarios en la base de datos
        List<Usuario> todosUsuarios = usuarioRepository.findAll();
        System.out.println("Total usuarios en la base de datos: " + todosUsuarios.size());
        for (Usuario u : todosUsuarios) {
            System.out.println("Usuario: " + u.getNombre() + ", ID: " + u.getId() + ", Rol: " + u.getRol());
        }

        // Obtener todos los veterinarios (usuarios con rol "VETERINARIO")
        List<Usuario> todosVeterinarios = usuarioRepository.findByRol("VETERINARIO");
        if (todosVeterinarios.isEmpty()) {
            todosVeterinarios = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol() != null && u.getRol().trim().equalsIgnoreCase("VETERINARIO"))
                    .toList();
        }
        System.out.println("Veterinarios encontrados: " + todosVeterinarios.size());
        for (Usuario u : todosVeterinarios) {
            System.out.println("Veterinario: " + u.getNombre() + ", ID: " + u.getId() + ", Rol: " + u.getRol());
        }
        model.addAttribute("todosVeterinarios", todosVeterinarios);

        if (nombre != null && !nombre.isEmpty()) {
            List<Usuario> veterinariosEncontrados = usuarioRepository.findByNombreContainingIgnoreCase(nombre.trim());
            veterinariosEncontrados = veterinariosEncontrados.stream()
                    .filter(u -> u.getRol() != null && u.getRol().trim().equalsIgnoreCase("VETERINARIO"))
                    .toList();
            System.out.println("Veterinarios encontrados por nombre '" + nombre + "': " + veterinariosEncontrados.size());
            for (Usuario u : veterinariosEncontrados) {
                System.out.println("Veterinario encontrado por nombre: " + u.getNombre() + ", ID: " + u.getId());
            }

            if (!veterinariosEncontrados.isEmpty()) {
                Usuario veterinario = veterinariosEncontrados.get(0);
                List<Cita> citas = citaRepository.findByVeterinarioId(veterinario.getId().toString());

                // Resolver nombres de usuarios y mascotas
                for (Cita cita : citas) {
                    // Depuración: Mostrar los valores de usuarioId y mascotaId
                    System.out.println("Cita ID: " + cita.getId() + ", usuarioId: " + cita.getUsuarioId() + ", mascotaId: " + cita.getMascotaId());

                    // Validar y buscar nombre del usuario
                    String usuarioNombre = "Desconocido";
                    if (cita.getUsuarioId() != null && cita.getUsuarioId().length() == 24 && cita.getUsuarioId().matches("^[0-9a-fA-F]+$")) {
                        Optional<Usuario> usuarioOpt = usuarioRepository.findById(new ObjectId(cita.getUsuarioId()));
                        usuarioNombre = usuarioOpt.map(Usuario::getNombre).orElse("Desconocido");
                    } else {
                        System.out.println("usuarioId inválido para cita " + cita.getId() + ": " + cita.getUsuarioId());
                    }

                    // Validar y buscar nombre de la mascota
                    String mascotaNombre = "Desconocida";
                    if (cita.getMascotaId() != null && cita.getMascotaId().length() == 24 && cita.getMascotaId().matches("^[0-9a-fA-F]+$")) {
                        Optional<Mascota> mascotaOpt = mascotaRepository.findById(new ObjectId(cita.getMascotaId()));
                        mascotaNombre = mascotaOpt.map(Mascota::getNombre).orElse("Desconocida");
                    } else {
                        System.out.println("mascotaId inválido para cita " + cita.getId() + ": " + cita.getMascotaId());
                    }

                    // Asignar nombres al modelo
                    cita.setUsuarioNombre(usuarioNombre);
                    cita.setMascotaNombre(mascotaNombre);
                }

                System.out.println("Citas encontradas para " + veterinario.getNombre() + ": " + citas.size());
                model.addAttribute("citas", citas);
                model.addAttribute("veterinarioSeleccionado", veterinario);
            } else {
                model.addAttribute("mensaje", "No se encontró ningún veterinario con ese nombre.");
            }
        }

        return "citas/verVeterinariosDisponibles";
    }
}