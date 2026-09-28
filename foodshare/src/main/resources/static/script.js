const $ = s => document.querySelector(s);
let page = 0, lastPage = true;

async function api(path, method = 'GET', body) {
  const res = await fetch('/api' + path, { method, headers: { 'Content-Type': 'application/json' }, body: body ? JSON.stringify(body) : undefined });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    const d = data.details ? ': ' + Object.values(data.details).join(', ') : '';
    throw new Error((data.error || res.statusText) + d);
  }
  return data;
}
function toast(msg, err) {
  const t = $('#toast'); t.textContent = msg; t.className = 'show' + (err ? ' err' : '');
  setTimeout(() => t.className = '', 3600);
}
async function run(fn, ok) { try { await fn(); if (ok) toast(ok); } catch (e) { toast(e.message, true); await refresh().catch(() => {}); } }
const tag = s => `<span class="tag ${s}">${s}</span>`;
const fmt = t => new Date(t).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
function timeLeft(t) {
  const ms = new Date(t) - Date.now();
  if (ms <= 0) return '<span class="left urgent">Expired</span>';
  const m = Math.floor(ms / 60000), h = Math.floor(m / 60);
  const txt = h >= 24 ? Math.floor(h / 24) + ' d ' + (h % 24) + ' h' : h ? h + ' h ' + (m % 60) + ' m' : m + ' m';
  return `<span class="left${m < 60 ? ' urgent' : ''}">${txt}</span>`;
}
const opts = (l, f) => l.map(x => `<option value="${x.id}">${f(x)}</option>`).join('');
function table(el, rows, cols) {
  el.innerHTML = '<thead><tr>' + cols.map(c => `<th>${c[0]}</th>`).join('') + '</tr></thead><tbody>' +
    (rows.length ? rows.map(r => '<tr>' + cols.map(c => `<td>${c[1](r) ?? ''}</td>`).join('') + '</tr>').join('')
                 : `<tr><td colspan="${cols.length}" class="hint">Nothing here yet.</td></tr>`) + '</tbody>';
}
function act(method, path, msg) { run(async () => { await api(path, method); await refresh(); }, msg); }
function claim(listingId) {
  const ngoId = +$('#ngoSel').value;
  if (!ngoId) return toast('Add an NGO first', true);
  run(async () => { await api('/claims', 'POST', { listingId, ngoId }); await refresh(); }, 'Claimed. Collect it before it expires.');
}

async function refresh() {
  const [avail, all, claims, impact, donors, ngos] = await Promise.all([
    api(`/listings?status=AVAILABLE&page=${page}&size=8&sort=${$('#sortSel').value}`),
    api('/listings?size=100&sort=createdAt,desc'), api('/claims'), api('/reports/diverted-monthly'),
    api('/donors'), api('/ngos')]);
  lastPage = avail.last;
  $('#pageInfo').textContent = `Page ${avail.number + 1} of ${Math.max(avail.totalPages, 1)}`;
  $('#prev').disabled = page === 0; $('#next').disabled = lastPage;
  table($('#availTable'), avail.content, [['Food', r => r.foodName], ['Type', r => r.foodType], ['Qty', r => r.quantity],
    ['Weight', r => r.weightKg + ' kg'], ['Donor', r => r.donor.name], ['Safe until', r => fmt(r.safeUntil)], ['Time left', r => timeLeft(r.safeUntil)],
    ['', r => `<button onclick="claim(${r.id})">Claim</button>`]]);
  table($('#allTable'), all.content, [['#', r => r.id], ['Food', r => r.foodName], ['Donor', r => r.donor.name], ['Weight', r => r.weightKg + ' kg'],
    ['Safe until', r => fmt(r.safeUntil)], ['Status', r => tag(r.status)],
    ['', r => ['AVAILABLE', 'EXPIRED'].includes(r.status) ? `<button class="alt" onclick="act('DELETE','/listings/${r.id}','Listing deleted')">Delete</button>` : '']]);
  table($('#claimTable'), claims.slice().reverse(), [['#', r => r.id], ['Food', r => r.listing.foodName], ['NGO', r => r.ngo.name],
    ['Claimed', r => fmt(r.claimedAt)], ['Collected', r => r.collectedAt ? fmt(r.collectedAt) : '-'], ['Status', r => tag(r.status)],
    ['', r => r.status === 'ACTIVE' ? `<button onclick="act('PUT','/claims/${r.id}/collect','Marked collected')">Mark collected</button><button class="alt" onclick="act('PUT','/claims/${r.id}/cancel','Claim cancelled')">Cancel</button>` : '']]);
  table($('#impactTable'), impact, [['Month', r => r.month], ['Diverted listings', r => r.divertedListings], ['Diverted items', r => r.divertedQuantity],
    ['Diverted weight', r => r.divertedWeightKg + ' kg'], ['Expired listings', r => r.expiredListings], ['Wasted weight', r => r.expiredWeightKg + ' kg']]);
  const people = [['Name', r => r.name], ['Email', r => r.email], ['Phone', r => r.phone ?? ''], ['Address', r => r.address ?? '']];
  table($('#donorTable'), donors, people.concat([['', r => `<button class="alt" onclick="act('DELETE','/donors/${r.id}','Donor deleted')">Delete</button>`]]));
  table($('#ngoTable'), ngos, people.concat([['', r => `<button class="alt" onclick="act('DELETE','/ngos/${r.id}','NGO deleted')">Delete</button>`]]));
  const keep = $('#ngoSel').value;
  $('#ngoSel').innerHTML = opts(ngos, n => n.name); if (keep) $('#ngoSel').value = keep;
  $('#donorSel').innerHTML = opts(donors, d => d.name);
}

$('#listingForm').onsubmit = e => { e.preventDefault(); const f = e.target;
  run(async () => {
    await api('/listings', 'POST', { donorId: +$('#donorSel').value, foodName: f.foodName.value, foodType: f.foodType.value,
      quantity: +f.quantity.value, weightKg: +f.weightKg.value, safeUntil: f.safeUntil.value });
    f.reset(); setDefaultTime(); await refresh(); }, 'Food listed'); };
document.querySelectorAll('form[data-kind]').forEach(f => f.onsubmit = e => { e.preventDefault();
  run(async () => { await api('/' + f.dataset.kind, 'POST', Object.fromEntries(new FormData(f))); f.reset(); await refresh(); }, 'Saved'); });
$('#sortSel').onchange = () => { page = 0; run(refresh); };
$('#prev').onclick = () => { page--; run(refresh); };
$('#next').onclick = () => { page++; run(refresh); };
document.querySelectorAll('nav button').forEach(b => b.onclick = () => {
  document.querySelectorAll('nav button,.tab').forEach(x => x.classList.remove('on'));
  b.classList.add('on'); $('#' + b.dataset.tab).classList.add('on'); });
function setDefaultTime() {
  const d = new Date(Date.now() + 3 * 3600e3 - new Date().getTimezoneOffset() * 60e3);
  $('#listingForm').safeUntil.value = d.toISOString().slice(0, 16);
}
setDefaultTime();
run(refresh);
setInterval(() => refresh().catch(() => {}), 30000);
