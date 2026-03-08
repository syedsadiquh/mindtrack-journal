# MindTrack Journal

> **A secure, private, and intelligent journaling application where every feature is designed to protect user data and build trust.**

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring_Boot_4.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

---

## Overview

MindTrack Journal is a modern journaling application built with a "Privacy First" philosophy. Your thoughts should remain yours - encrypted, protected, and private. This application combines a seamless, distraction-free writing experience with robust security measures and private, self-hosted AI analytics.

## Features

### Core User Experience

We designed the core journaling experience to be simple, secure, and distraction-free.

- **Private Journaling Requirements:** The interface provides a clean canvas for your thoughts. It includes a Notion-style rich text editor supporting bold, italics, code blocks, and lists, allowing you to express yourself without limitations. You have full control to view, edit, and permanently delete your entries.
- **Secure Account Management:** From registration to account deletion, every step is built with security in mind. We provide secure password resets via time-limited links and a comprehensive "right to be forgotten" feature that permanently erases all your data upon request.

### AI & Analytics

The application provides intelligent insights into your emotional trends without compromising your privacy.

- **Private Sentiment Analysis:** Your journal entries are automatically analysed for sentiment (Positive, Negative, Neutral). Crucially, this is handled by a **self-hosted AI service**, ensuring your private text never leaves your infrastructure or is shared with third-party AI providers.
- **Personal Analytics Dashboard:** Visualise your emotional journey with sentiment trend charts and a mood calendar. These insights are available strictly to you, securely rendered on your private dashboard.

### Security & Privacy

This is the foundation of MindTrack Journal. We employ industry-standard security practices to ensure your data is unreadable to anyone but you.

- **Authentication:** We use BCrypt for password hashing and stateless, cryptographically signed JWTs stored in secure, HTTP-only cookies to prevent common web attacks like XSS.
- **Strict Data Ownership:** The backend enforces rigorous access controls, ensuring that users can only access their own data.
- **End-to-End Protection:**
  - **In Transit:** All communication is secured via mandatory HTTPS/TLS.
  - **At Rest:** The database uses encryption at rest.
  - **Application Level:** Content is encrypted by the application *before* it reaches the database. Even if the database were compromised, your journal entries would remain unreadable.
- **Network Security:** We implement strict CORS policies, security headers (CSP, HSTS), and rate limiting on sensitive endpoints to protect against brute-force attacks.

### Technical Architecture — "Citadel" Architecture

MindTrack Journal uses a **Citadel Architecture**: a high-performance **hybrid architecture** that combines a Spring Boot 4.x Modulith for core domain workflows with supporting microservices for specialized capabilities such as analytics, payments, gateway routing, and private ML processing.

- **Hybrid Backend Architecture (Spring Modulith + Microservices):** The platform uses Spring Boot 4.x across its backend services. Core user and journal workflows run inside a Spring Modulith with strictly isolated bounded contexts and Spring Application Events for low-latency internal communication, while complementary microservices handle specialized responsibilities to preserve flexibility and deliver high performance at scale.
- **Async ML Integration:** Journal entries are saved and return `201 Created` immediately. Sentiment analysis is triggered via a Spring Modulith event, handled asynchronously on a dedicated thread pool, and calls the ML service via Feign with exponential backoff retry. Serverless cold-starts never degrade user-facing latency.
- **Monorepo Architecture:** Frontend, Backend, and ML services are managed in a single repository.
- **Containerization:** Every component—Spring Boot 4.x services, Python ML Service, PostgreSQL, Keycloak, Redis—is fully dockerized.

---

## Roadmap

- **Import from Notion:** We are building tools to migrate your existing journals from Notion seamlessly.
- **Shariah-Compliant Payments:** Upcoming support for Tabby, Tamara, and PayHalal to provide ethical payment options.

---

## Architecture

### System Architecture
<img width="5875" height="5291" alt="MindTrack Architecture excalidraw" src="https://github.com/user-attachments/assets/93f4a462-ac20-4d73-9e78-b7ac8084ecb4" />


### Database Schema
<img width="7764" height="4772" alt="MindTrack Journal DB excalidraw" src="https://github.com/user-attachments/assets/5cdfce9a-f915-4824-8ee6-e886d597138f" />

<img width="3972" height="3326" alt="MindTrack Users DB excalidraw" src="https://github.com/user-attachments/assets/832848c9-d36e-42e2-90ab-0d368db00ade" />

<img width="5272" height="3259" alt="MindTrack Payments DB excalidraw" src="https://github.com/user-attachments/assets/abe23c25-979b-46f8-b619-5ed8115d6162" />

---

## Tech Stack

- **Frontend:** Next.js, React (Modern, responsive UI with SSR)
- **Backend:** Java 21, Spring Boot 4.x, Spring Modulith + Microservices (Hybrid architecture with bounded context isolation and specialized services)
- **ML Service:** Python, FastAPI, TextBlob (Self-hosted sentiment analysis — private by design)
- **Database:** PostgreSQL
- **Auth:** Keycloak (OAuth2 / JWT resource server)
- **DevOps:** Docker, Docker Compose (Containerization)

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21+ (for local backend dev)
- Node.js 18+ (for local frontend dev)
- Python 3.12+ (for local ML dev)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/syedsadiquh/mindtrack-journal.git
   cd mindtrack-journal
   ```

2. **Start the application (Hybrid Architecture):**
   ```bash
   cd backend
   docker-compose up --build
   ```

3. **Access the services:**
   - Frontend: `http://localhost:3000`
   - Backend API Gateway: `http://localhost:2000`
   - ML Service: `http://localhost:8000`
