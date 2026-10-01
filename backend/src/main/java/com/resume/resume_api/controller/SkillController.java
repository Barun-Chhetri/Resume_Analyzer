package com.resume.resume_api.controller;
import com.resume.resume_api.entity.Skill;
import com.resume.resume_api.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SkillController {
    private final SkillRepository skillRepository;

    @PostMapping
    public Skill addSkill(@RequestBody Skill skill) {
        return skillRepository.save(skill);
    }

    @GetMapping
    public List<Skill> getSkills() {
        return skillRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteSkill(@PathVariable Long id) {
        skillRepository.deleteById(id);
    }
}
