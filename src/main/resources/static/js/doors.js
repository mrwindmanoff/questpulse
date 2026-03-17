// Быстрые двери с идеальным соприкосновением
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
        visibility: visible;
    `;

    // Левая дверь (50% ширины)
    const leftDoor = document.createElement('div');
    leftDoor.id = 'left-door-animation';
    leftDoor.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 50%;
        height: 100%;
        background: linear-gradient(135deg, #0a0f0f 0%, #1a1f2f 50%, #0a0f0f 100%);
        border-right: 6px solid #0ff;
        box-shadow: 0 0 40px #0ff, 0 0 80px #0ff inset;
        transition: transform 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        transform: translateX(0);
        z-index: 999999;
        transform-origin: left center;
        will-change: transform;
    `;

    // Правая дверь (50% ширины)
    const rightDoor = document.createElement('div');
    rightDoor.id = 'right-door-animation';
    rightDoor.style.cssText = `
        position: absolute;
        top: 0;
        right: 0;
        width: 50%;
        height: 100%;
        background: linear-gradient(135deg, #1a1f2f 0%, #0a0f0f 50%, #1a1f2f 100%);
        border-left: 6px solid #f0f;
        box-shadow: 0 0 40px #f0f, 0 0 80px #f0f inset;
        transition: transform 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
        transform: translateX(0);
        z-index: 999999;
        transform-origin: right center;
        will-change: transform;
    `;

    // Узор на дверях
    const leftPattern = document.createElement('div');
    leftPattern.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: repeating-linear-gradient(
            45deg,
            transparent,
            transparent 20px,
            rgba(0, 255, 255, 0.1) 20px,
            rgba(0, 255, 255, 0.1) 40px
        );
        pointer-events: none;
    `;
    leftDoor.appendChild(leftPattern);

    const rightPattern = document.createElement('div');
    rightPattern.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: repeating-linear-gradient(
            -45deg,
            transparent,
            transparent 20px,
            rgba(255, 0, 255, 0.1) 20px,
            rgba(255, 0, 255, 0.1) 40px
        );
        pointer-events: none;
    `;
    rightDoor.appendChild(rightPattern);

    // Текст посередине (тоже быстрее появляется/исчезает)
    const centerText = document.createElement('div');
    centerText.id = 'doors-text';
    centerText.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        color: #0ff;
        font-family: 'Courier New', monospace;
        font-size: 2.5rem;
        text-shadow: 0 0 20px #0ff, 0 0 40px #0ff;
        z-index: 1000000;
        text-align: center;
        opacity: 1;
        transition: opacity 0.3s ease;
        background: rgba(10, 15, 15, 0.9);
        padding: 1.5rem 3rem;
        border: 4px solid #0ff;
        box-shadow: 0 0 50px #0ff;
        letter-spacing: 8px;
        white-space: nowrap;
    `;
    centerText.innerHTML = 'ЗАГРУЗКА<div style="font-size: 1.5rem; margin-top: 0.5rem;" id="doors-dots">...</div>';

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

    // Анимация точек (быстрее не надо, оставим как есть)
    let dotsCount = 0;
    setInterval(function() {
        const dots = document.getElementById('doors-dots');
        if (dots) {
            dotsCount = (dotsCount + 1) % 4;
            dots.textContent = '.'.repeat(dotsCount);
        }
    }, 300);

    // Функция для открытия дверей (разъезжаются в стороны)
    window.openDoors = function() {
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        
        if (left && right) {
            // Двери разъезжаются ровно на свою ширину (50%)
            left.style.transform = 'translateX(-100%)';
            right.style.transform = 'translateX(100%)';
            
            // Текст исчезает быстрее
            if (text) {
                setTimeout(() => {
                    text.style.opacity = '0';
                }, 150);
            }
        }
    };

    // Функция для закрытия дверей (съезжаются до соприкосновения)
    window.closeDoors = function() {
        const left = document.getElementById('left-door-animation');
        const right = document.getElementById('right-door-animation');
        const text = document.getElementById('doors-text');
        
        if (left && right) {
            // Сразу показываем текст (без задержки)
            if (text) {
                text.style.opacity = '1';
            }
            
            // Двери съезжаются ровно до соприкосновения
            left.style.transform = 'translateX(0)';
            right.style.transform = 'translateX(0)';
        }
    };

    // При загрузке страницы двери открыты (разъехались)
    setTimeout(() => {
        window.openDoors();
    }, 50); // Ещё быстрее начинаем открывать

    // Перехватываем переходы по ссылкам
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.href && !link.href.includes('#')) {
            const isSameDomain = link.href.startsWith(window.location.origin);
            const isNotLogout = !link.href.includes('logout');
            
            if (isSameDomain && isNotLogout) {
                e.preventDefault();
                // Закрываем двери
                window.closeDoors();
                setTimeout(() => {
                    window.location.href = link.href;
                }, 600); // Ждём окончания анимации (0.6 секунды)
            }
        }
    });

    // Перехватываем отправку форм
    document.addEventListener('submit', function(e) {
        const form = e.target;
        if (!form.action || !form.action.includes('logout')) {
            window.closeDoors();
        }
    });

    // Для кнопки назад в браузере
    window.addEventListener('pageshow', function(event) {
        if (event.persisted) {
            window.openDoors();
        }
    });

    window.addEventListener('load', function() {
        setTimeout(() => {
            window.openDoors();
        }, 300);
    });
})();