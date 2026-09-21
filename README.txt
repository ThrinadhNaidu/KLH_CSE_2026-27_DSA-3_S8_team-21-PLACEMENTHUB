PLACEMENT HUB
=============
A web-based student placement management and approval system using:
- HTML + CSS + JavaScript frontend
- Java built-in HttpServer backend
- TXT files as the database
- DSA: Linear Search, KMP, Levenshtein, Fuzzy Matching, Jaccard Similarity, Trie and PriorityQueue max heap

PROJECT STRUCTURE
-----------------
PlacementHub_Web_Project/
|-- src/PlacementHubServer.java    Java HTTP server, models, APIs and DSA
|-- data/                          TXT storage files
|   |-- students.txt
|   |-- admins.txt
|   |-- companies.txt
|   |-- applications.txt
|   |-- notifications.txt
|   |-- drives.txt
|   `-- placement_history.txt
|-- web/
|   |-- index.html                 Landing page
|   |-- login.html                 Student/admin login
|   |-- register.html              Student registration
|   |-- student.html               Student dashboard
|   |-- admin.html                 Admin dashboard
|   |-- css/style.css              Shared styling
|   `-- js/
|       |-- student.js
|       `-- admin.js
|-- out/                           Compiled Java classes
`-- README.txt

REQUIREMENTS
------------
Java 17 or later.

RUN
---
1. Open terminal in the `PlacementHub_Web_Project` folder.
2. Compile:
   javac -d out src/PlacementHubServer.java
3. Run:
   java -cp out PlacementHubServer
4. Open this local website in a browser:
   http://localhost:8080/

The direct home page link is http://localhost:8080/index.html. The server must
remain running while using the website. Press Ctrl+C in the terminal to stop it.

DEMO ACCOUNTS
-------------
Admin:
email: admin@placementhub.com
password: admin123

Student:
email: rahul@gmail.com
password: 1234

DATA
----
All records are stored in data/*.txt.
This is intended as an academic/demo project. Passwords are stored as plain text for simplicity.

The server creates applications.txt, notifications.txt, drives.txt and placement_history.txt automatically if they do not exist.
The pipe character (|) separates fields and commas separate list values such as skills.

STUDENT WORKFLOW
----------------
1. Register and log in as a student.
2. Update the profile, CGPA, branch, skills, training and internships.
3. Search companies using KMP matching.
4. Check eligibility and view skill, CGPA, experience and overall scores.
5. Apply only when mandatory requirements are satisfied.
6. Track PENDING, APPROVED, REJECTED, SHORTLISTED, INTERVIEW, SELECTED or NOT_SELECTED applications.
7. Read admin messages and workflow notifications in the dashboard.
8. Selected applications are copied to placement_history.txt.

ADMIN WORKFLOW
--------------
1. Log in with the admin account.
2. Add or delete companies and search students.
3. Review applications received from students.
4. Approve, reject with a reason, or move an application to another status.
5. Send a direct message to an applicant from the application card.
6. Create placement drives.
7. Find ranked candidates using the PriorityQueue max heap.

APPLICATION STATUSES
--------------------
PENDING -> APPROVED -> SHORTLISTED -> INTERVIEW -> SELECTED
                       `-> REJECTED
                       `-> NOT_SELECTED

MATCHING FORMULA
----------------
Overall Score = 0.5 * Skill Match + 0.3 * CGPA Score + 0.2 * Experience Score
The result is capped at 100 percent. Skill Match uses Jaccard Similarity.
Eligibility separately checks minimum CGPA, required training and required internship.

DSA USAGE
---------
Linear Search: Student ID lookup and applicant lookup.
KMP Pattern Matching: Company, role, student and skill search.
Levenshtein Distance: Typo-tolerant fuzzy skill matching such as pythn -> Python.
Fuzzy Matching: Reuses Levenshtein results for approximate searches.
Jaccard Similarity: Calculates overlap between student and company skills.
Trie: Prefix autocomplete for skills such as jav -> java and javascript.
PriorityQueue / Max Heap: Retrieves the highest-scoring candidates for top-K ranking.

TXT RECORDS
-----------
students.txt: id|name|email|password|branch|cgpa|skills|training|internships|certifications|projects|placementStatus
companies.txt: id|name|role|description|minCGPA|requiredSkills|requiredTraining|requiredInternship|location|package|deadline|openings
applications.txt: applicationId|studentId|studentName|companyId|companyName|role|matchScore|date|status|adminRemark
notifications.txt: notificationId|recipientId|type|message|date|read
drives.txt: driveId|company|role|date|venue|openings|deadline|requirements
placement_history.txt: studentId|studentName|company|role|package|selectionDate|status

API REFERENCE
-------------
Authentication and profile:
POST /api/login, POST /api/register, GET /api/student, POST /api/student/update

Companies and students:
GET /api/companies?q=text, POST /api/company/add, POST /api/company/delete?id=COMP001
GET /api/students?q=text&minCGPA=8.0

Matching and search:
GET /api/match?company=COMP001&student=STU101
GET /api/match-all?company=COMP001&limit=5
GET /api/fuzzy?q=pythn
GET /api/skills/autocomplete?q=jav

Applications and notifications:
POST /api/application/apply
GET /api/applications?studentId=STU101&status=PENDING
POST /api/application/status
POST /api/application/approve
POST /api/application/reject
GET /api/notifications?recipient=STU101
POST /api/notifications/send
POST /api/notifications/read

Drives, history and analytics:
POST /api/drive/add, GET /api/drives
GET /api/placement-history?studentId=STU101
GET /api/analytics
GET /api/stats

FILES CREATED OR MODIFIED
-------------------------
Created automatically: data/applications.txt, data/notifications.txt, data/drives.txt, data/placement_history.txt.
Modified: src/PlacementHubServer.java, web/student.html, web/js/student.js, web/admin.html, web/js/admin.js and this README.txt.

CURRENT PROJECT SCOPE
---------------------
This remains a simple academic TXT-file project. The existing Java server architecture and original DSA algorithms are preserved.
The dashboards currently combine company management, student management, matching, applications, notifications and drives in two pages.
There is no external database, chart library or framework dependency.

WORKFLOW APIs
-------------
POST /api/application/apply       Create an eligible application and admin notification.
GET  /api/applications             List applications; use studentId or status filters.
POST /api/application/status       Move an application through its workflow.
POST /api/application/approve      Approve an application.
POST /api/application/reject       Reject an application with a remark.
GET  /api/notifications             Read notifications for a recipient.
GET  /api/skills/autocomplete       Trie prefix suggestions.
GET  /api/match                    Composite eligibility and match score.
GET  /api/match-all                PriorityQueue top candidates; use limit or all.
POST /api/drive/add, GET /api/drives Placement drive management.
GET  /api/placement-history         Selected student placement history.
GET  /api/analytics                 Application and placement statistics.

TXT FILES
---------
applications.txt, notifications.txt, drives.txt and placement_history.txt are created automatically on server startup.
