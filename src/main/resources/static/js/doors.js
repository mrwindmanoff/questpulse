// Анимация дверей с плавным открытием и закрытием
(function() {
    // Создаём двери сразу при загрузке скрипта
    const doorsContainer = document.createElement('div');
    doorsContainer.id = 'doors-animation';
    doorsContainer.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 999999;
        pointer-events: none;
        display: flex;
        visibility: hidden;
    `;

    // Левая дверь
    const leftDoor = document.createElement('div');
    leftDoor.id = 'left-door-animation';
    leftDoor.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 50%;
        height: 100%;
        background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 100%);
        border-right: 4px solid #0ff;
        box-shadow: 0 0 30px #0ff;
        transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        transform: translateX(-100%);
        z-index: 999999;
    `;

    // Правая дверь
    const rightDoor = document.createElement('div');
    rightDoor.id = 'right-door-animation';
    rightDoor.style.cssText = `
        position: absolute;
        top: 0;
        right: 0;
        width: 50%;
        height: 100%;
        background: linear-gradient(135deg, #1a1f2f 0%, #0a0f0f 100%);
        border-left: 4px solid #f0f;
        box-shadow: 0 0 30px #f0f;
        transition: transform 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        transform: translateX(100%);
        z-index: 999999;
    `;

    // Текст посередине
    const centerText = document.createElement('div');
    centerText.id = 'doors-text';
    centerText.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: #0ff;
        font-family: 'Courier New', monospace;
        font-size: 2rem;
        text-shadow: 0 0 10px #0ff;
        z-index: 1000000;
        text-align: center;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    centerText.innerHTML = 'ЗАГРУЗКА<span id="doors-dots">...</span>';

    doorsContainer.appendChild(leftDoor);
    doorsContainer.appendChild(rightDoor);
    doorsContainer.appendChild(centerText);

    // Добавляем двери в body сразу
    if (document.body) {
        document.body.appendChild(doorsContainer);
    } else {
        document.addEventListener('DOMContentLoaded', function() {
            document.body.appendChild(doorsContainer);
        });
    }

    // Анимация точек
    let dotsCount = 0;
    setInterval(function() {
        const dots = document.getElementById('doors-dots');
        if (dots) {
            dotsCount = (dotsCount + 1) % 4;
            dots.textContent = '.'.repeat(dotsCount);
        }
    }, 300);

    // Функция для открытия дверей
    window.openDoors = function() {
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        const container = document.getElementById('doors-animation');
        
        if (left && right) {
            // Плавно открываем двери
            left.style.transform = 'translateX(-100%)';
            right.style.transform = 'translateX(100%)';
            if (text) {
                text.style.opacity = '0';
            }
            
            // Скрываем контейнер после анимации
            setTimeout(() => {
                if (container) {
                    container.style.visibility = 'hidden';
                }
            }, 800);
        }
    };

    // Функция для закрытия дверей (с плавной анимацией)
    window.closeDoors = function() {
        const container = document.getElementById('doors-animation');
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        
        if (container) {
            // Показываем контейнер перед анимацией
            container.style.visibility = 'visible';
        }
        
        if (left && right) {
            // Сначала показываем текст
            if (text) {
                text.style.opacity = '1';
            }
            
            // Небольшая задержка перед закрытием дверей для плавности
            setTimeout(() => {
                // Плавно закрываем двери
                left.style.transform = 'translateX(0)';
                right.style.transform = 'translateX(0)';
            }, 50);
        }
    };

    // При загрузке страницы двери открыты (по умолчанию)
    setTimeout(() => {
        window.openDoors();
    }, 100);

    // Перехватываем переходы по ссылкам
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.href && !link.href.includes('#')) {
            const isSameDomain = link.href.startsWith(window.location.origin);
            const isNotLogout = !link.href.includes('logout');
            
            if (isSameDomain && isNotLogout) {
                e.preventDefault();
                // Плавно закрываем двери перед переходом
                window.closeDoors();
                setTimeout(() => {
                    window.location.href = link.href;
                }, 800); // Ждём окончания анимации закрытия
            }
        }
    });

    // Перехватываем отправку форм (например, логин)
    document.addEventListener('submit', function(e) {
        const form = e.target;
        // Проверяем, что это не logout (чтобы не закрывать двери при выходе)
        if (!form.action || !form.action.includes('logout')) {
            window.closeDoors();
        }
    });

    // Для кнопки назад в браузере
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            // Страница загружена из кэша (при нажатии "назад")
            window.openDoors();
        }
    });
})();