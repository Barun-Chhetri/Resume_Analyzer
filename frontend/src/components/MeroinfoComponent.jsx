import React, { useState, useEffect } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import axios from "axios";
import barunImage from "../assets/barun.jpg";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { 
  faEnvelope, 
  faPhone, 
  faMapMarker, 
  faGlobe, 
  faLaptopCode,
  faCode, 
  faGraduationCap, 
  faBriefcase,
  faMoon,
  faSun
} from "@fortawesome/free-solid-svg-icons";

const MeroinfoComponent = () => {
  const [darkMode, setDarkMode] = useState(false);
  const [resume, setResume] = useState(null);

  // Toggle theme
  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
    document.body.setAttribute('data-theme', darkMode ? 'light' : 'dark');
  };

  // Fetch resume data from backend
  useEffect(() => {
    axios.get("http://localhost:8080/api/resume")
      .then((response) => {
        setResume(response.data);
      })
      .catch((error) => {
        console.error("Error fetching resume:", error);
      });
  }, []);

  if (!resume) {
    return <div className="text-center mt-5">Loading Resume...</div>;
  }

  return (
    <div className={darkMode ? "bg-dark text-light" : "bg-light text-dark"} style={{ minHeight: "100vh" }}>
      <div className="container">
        {/* Dark Mode Toggle */}
        <button onClick={toggleDarkMode} className="theme-toggle">
          <FontAwesomeIcon icon={darkMode ? faSun : faMoon} />
        </button>
  
        {/* Header Section */}
        <div className="row align-items-center mb-4">
          <div className="col-md-3 text-center mb-3 mb-md-0">
            <img
              className="rounded-circle img-fluid"
              src={barunImage}
              alt="Profile"
              style={{ width: "150px", height: "150px", objectFit: "cover" }}
            />
          </div>
          <div className="col-md-9 text-center text-md-start">
            <h1 className="display-5 mb-1 fw-bold">
              {resume.contact.name?.toUpperCase() || "BARUN CHHETRI"}
            </h1>
            <p className="h4 text-secondary mb-0">
              <FontAwesomeIcon icon={faLaptopCode} className="me-2" />
              {resume.contact.title || "Fullstack Developer"}
            </p>
          </div>
        </div>
  
        {/* Main Content Grid */}
        <div className="row g-4">
          {/* Left Column */}
          <div className="col-md-4">
            {/* Contact Section */}
            <section className="resume-section">
              <h2 className="section-title">
                <FontAwesomeIcon icon={faMapMarker} className="me-2" />
                Contact
              </h2>
              <ul className="list-unstyled">
                <li className="mb-2">
                  <FontAwesomeIcon icon={faPhone} className="me-2 text-primary" />
                  {resume.contact.phone}
                </li>
                <li className="mb-2">
                  <FontAwesomeIcon icon={faEnvelope} className="me-2 text-primary" />
                  {resume.contact.email}
                </li>
                <li className="mb-2">
                  <FontAwesomeIcon icon={faGlobe} className="me-2 text-primary" />
                  {resume.contact.website}
                </li>
                <li>
                  <FontAwesomeIcon icon={faMapMarker} className="me-2 text-primary" />
                  {resume.contact.address}
                </li>
              </ul>
            </section>
  
            {/* Skills Section */}
            <section className="resume-section">
              <h2 className="section-title">
                <FontAwesomeIcon icon={faCode} className="me-2" />
                Skills
              </h2>
              <ul className="list-unstyled">
                {resume.skills.map((skill, index) => (
                  <li key={index} className="skill-item">{skill.name}</li>
                ))}
              </ul>
            </section>
  
            {/* Education Section */}
            <section className="resume-section">
              <h2 className="section-title">
                <FontAwesomeIcon icon={faGraduationCap} className="me-2" />
                Education
              </h2>
              {resume.education.map((edu, index) => (
                <div key={index} className="education-item">
                  <h3 className="h6 mb-0">{edu.degree}</h3>
                  <p className="text-active mb-0">{edu.school}</p>
                  <small className="text-active">{edu.year}</small>
                </div>
              ))}
            </section>
          </div>
  
          {/* Right Column */}
          <div className="col-md-8">
            {/* About Section */}
            <section className="resume-section">
              <h2 className="section-title">
                <FontAwesomeIcon icon={faBriefcase} className="me-2" />
                Professional Summary
              </h2>
              <p className="text-active">
                {resume.contact.summary || "Passionate developer eager to build scalable solutions."}
              </p>
            </section>
  
            {/* Experience Section */}
            <section className="resume-section">
              <h2 className="section-title">
                <FontAwesomeIcon icon={faBriefcase} className="me-2" />
                Work Experience
              </h2>
              {resume.experience.map((exp, index) => (
                <div key={index} className="experience-item">
                  <div className="d-flex justify-content-between align-items-center">
                    <h3 className="h5 mb-0">{exp.title}</h3>
                    <span className="badge bg-primary">{exp.period}</span>
                  </div>
                  <p className="mb-1">{exp.company}</p>
                  <ul className="text-active">
                    {exp.achievements.map((ach, i) => (
                      <li key={i}>{ach}</li>
                    ))}
                  </ul>
                </div>
              ))}
            </section>
            
          </div>
        </div>
      </div>
    </div>
  );
};

export default MeroinfoComponent;
