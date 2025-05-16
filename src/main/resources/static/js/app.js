//функция для POST-запросов с JSON
async function postJson(url, body) {
    const res = await fetch(url, { //отправляет HTTP-запрос на указанный url
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body) //из JSON в строку
    });
    if (!res.ok) throw new Error(await res.text());
    //сли сервер вернул ошибку (!res.ok), мы выдираем ответ как текст и бросаем исключение
    return res.json();
}


//Функция для защищённых запросов с токеном
//когда нужно достучаться до защищённого /api/** эндпоинта
export async function authFetch(url, opts = {}) {
    const access = localStorage.getItem('accessToken'); //Берёт из localStorage ваш accessToken
    opts.headers = {...(opts.headers || {}), 'Authorization': `Bearer ${access}`};
    //Вставляет в заголовок Authorization: Bearer <токен>
    return fetch(url, opts);
}

// Регистрация + автологин
const regForm = document.getElementById('registerForm');
//Находим форму с id="registerForm"
if (regForm) {
    regForm.addEventListener('submit', async e => {
        //Вешаем submit-обработчик, чтобы перехватить сабмит
        e.preventDefault();
        try {
            const data = {//Собираем поля email, password, флаг tutor
                email: regForm.email.value,
                password: regForm.password.value,
                tutor: regForm.tutor.checked
            };
            await postJson('/api/v1/auth/register', data);//создаём пользователя
            //Сразу же логинимся тем же email/password, получаем обратно { accessToken, refreshToken }
            const tokens = await postJson('/api/v1/auth/login', {
                username: data.email,
                password: data.password
            });
            //Сохраняем оба токена в localStorage
            localStorage.setItem('accessToken', tokens.accessToken);
            localStorage.setItem('refreshToken', tokens.refreshToken);
            //Перенаправляем браузер на главную фронтенд-часть /frontend
            window.location.href = '/frontend';
        } catch (err) {
            // err.message содержит JSON-строку с {"message":"..."}
            const {message} = JSON.parse(err.message);
            alert(message);                 // самый простой способ
            // или запишите message во внутристраничный <div class="alert alert-danger">…</div>
        }
    });
}

// Логин
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async e => {
        e.preventDefault();
        try {
            const tokens = await postJson('/api/v1/auth/login', {
                username: loginForm.username.value,
                password: loginForm.password.value
            });
            localStorage.setItem('accessToken', tokens.accessToken);
            localStorage.setItem('refreshToken', tokens.refreshToken);
            window.location.href = '/frontend';
        } catch (err) {
            // err.message содержит JSON-строку с {"message":"..."}
            const {message} = JSON.parse(err.message);
            alert(message);                 // самый простой способ
            // или запишите message во внутристраничный <div class="alert alert-danger">…</div>
        }
    });
}

// Logout‑кнопка (выйти)
window.addEventListener('DOMContentLoaded', () => {
    const logoutBtn = document.getElementById('logoutBtn');
    //Ищем кнопку с id="logoutBtn". Если её нет, выходим.
    if (!logoutBtn) return;
    //Если в localStorage нет accessToken, прячем кнопку через CSS-класс d-none
    //Если пользователь нажал «Выйти»:
    const hasToken = !!localStorage.getItem('accessToken');
    logoutBtn.classList.toggle('d-none', !hasToken);
    logoutBtn.addEventListener('click', () => {
        localStorage.clear();//Чистим все записи в localStorage (удаляем токены)
        // это вызовет Spring-Security logout и очистит куку
        window.location.href = '/frontend/logout'; //вернёт на страницу логина.
    });
});