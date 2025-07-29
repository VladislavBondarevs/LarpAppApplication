# LarpAppApplication 🎭

LarpAppApplication is a Spring Boot web application developed for managing live-action role-playing (LARP) organizations, characters, and participants.
It includes user roles, organizational structures, and backend logic split by layers for clean architecture.

## 🔧 Technologies Used

- Java 21
- Spring Boot
- Spring Security
- Thymeleaf (HTML templates)
- Maven
- PostgreSQL or H2 (for development)

## ✨ Features

- User registration and login
- Role-based access control (`USER`, `ADMIN`)
- Organization and faction management
- Character and role assignment
- Admin panel with user and role control
- Modular architecture using DTOs, services, and exception handling

## 📁 Project Structure

src/
└── main/
├── java/
│ └── de/larphelden/larp_app/
│ ├── controllers/
│ ├── dto/
│ ├── exceptions/
│ ├── impl/
│ ├── models/
│ ├── repositories/
│ ├── security/
│ └── services/
└── resources/
├── static/
└── templates/
└── infos/

## 🚀 How to Run

mvn clean spring-boot:run

Then open http://localhost:8080 in your browser.

Default roles: USER, ADMIN

## 📄 License
This project is private and intended for demonstration purposes.

## 👤 Author
Vladislav Bondarevs