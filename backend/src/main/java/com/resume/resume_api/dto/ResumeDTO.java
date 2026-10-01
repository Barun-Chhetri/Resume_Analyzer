package com.resume.resume_api.dto;

import com.resume.resume_api.entity.Contact;
import com.resume.resume_api.entity.Education;
import com.resume.resume_api.entity.Experience;
import com.resume.resume_api.entity.Skill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDTO {
    private Contact contact;
    private List<Skill> skills;
    private List<Education> education;
    private List<Experience> experience;
}
