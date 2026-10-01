package com.resume.resume_api.controller;
import com.resume.resume_api.entity.Education;
import com.resume.resume_api.repository.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/educations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class EducationController {
    private final EducationRepository educationRepository;

    @PostMapping
    public Education addEducation(@RequestBody Education education) {
        return educationRepository.save(education);
    }

    @GetMapping
    public List<Education> getEducations() {
        return educationRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteEducation(@PathVariable Long id) {
        educationRepository.deleteById(id);
    }
}
