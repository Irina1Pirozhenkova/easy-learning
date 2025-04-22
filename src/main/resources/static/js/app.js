async function postJson(url, body) {
  const res = await fetch(url, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export async function authFetch(url, opts = {}) {
  const access = localStorage.getItem('accessToken');
  opts.headers = {...(opts.headers || {}), 'Authorization': `Bearer ${access}`};
  return fetch(url, opts);
}

// Регистрация + автологин
const regForm = document.getElementById('registerForm');
if (regForm) {
  regForm.addEventListener('submit', async e => {
    e.preventDefault();
    const data = {
      email: regForm.email.value,
      password: regForm.password.value,
      tutor: regForm.tutor.checked
    };
    await postJson('/api/v1/auth/register', data);
    const tokens = await postJson('/api/v1/auth/login', {
      username: data.email,
      password: data.password
    });
    localStorage.setItem('accessToken',  tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    window.location.href = '/';
  });
}

// Логин
const loginForm = document.getElementById('loginForm');
if (loginForm) {
  loginForm.addEventListener('submit', async e => {
    e.preventDefault();
    const payload = {
      username: loginForm.username.value,
      password: loginForm.password.value
    };
    const tokens = await postJson('/api/v1/auth/login', payload);
    localStorage.setItem('accessToken',  tokens.accessToken);
    localStorage.setItem('refreshToken', tokens.refreshToken);
    window.location.href = '/';
  });
}

// Logout‑кнопка
window.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logoutBtn');
  if (!logoutBtn) return;
  const hasToken = !!localStorage.getItem('accessToken');
  logoutBtn.classList.toggle('d-none', !hasToken);
  logoutBtn.addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/login';
  });
});