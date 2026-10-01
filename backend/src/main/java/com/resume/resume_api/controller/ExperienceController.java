package com.resume.resume_api.controller;
import com.resume.resume_api.entity.Experience;
import com.resume.resume_api.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExperienceController {
    private final ExperienceRepository experienceRepository;

    @PostMapping
    public Experience addExperience(@RequestBody Experience experience) {
        return experienceRepository.save(experience);
    }

    @GetMapping
    public List<Experience> getExperiences() {
        return experienceRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteExperience(@PathVariable Long id) {
        experienceRepository.deleteById(id);
    }
}
