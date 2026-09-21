const user=JSON.parse(localStorage.getItem('session')||'null');
const safeUser = user && typeof user === 'object' ? user : null;
if(!safeUser || safeUser.role!=='student') location.href='/login.html';
const displayName = safeUser && safeUser.name ? safeUser.name : 'Student';
document.getElementById('welcome').textContent = displayName;
let current;
async function loadProfile(){
 const r=await fetch('/api/student?id='+encodeURIComponent(safeUser.id)); const d=await r.json();
 current = d && d.student ? d.student : { name: displayName };
 studentName.textContent = current.name || displayName;
 stats.innerHTML=`<div class="stat">CGPA<b>${current.cgpa ?? 0}</b></div><div class="stat">Skills<b>${(current.skills || []).length}</b></div><div class="stat">Certifications<b>${(current.certifications || []).length}</b></div><div class="stat">Status<b>${current.placementStatus || 'Not Placed'}</b></div>`;
 profileForm.innerHTML=`
 <label>Student ID<input id="p_id" value="${current.id || ''}" disabled></label>
 <label>Name<input id="p_name" value="${current.name || displayName}"></label>
 <label>Email<input id="p_email" value="${current.email || ''}" disabled></label>
 <label>Branch<input id="p_branch" value="${current.branch || ''}"></label>
 <label>CGPA<input id="p_cgpa" value="${current.cgpa ?? 0}" type="number" step="0.01" min="0" max="10"></label>
 <label>Skills<input id="p_skills" value="${(current.skills || []).join(', ')}"></label>
 <label>Training<input id="p_training" value="${(current.training || []).join(', ')}"></label>
 <label>Internships<input id="p_internships" value="${(current.internships || []).join(', ')}"></label>
 <label>Certifications<input id="p_certifications" value="${(current.certifications || []).join(', ')}"></label>
 <label>Projects<input id="p_projects" value="${(current.projects || []).join(', ')}"></label>`;
}
async function updateProfile(){
 const body={id:current.id,name:p_name.value,branch:p_branch.value,cgpa:p_cgpa.value,skills:p_skills.value,training:p_training.value,internships:p_internships.value,certifications:p_certifications.value,projects:p_projects.value};
 const r=await fetch('/api/student/update',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
 const d=await r.json(); profileMsg.textContent=d.message; if(d.success) loadProfile();
}
async function loadCompanies(){
 const q=encodeURIComponent(companySearch.value); const r=await fetch('/api/companies?q='+q); const d=await r.json();
 companies.innerHTML=d.companies.map(c=>`<div class="company-card"><h3>${c.name}</h3><b>${c.role}</b><p>${c.description}</p><span class="tag">CGPA ${c.minCGPA}+</span><span class="tag">${c.location}</span><span class="tag">${c.pkg}</span><p><b>Skills:</b> ${c.requiredSkills.map(x=>`<span class="tag">${x}</span>`).join('')}</p><button class="btn btn-outline" onclick="checkMatch('${c.id}')">Check My Match</button><div id="match-${c.id}"></div></div>`).join('')||'<p>No companies found.</p>';
}
async function checkMatch(id){const r=await fetch('/api/match?company='+encodeURIComponent(id)+'&student='+encodeURIComponent(current.id));const d=await r.json();const e=document.getElementById('match-'+id);e.innerHTML=d.eligible?`<p class="success">✓ Eligible — Overall match: <span class="score">${d.overallScore}%</span></p><p>Skill ${d.skillMatch}% | CGPA ${d.cgpaScore}% | Experience ${d.experienceScore}%</p><p>Matched: ${d.matchedSkills.join(', ')||'None'}</p><button class="btn btn-primary" onclick="applyCompany('${id}')">Apply Now</button>`:`<p class="danger-text">Not eligible: ${d.reasons.join(', ')}</p>`;}
async function applyCompany(companyId){const r=await fetch('/api/application/apply',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({studentId:current.id,companyId})});const d=await r.json();alert(d.success?'Application submitted: '+d.application.id:d.message);if(d.success){loadApplications();loadNotifications();}}
async function loadApplications(){const r=await fetch('/api/applications?studentId='+encodeURIComponent(current.id));const d=await r.json();applicationList.innerHTML=d.applications.map(a=>`<div class="student-card"><b>${a.companyName} - ${a.role}</b><p>${a.id} | Match ${a.score}% | <span class="tag">${a.status}</span></p>${a.remark?'<p>'+a.remark+'</p>':''}</div>`).join('')||'<p>No applications yet.</p>';}
async function loadNotifications(){const r=await fetch('/api/notifications?recipient='+encodeURIComponent(current.id));const d=await r.json();notificationList.innerHTML=d.notifications.map(n=>`<div class="student-card"><b>${n.type}</b><p>${n.message}</p><small>${n.date}</small></div>`).join('')||'<p>No notifications.</p>';}
async function autocompleteSkills(){const q=encodeURIComponent(skillSearch.value);if(!q){suggestions.innerHTML='';return;}const r=await fetch('/api/skills/autocomplete?q='+q);const d=await r.json();suggestions.innerHTML=d.suggestions.map(x=>`<button class="tag" onclick="skillSearch.value='${x}';fuzzySearch()">${x}</button>`).join('');}
async function fuzzySearch(){const q=encodeURIComponent(skillSearch.value);const r=await fetch('/api/fuzzy?q='+q);const d=await r.json();searchResults.innerHTML=d.results.map(x=>`<div class="student-card"><b>${x.name}</b> (${x.id}) — matched skill: <span class="tag">${x.skill}</span> — distance ${x.distance}</div>`).join('')||'<p>No similar skills found.</p>';}
function logout(){localStorage.removeItem('session');location.href='/login.html'}
loadProfile();loadCompanies();loadApplications();loadNotifications();
