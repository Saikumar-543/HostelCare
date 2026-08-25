const complaints = [
  {id:'CMP1028', room:'A-204', category:'Plumbing', icon:'droplets', priority:'High', priorityClass:'high', status:'Pending', statusClass:'pending', staff:'Unassigned', updated:'12 min ago'},
  {id:'CMP1027', room:'B-112', category:'Wi-Fi', icon:'wifi', priority:'Medium', priorityClass:'medium', status:'In progress', statusClass:'progress', staff:'Ravi Kumar', updated:'34 min ago'},
  {id:'CMP1026', room:'C-018', category:'Electrical', icon:'zap', priority:'High', priorityClass:'high', status:'In progress', statusClass:'progress', staff:'Anil Verma', updated:'1 hr ago'},
  {id:'CMP1025', room:'A-105', category:'Cleaning', icon:'sparkles', priority:'Low', priorityClass:'low', status:'Resolved', statusClass:'resolved', staff:'Meena Iyer', updated:'2 hrs ago'},
  {id:'CMP1024', room:'D-301', category:'Food hygiene', icon:'utensils', priority:'Critical', priorityClass:'high', status:'Pending', statusClass:'pending', staff:'Unassigned', updated:'3 hrs ago'}
];
const roleData = {
  admin: {name:'Priya', initials:'PS', lede:'Here is what is happening across your hostel today.', metrics:['128','24','18','2.4 days'], rows:complaints},
  student: {name:'Sai', initials:'SK', lede:'Track your requests and stay up to date on every resolution.', metrics:['6','2','3','2.1 days'], rows:complaints.slice(0, 3).map((complaint, index) => ({...complaint, staff:index === 0 ? 'Unassigned' : complaint.staff}))},
  staff: {name:'Ravi', initials:'RK', lede:'Your assigned work queue, updates, and resolution progress in one place.', metrics:['8','3','2','1.8 days'], rows:complaints.slice(1, 5)}
};
const rows = document.querySelector('#complaintRows');
const toast = document.querySelector('#toast');
let currentRole = 'admin';
let activeRows = complaints;
function renderRows(items = complaints) {
  activeRows = items;
  rows.innerHTML = items.map(c => `<tr><td>${c.id}</td><td><span class="category-cell"><i class="category-icon"><i data-lucide="${c.icon}"></i></i>${c.category}</span></td><td><span class="pill ${c.priorityClass}">${c.priority}</span></td><td><span class="pill ${c.statusClass}">${c.status}</span></td><td>${c.staff}</td><td>${c.updated}</td><td><button class="row-action" aria-label="Open ${c.id}" data-id="${c.id}"><i data-lucide="arrow-up-right"></i></button></td></tr>`).join('');
  lucide.createIcons();
  document.querySelectorAll('.row-action').forEach(button => button.addEventListener('click', () => openDetails(button.dataset.id)));
}
function showToast(message) { toast.textContent = message; toast.classList.add('show'); window.clearTimeout(showToast.timer); showToast.timer = window.setTimeout(() => toast.classList.remove('show'), 2400); }
async function requestComplaintAction(id, action, body) {
  const response = await fetch(`/api/complaints/${encodeURIComponent(id)}/${action}`, {
    method: 'POST',
    headers: body ? {'Content-Type': 'application/json'} : undefined,
    body: body ? JSON.stringify(body) : undefined
  });
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    throw new Error(payload.error || 'The complaint action failed');
  }
  return response.json();
}
function openDetails(id) {
  const complaint = complaints.find(item => item.id === id) || {id, room:'A-204', category:'Plumbing', priority:'High', priorityClass:'high', status:'Pending', statusClass:'pending', staff:'Unassigned', updated:'just now'};
  const actions = currentRole === 'admin' ? '<label class="assign-select">Assign to<select id="assignStaff"><option>Ravi Kumar · Plumbing</option><option>Anil Verma · Electrical</option><option>Suresh Babu · Washing machine</option><option>Meena Iyer · Food hygiene</option></select></label><button class="button action-secondary" data-action="assign">Save assignment</button><button class="button primary" data-action="close">Close complaint</button>' : currentRole === 'staff' ? '<label class="upload-box resolution-upload"><span><i data-lucide="camera"></i><b>Resolution photo</b><small>JPG or PNG</small></span><input id="resolutionUpload" type="file" accept="image/png,image/jpeg"></label><button class="button action-secondary" data-action="start">Start work</button><button class="button primary" data-action="resolve">Upload & mark resolved</button>' : '<span class="read-only-note"><i data-lucide="eye"></i> Read-only view for students</span>';
  document.querySelector('#detailContent').innerHTML = `<div class="detail-head"><div><p class="eyebrow">COMPLAINT DETAIL</p><h2>${complaint.id}</h2><p class="modal-copy">Room ${complaint.room} · ${complaint.category}</p></div><span class="pill ${complaint.statusClass}">${complaint.status}</span></div><div class="detail-meta"><span class="pill ${complaint.priorityClass}">${complaint.priority} priority</span><span class="pill low">${complaint.staff}</span></div><div class="detail-description">${complaint.description || 'Bathroom fixture requires attention. The resident included this request for the maintenance team.'}</div><div class="timeline"><div class="timeline-item"><strong>Complaint submitted</strong><span>Request received from the resident</span><small>Today, 09:14 · System</small></div><div class="timeline-item"><strong>Awaiting triage</strong><span>Queued for admin review</span><small>Today, 09:16 · System</small></div><div class="timeline-item"><strong>Evidence attached</strong><span>Before images are available in the complaint record</span><small>Today, 09:17 · ${complaint.room}</small></div></div><div class="detail-actions">${actions}</div>`;
  document.querySelector('#detailModal').classList.add('open');
  document.querySelectorAll('[data-action]').forEach(button => button.addEventListener('click', async () => {
    const action = button.dataset.action;
    const message = {assign:'Staff assignment saved', close:'Complaint closed', start:'Work started', resolve:'Complaint marked resolved'}[action];
    button.disabled = true;
    try {
      if (action === 'assign') {
        const staffId = Number(document.querySelector('#assignStaff').selectedIndex + 1);
        await requestComplaintAction(id, action, {staffId, staffName:document.querySelector('#assignStaff').value});
      } else if (action === 'resolve') {
        const file = document.querySelector('#resolutionUpload').files[0];
        if (file) {
          const data = await new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
          });
          await requestComplaintAction(id, 'resolution-image', {name:file.name, data});
        }
        await requestComplaintAction(id, action);
      } else {
        await requestComplaintAction(id, action);
      }
      complaint.status = action === 'resolve' ? 'Resolved' : action === 'start' ? 'In progress' : complaint.status;
      showToast(message);
      document.querySelector('#detailModal').classList.remove('open');
      await refreshComplaints();
    } catch (error) {
      button.disabled = false;
      showToast(error.message);
    }
  }));
  lucide.createIcons();
}
renderRows();
document.querySelector('#roleSelect').addEventListener('change', event => {
  currentRole = event.target.value;
  const role = roleData[event.target.value];
  document.querySelector('#greeting').innerHTML = `Good morning, ${role.name} <span>✦</span>`;
  document.querySelector('#lede').textContent = role.lede;
  document.querySelector('#topAvatar').textContent = role.initials;
  document.querySelectorAll('.metric-card > strong').forEach((metric, index) => metric.textContent = role.metrics[index]);
  renderRows(role.rows);
  showToast(`${event.target.options[event.target.selectedIndex].text} dashboard loaded`);
});
document.querySelector('#searchInput').addEventListener('input', event => { const term = event.target.value.toLowerCase(); renderRows(activeRows.filter(c => Object.values(c).some(value => String(value).toLowerCase().includes(term)))); });
document.querySelector('#filterButton').addEventListener('click', () => showToast('Filters are ready for category, priority and status'));
document.querySelector('#exportButton').addEventListener('click', () => showToast('Report export started'));
document.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', () => { document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active')); const matching = document.querySelector(`.nav-item[data-view="${button.dataset.view}"]`); if (matching) matching.classList.add('active'); document.querySelector('#pageTitle').textContent = button.dataset.view[0].toUpperCase() + button.dataset.view.slice(1); if (button.dataset.view !== 'overview') showToast(`${button.dataset.view[0].toUpperCase() + button.dataset.view.slice(1)} view is ready to connect`); }));
const modal = document.querySelector('#modal');
document.querySelector('#newComplaintButton').addEventListener('click', () => modal.classList.add('open'));
document.querySelector('#modalClose').addEventListener('click', () => modal.classList.remove('open'));
document.querySelector('#detailClose').addEventListener('click', () => document.querySelector('#detailModal').classList.remove('open'));
modal.addEventListener('click', event => { if (event.target === modal) modal.classList.remove('open'); });
document.querySelector('#submitComplaint').addEventListener('click', async () => {
  const room = document.querySelector('#complaintRoom').value.trim();
  const description = document.querySelector('#complaintDescription').value.trim();
  if (!room || description.length < 5) { showToast('Add a room number and a description of at least 5 characters'); return; }
  const category = document.querySelector('#complaintCategory').value;
  const files = [...document.querySelector('#imageUpload').files].slice(0, 3);
  try {
    const response = await fetch('/api/complaints', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({roomNumber:room, category:category.toUpperCase().replaceAll(' ','_'), description})});
    if (response.ok) {
      const saved = await response.json();
      await Promise.all(files.map(file => new Promise(resolve => { const reader = new FileReader(); reader.onload = () => fetch(`/api/complaints/${saved.id}/images`, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({name:file.name, data:reader.result})}).finally(resolve); reader.readAsDataURL(file); })));
      modal.classList.remove('open'); renderRows([saved, ...activeRows]); showToast('Complaint and evidence uploaded'); return;
    }
  } catch (_) { /* Continue with the offline demo store. */ }
  complaints.unshift({id:`CMP${1029 + complaints.length}`, room, category, icon:category === 'Electrical' ? 'zap' : category === 'Wi-Fi' ? 'wifi' : category === 'Cleaning' ? 'sparkles' : 'wrench', priority:category === 'Electrical' || category === 'Food hygiene' ? 'High' : 'Medium', priorityClass:category === 'Electrical' || category === 'Food hygiene' ? 'high' : 'medium', status:'Pending', statusClass:'pending', staff:'Unassigned', updated:'just now', description});
  modal.classList.remove('open');
  document.querySelector('#complaintRoom').value = '';
  document.querySelector('#complaintDescription').value = '';
  document.querySelector('#imagePreview').innerHTML = '';
  renderRows(currentRole === 'admin' ? complaints : roleData[currentRole].rows);
  showToast('Complaint created and queued for triage');
});
document.querySelector('#menuButton').addEventListener('click', () => document.querySelector('#sidebar').classList.toggle('open'));
document.querySelector('#logoutButton').addEventListener('click', async () => { try { await fetch('/api/auth/logout', {method:'POST'}); } catch (_) { /* Demo mode has no server session. */ } appShell.classList.remove('authenticated'); loginScreen.style.display = ''; document.querySelector('#loginForm').hidden = false; document.querySelector('#registerForm').hidden = true; showToast('Signed out'); });
lucide.createIcons();
const loginScreen = document.querySelector('#loginScreen');
const appShell = document.querySelector('.app-shell');
const loginForm = document.querySelector('#loginForm');
const demoAccounts = {
  admin: {email:'admin@hostel.com', password:'admin123'},
  student: {email:'sai@hostel.com', password:'password123'},
  staff: {email:'ravi@hostel.com', password:'staff123'}
};
loginForm.addEventListener('submit', event => {
  event.preventDefault();
  const role = document.querySelector('#loginRole').value;
  const account = demoAccounts[role];
  const credentials = {role, email:document.querySelector('#loginEmail').value.trim(), password:document.querySelector('#loginPassword').value};
  fetch('/api/auth/login', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(credentials)})
    .then(response => response.ok ? response.json() : Promise.reject(new Error('demo mode')))
    .then(user => enterApp(user.role))
    .catch(() => {
      if (credentials.email !== account.email || credentials.password !== account.password) { showToast(`Use the demo ${role} account shown in the form`); return; }
      enterApp(role);
    });
});
function enterApp(role) {
  loginScreen.style.display = 'none';
  appShell.classList.add('authenticated');
  currentRole = role;
  document.querySelector('#roleSelect').value = role;
  document.querySelector('#roleSelect').dispatchEvent(new Event('change'));
  fetch('/api/dashboard').then(response => response.ok ? response.json() : Promise.reject()).then(stats => {
    const values = [stats.total, stats.pending, stats.inProgress, `${stats.resolved}`];
    document.querySelectorAll('.metric-card > strong').forEach((metric, index) => metric.textContent = values[index]);
  }).catch(() => {});
  refreshComplaints();
}
async function refreshComplaints() {
  try {
    const response = await fetch('/api/complaints');
    if (!response.ok) throw new Error('Could not load complaints');
    const apiRows = await response.json();
    if (!Array.isArray(apiRows)) return;
    const normalized = apiRows.map(item => ({...item, category:item.category.replaceAll('_',' '), priority:item.priority[0] + item.priority.slice(1).toLowerCase(), status:item.status.replaceAll('_',' ').toLowerCase(), priorityClass:item.priority === 'CRITICAL' || item.priority === 'HIGH' ? 'high' : 'medium', statusClass:item.status === 'RESOLVED' || item.status === 'CLOSED' ? 'resolved' : item.status === 'IN_PROGRESS' ? 'progress' : 'pending', icon:'clipboard-list', updated:'recently'}));
    complaints.splice(0, complaints.length, ...normalized); renderRows(normalized);
  } catch (_) {
    showToast('Demo mode: connect MySQL for live data');
  }
}
document.querySelector('#loginRole').addEventListener('change', event => {
  const account = demoAccounts[event.target.value];
  document.querySelector('#loginEmail').value = account.email;
  document.querySelector('#loginPassword').value = account.password;
  document.querySelector('.demo-hint').textContent = `Demo: ${account.email} / ${account.password}`;
});
document.querySelector('#loginRole').dispatchEvent(new Event('change'));
document.querySelector('#registerToggle').addEventListener('click', () => { document.querySelector('#loginForm').hidden = true; document.querySelector('#registerForm').hidden = false; });
document.querySelector('#backToLogin').addEventListener('click', () => { document.querySelector('#registerForm').hidden = true; document.querySelector('#loginForm').hidden = false; });
document.querySelector('#registerForm').addEventListener('submit', async event => {
  event.preventDefault();
  const account = {name:document.querySelector('#registerName').value.trim(), email:document.querySelector('#registerEmail').value.trim(), phone:document.querySelector('#registerPhone').value.trim(), roomNumber:document.querySelector('#registerRoom').value.trim(), hostelBlock:document.querySelector('#registerBlock').value.trim(), password:document.querySelector('#registerPassword').value};
  try {
    const response = await fetch('/api/auth/register', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(account)});
    if (!response.ok) throw new Error((await response.json()).error || 'Registration failed');
    showToast('Account created. Sign in to continue');
  } catch (error) {
    if (error.message !== 'Failed to fetch' && !error.message.includes('Registration failed')) { showToast(error.message); return; }
    showToast('Registration is available when the database server is connected');
  }
  document.querySelector('#registerForm').hidden = true; document.querySelector('#loginForm').hidden = false;
  document.querySelector('#loginRole').value = 'student'; document.querySelector('#loginRole').dispatchEvent(new Event('change')); document.querySelector('#loginEmail').value = account.email; document.querySelector('#loginPassword').value = account.password;
});
document.querySelector('#imageUpload').addEventListener('change', event => {
  const preview = document.querySelector('#imagePreview');
  preview.innerHTML = '';
  [...event.target.files].slice(0, 3).forEach(file => {
    const image = document.createElement('img');
    image.alt = file.name;
    image.src = URL.createObjectURL(file);
    preview.appendChild(image);
  });
  if (event.target.files.length > 3) showToast('Only the first 3 images will be attached');
});
